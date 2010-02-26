// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.DNSConstants;
import javax.jmdns.impl.DNSRecord;
import javax.jmdns.impl.JmDNSImpl;

import android.util.Log;
import android.content.ContentValues;
import android.os.Handler;
import android.os.Message;

import org.hnode.HNodeBrowser;
import org.hnode.HNodeMPoint;
import org.hnode.HNodeRecord;
import org.hnode.HNode.Nodes;
import org.hnode.util.HNodeAddress;
import org.hnode.util.SwitchControl;

/**
 * HNode Browser implementation in Java.
 *
 * @version %I%, %G%
 * @author  Curtis Nottberg
 */
public class HNodeBrowserImpl extends HNodeBrowser
{
	public final static String TAG = HNodeBrowserImpl.class.toString();
	
	protected JmDNS       jmdns = null;
	protected JmDNSImpl   impl = null;
	protected ServiceListener listener;
	protected ServiceInfo     info;
	
	public final static String HNODE_TYPE = "_hmnode._tcp.local.";

	protected String      HostName;
	protected InetAddress HostAddr;
	protected Handler     nodeAddedCB;
	
	private HNodeRecord[] HnodeArray;
	private HNodeMPoint[] MPointArray;
	
	protected List<HNodeMPoint> MPointList = new LinkedList<HNodeMPoint>();
	protected List<HNodeRecord> NodeList = new LinkedList<HNodeRecord>();

	   /**
     * Return the HostName associated with this HNodeBrowser instance.
     * Note: May not be the same as what started.  The host name is subject to
     * negotiation.
     */
    public String getHostName()
    {
    	return(HostName);
    }

    /**
     * Return the address of the interface to which this instance of HNodeBrowser is
     * bound.
     */
    public InetAddress getInterface()
    {
    	return(HostAddr);
    }

    /**
     * Stop the MDNS portion of HNodeBrowser. Retain any discovered management servers and associated nodes.
     */    
	public void stopMDNS() 
	{
		jmdns.removeServiceListener(HNODE_TYPE, listener);
		jmdns.close();
		jmdns = null;
		impl  = null;
	}

    /**
     * Start the MDNS portion of HNodeBrowser. 
     */    
	public void startMDNS() throws Exception
	{
		if(jmdns != null)
			this.stopMDNS();
		
		jmdns = JmDNS.create(HostAddr, HostName);
		jmdns.addServiceListener(HNODE_TYPE, listener);
		
		impl = (JmDNSImpl)jmdns;		
	}

    /**
     * Close down HNodeBrowser. Release all resources and unregister all services.
     */
    public void stop()
    {
    	// Shutdown the MDNS portion.
    	if(jmdns != null)
    		this.stopMDNS();
		
		// Cleanup Mpoint list
		Iterator<HNodeMPoint> MPIter = MPointList.iterator();
		while(MPIter.hasNext())
		{
			HNodeMPoint tmpMP = MPIter.next();
			tmpMP.stop();
			tmpMP = null;
		}
		
		// Clear the switch list
		MPointList.clear();

		// Cleanup hnode list
		Iterator<HNodeRecord> NodeIter = NodeList.iterator();
		while(NodeIter.hasNext())
		{
			HNodeRecord tmpNode = NodeIter.next();
			tmpNode = null;
		}
		
		// Clear the switch list
		NodeList.clear();

		// trigger adapter refresh
    }

    /**
     * List Nodes and associated MPoints.
     * Debugging Only
     */
    public void printNodes()
    {
    	
    }

    /**
     * Returns a list of discovered Management Points.
     * 
     * @return An array of MPoint Objects.
     */
    public HNodeMPoint[] MPointList()
    {
    	return(MPointArray);
    }

    /**
     * Returns a list of discovered HNodes.
     *
     * @return An array of HNodeRecord objects.
     */
    public HNodeRecord[] HNodeList()
    {
    	return(HnodeArray);
    }

	// this screen will run a network query of all libraries
	// upon selection it will try authenticating with that library, and launch the pairing activity if failed
	public void start() throws Exception
	{
		startMDNS();
	}

	public Handler hnodeAdded = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			int i;
			HNodeRecord Node = (HNodeRecord)msg.obj;
			
			Log.w(TAG, "hnodeAdded event\n");
			NodeList.add(Node);

			// Tell the next layer up.
			if( nodeAddedCB != null )
			{
            	// Send message to Browser about new node. 
            	nodeAddedCB.sendMessage(Message.obtain(nodeAddedCB,-1, Node));
			}			
		}
	};
	
	public final static int DELAY = 500;

	public Handler resultsUpdated = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			HNodeMPoint MPoint;
			HNodeAddress Address;
			
			Log.w(TAG, String.format("resultsUpdated(event=\n%s\n)", msg.toString()));
			
			// fetch the dns txt record to get library info
			String dnsname = (String)msg.obj;
			Log.w(TAG, dnsname);

			// find the parent computer running this service
			DNSRecord.Service srvrec = (DNSRecord.Service)impl.getCache().get(dnsname, DNSConstants.TYPE_SRV, DNSConstants.CLASS_IN);
			String hostname = srvrec.server;
			
			Log.w(TAG, hostname);
			
			// finally, resolve A record for parent host computer
			DNSRecord.Address arec = (DNSRecord.Address)impl.getCache().get(hostname, DNSConstants.TYPE_A, DNSConstants.CLASS_IN);
			String addr = arec.getAddress().toString().replaceFirst("[^0-9\\.]", "");

			Log.w(TAG, addr);
			
			Log.w(TAG, String.format("%d", srvrec.port));
			
			Address = new HNodeAddress(arec.getAddress(), srvrec.port);
			
			// Make sure a MPoint for this management node doesn't already exist.
			Iterator<HNodeMPoint> MPIter = MPointList.iterator();
			while(MPIter.hasNext())
			{
				HNodeMPoint tmpMP = MPIter.next();
				if( tmpMP.getUriStr().equals( Address.getUriStr() ))
				{
					// This management server is already known so don't add a 
					// 2nd record pointing to it.
					return;
				}
			}

			// Create the new MPoint object to manage the communication with the server.
			MPoint = HNodeMPoint.create(Address, hnodeAdded);
			
			// Add the MPoint to the active list.
			MPointList.add( MPoint );
	
			// Kick off the MPoint enumeration request.
			Log.w(TAG, String.format("MPointList[0]: %s", MPointList.get(0).toString()));
			MPoint.start();
			MPoint = null;
			
			/*
			if(msg.obj != null)
				adapter.notifyFound((String)msg.obj);
			adapter.notifyDataSetChanged();
			*/
		}
	};

	/**
	 * Constructor
	 * @param nodeAdded 
	 */
	public HNodeBrowserImpl(InetAddress HostAddr, String HostNameStr, Handler nodeAdded)
	{
		// Initilize our class
		HostName = HostNameStr;
		HostAddr = HostAddr;
		nodeAddedCB = nodeAdded;
			
		// launch dns backend and fire a service search
		// on arrival update our adapter, but wait on actual dns resolution until user clicks
		// on click try negotiating with the library
		
		this.listener = new ServiceListener() 
		{

			public void serviceAdded(ServiceEvent event) 
			{
				
				// someone is yelling about their touch-able service (prolly itunes)
				// go figure out what their ip address is
				
				final String name = event.getName(),
					type = event.getType();
				
				// trigger delayed gui event
				// needs to be delayed because jmdns hasnt parsed txt info yet
				
				Log.w(TAG, String.format("serviceAdded(event=\n%s\n)", event.toString()));
				
				String address = String.format("%s.%s", name, type);
				resultsUpdated.sendMessageDelayed(Message.obtain(resultsUpdated, -1, address), DELAY);
				
			}

			public void serviceRemoved(ServiceEvent event) 
			{
			}

			public void serviceResolved(ServiceEvent event) 
			{
				Log.w(TAG, String.format("serviceResolved(event=\n%s\n)", event.toString()));
			}
			
		};		

	}


}
