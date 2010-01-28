//Copyright 2009-2010 Curtis Nottberg
//Licensed under Apache License version 2.0
//Original license LGPL
package org.hnode;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;

import org.hnode.impl.HNodeMPointImpl;
import org.hnode.util.HNodeAddress;

import android.os.Handler;

public abstract class HNodeMPoint
{
    public final static byte[] NO_VALUE = new byte[0];

    /**
     * Construct a service description for registrating with JmDNS.
     *
     * @param type fully qualified service type name, such as <code>_http._tcp.local.</code>.
     * @param name unqualified service instance name, such as <code>foobar</code>
     * @param port the local port on which the service runs
     * @param text string describing the service
     */
    public static HNodeMPoint create(HNodeAddress Address, Handler hnodeAdded)
    {
        return new HNodeMPointImpl(Address, hnodeAdded);
    }

    /**
     * Start the interaction with the managment node.  Called after the management
     * node is discovered via zeroconf.
     */
    public abstract void start();

    /**
     * Shutdown the connection to the management point.
     */
    public abstract void stop();

    /**
     * Set the address of the associated management node.
     */
    public abstract void setAddress(InetAddress Address);

    /**
     * Tell the management server that we wish to receive debug packets.
     */
    public abstract void enableDebug();

    /**
     * Get the address of the management HNode
     */
    public abstract InetAddress getAddress();

    /**
     * Return a list of the HNodes associated with this management point
     */    
    public abstract HNodeRecord[] HNodeList();
 
    public abstract String getNiceTextString();

	public abstract String getUriStr();

}