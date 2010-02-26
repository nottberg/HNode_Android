// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode;

import java.io.IOException;
import java.net.InetAddress;

import org.hnode.impl.HNodeBrowserImpl;

import android.os.Handler;
/**
 * HNode Browser implementation in Java.
 *
 * @version %I%, %G%
 * @author  Curtis Nottberg
 */

public abstract class HNodeBrowser
{
    /**
     * The version of HNodeBrowser.
     */
    public static String VERSION = "2.0";

    /**
     * Create an instance of JmDNS.
     */
    public static HNodeBrowser create(InetAddress addr, String hostname, Handler nodeAdded) throws IOException
    {
        return new HNodeBrowserImpl(addr, hostname, nodeAdded);
    }
    
    /**
     * Create an instance of JmDNS and bind it to a
     * specific network interface given its IP-address.
     */
    //public static JmDNS create(InetAddress addr) throws IOException
    //{
    //    return new JmDNSImpl(addr);
    //}
    
    /**
     * Return the HostName associated with this HNodeBrowser instance.
     * Note: May not be the same as what started.  The host name is subject to
     * negotiation.
     */
    public abstract String getHostName();

    /**
     * Return the address of the interface to which this instance of HNodeBrowser is
     * bound.
     */
    public abstract InetAddress getInterface() throws IOException;

    /**
     * Start the HNodeBrowser. Start searching for HNode on the local network.
     */
    public abstract void start() throws Exception;

    /**
     * Close down HNodeBrowser. Release all resources and unregister all services.
     */
    public abstract void stop();

    /**
     * Stop the MDNS portion of HNodeBrowser. Retain any discovered management servers and associated nodes.
     */    
	public abstract void stopMDNS();

	/**
     * Start the MDNS portion of HNodeBrowser. 
     */    	
	public abstract void startMDNS() throws Exception;

    /**
     * List Nodes and associated MPoints.
     * Debugging Only
     */
    public abstract void printNodes();

    /**
     * Returns a list of discovered Management Points.
     * 
     * @return An array of MPoint Objects.
     */
    public abstract HNodeMPoint[] MPointList();

    /**
     * Returns a list of discovered HNodes.
     *
     * @return An array of HNodeRecord objects.
     */
    public abstract HNodeRecord[] HNodeList();

}

