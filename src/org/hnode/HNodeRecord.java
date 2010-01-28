//Copyright 2009-2010 Curtis Nottberg
//Licensed under Apache License version 2.0
//Original license LGPL
package org.hnode;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;

import org.hnode.impl.HNodeRecordImpl;
import org.hnode.HNodeMPoint;

import org.hnode.util.HNodePacket;

public abstract class HNodeRecord
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
    public static HNodeRecord create(HNodeMPoint MPoint)
    {
        return new HNodeRecordImpl(MPoint);
    }

    /**
     * Fully qualified service type name, such as <code>_http._tcp.local.</code> .
     */
    //public abstract String getType();

    /**
     * Unqualified service instance name, such as <code>foobar</code> .
     */
    //public abstract String getName();

    /**
     * Fully qualified service name, such as <code>foobar._http._tcp.local.</code> .
     */
    //public abstract String getQualifiedName();

    /**
     * Get the name of the server.
     */
    //public abstract String getServer();

    /**
     * Get the host address of the service (ie X.X.X.X).
     */
    //public abstract String getHostAddress();

    public abstract void parseUpdatePacket(HNodePacket Packet);
    
    /**
     * Get the InetAddress of the service.
     */
    public abstract InetAddress getInetAddress();

    /**
     * Get the port for the service.
     */
    public abstract int getPort();

    /**
     * Get the priority of the service.
     */
    //public abstract int getPriority();

    /**
     * Get the weight of the service.
     */
    //public abstract int getWeight();

    /**
     * Get the text for the serivce as raw bytes.
     */
    //public abstract byte[] getTextBytes();

    /**
     * Get the text for the service. This will interpret the text bytes
     * as a UTF8 encoded string. Will return null if the bytes are not
     * a valid UTF8 encoded string.
     */
    //public abstract String getTextString();

    /**
     * Get the URL for this service. An http URL is created by
     * combining the address, port, and path properties.
     */
    //public abstract String getURL();

    /**
     * Get the URL for this service. An URL is created by
     * combining the protocol, address, port, and path properties.
     */
    //public abstract String getURL(String protocol);

    /**
     * Get a property of the service. This involves decoding the
     * text bytes into a property list. Returns null if the property
     * is not found or the text data could not be decoded correctly.
     */
    //public abstract byte[] getPropertyBytes(String name);

    /**
     * Get a property of the service. This involves decoding the
     * text bytes into a property list. Returns null if the property
     * is not found, the text data could not be decoded correctly, or
     * the resulting bytes are not a valid UTF8 string.
     */
    //public abstract String getPropertyString(String name);

    /**
     * Enumeration of the property names.
     */
    //public abstract Enumeration getPropertyNames();

    public abstract String getNiceTextString();

	public abstract String getEPType(int EPIndex);

	public abstract int getEPCount();

	public abstract String getEPRevision(int i);

	public abstract String getUIDStr();

	public abstract String getNickName();

	public abstract String getAddressStr();

	public abstract int getEPPort(int EPIndex);
	
}
