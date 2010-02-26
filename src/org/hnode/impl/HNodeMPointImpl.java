// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode.impl;

import java.io.*;
import java.net.*;
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
import android.os.Handler;
import android.os.Message;

import org.hnode.HNodeBrowser;
import org.hnode.HNodeRecord;
import org.hnode.HNodeMPoint;
import org.hnode.util.HNodeAddress;
import org.hnode.util.HNodePacket;
import org.hnode.util.HNodeTCPSource;

/**
 * HNode Browser implementation in Java.
 *
 * @version %I%, %G%
 * @author  Curtis Nottberg
 */
public class HNodeMPointImpl extends HNodeMPoint
{
	public final static String TAG = HNodeMPointImpl.class.toString();

	private static final int HNODE_MGMT_NONE       = 0;
	private static final int HNODE_MGMT_SHUTDOWN   = 1;
	private static final int HNODE_MGMT_REQ_PINFO  = 2;
	private static final int HNODE_MGMT_RSP_PINFO  = 3;
	private static final int HNODE_MGMT_DEBUG_CTRL = 5;
	private static final int HNODE_MGMT_EVENT_CTRL = 6;
	private static final int HNODE_MGMT_ACK        = 7;
	private static final int HNODE_MGMT_DEBUG_PKT  = 8;
	private static final int HNODE_MGMT_EVENT_PKT  = 9;
	private static final int HNODE_MGMT_NODE_PKT   = 10;

	HNodeAddress   MgmtAddress;
	HNodeTCPSource MgmtSource;

	private List<HNodeRecord> HNodeList = new LinkedList<HNodeRecord>();

	Handler nodeAdded;
	
	//protected List<String> known = new LinkedList<String>();
	
    public HNodeMPointImpl(HNodeAddress Address, Handler nodeAdded) 
    {
    	Log.w(TAG, "MPoint constructor\n");

    	this.nodeAdded = nodeAdded;
    	
    	MgmtAddress = Address;
     	MgmtSource = new HNodeTCPSource(txhandler, rxhandler, Address);
	}

    /**
     * Start the interaction with the managment node.  Called after the management
     * node is discovered via zeroconf.
     */
    public void start()
    {
    	MgmtSource.start();

        // Format the first request packet to the hnode management
        // server.
    	HNodePacket Packet = new HNodePacket();
    	
    	Packet.set_uint(12);
    	Packet.set_uint(HNODE_MGMT_REQ_PINFO);
    	Packet.set_uint(0x1);
    	
    	MgmtSource.sendPacket(Packet);
    		
       	Log.w(TAG, "output packet submitted\n");
    }

    // Define the Handler that receives messages from the thread and update the progress
    final Handler rxhandler = new Handler() 
    {
   	
        public void handleMessage(Message msg)
        {
        	HNodePacket Packet = (HNodePacket) msg.obj;
        	
        	int length = Packet.get_uint();
        	int type = Packet.get_uint();
        	int tag = Packet.get_uint();
        			
        	switch(type)
        	{
        		case HNODE_MGMT_NONE:      
        		case HNODE_MGMT_SHUTDOWN:   
        		case HNODE_MGMT_REQ_PINFO: 
        		case HNODE_MGMT_RSP_PINFO:  
        		case HNODE_MGMT_DEBUG_CTRL: 
        		case HNODE_MGMT_EVENT_CTRL:         
        		case HNODE_MGMT_DEBUG_PKT:  
        		case HNODE_MGMT_EVENT_PKT:  
        		default:
                	Log.w(TAG, String.format("Rx Packet: %d, %d, 0x%x\n", length, type, tag));
        		break;
        				
        		case HNODE_MGMT_NODE_PKT:
                	Log.w(TAG, String.format("Rx Node Packet: %d, 0x%x\n", length, tag));
                	
                	HNodeRecord Node = HNodeRecord.create(HNodeMPointImpl.this);
                	
                	Node.parseUpdatePacket(Packet);
                	
                	HNodeList.add(Node);
                	
                	// Send message to Browser about new node. 
                	nodeAdded.sendMessage(Message.obtain(nodeAdded,-1, Node));
        		break;
        			
        		case HNODE_MGMT_ACK:
                	Log.w(TAG, String.format("Rx Ack Packet: 0x%x\n", tag));
        		break;
        	}
        }
    };

    // Define the Handler that receives messages from the thread and update the progress
    final Handler txhandler = new Handler() 
    {
        public void handleMessage(Message msg) 
        {
           	Log.w(TAG, "TxPacket\n");
        }
    };
   
    /**
     * Shutdown the connection to the management point.
     */
    public void stop()
    {
    	MgmtSource.stop();    	
    }

    /**
     * Set the address of the associated management node.
     */
    public void setAddress(InetAddress Address)
    {
    	
    }

    /**
     * Tell the management server that we wish to receive debug packets.
     */
    public void enableDebug()
    {
    	
    }

    /**
     * Get the address of the management HNode
     */
    public InetAddress getAddress()
    {
    	return(MgmtSource.getAddress());
    }

    /**
     *  Get the URI of the management server that this MPoint represents.
     */
	public String getUriStr()
	{
		return MgmtAddress.getUriStr();
	}

    /**
     * Return a list of the HNodes associated with this management point
     */    
    public HNodeRecord[] HNodeList()
    {
    	return null;
    	//return(HNodeArray);
    }
 
    public String getNiceTextString()
    {
    	return("Bob2");
    }

}

