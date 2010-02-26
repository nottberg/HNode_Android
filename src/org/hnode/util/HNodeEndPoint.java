// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode.util;

import java.net.InetAddress;

import org.hnode.HNodeRecord;

import android.util.Log;

public class HNodeEndPoint {

	public final static String TAG = HNodeEndPoint.class.toString();

	InetAddress EPAddress;
	int         EPPort;
	
	short       MicroVersion;
	byte        MinorVersion;
	byte        MajorVersion;
	
	int         AssociateEPIndex;
	
	int         MimeTypeLength;
	String      MimeTypeStr;
	
	public void parseUpdatePacket(HNodePacket Packet, InetAddress NodeAddr) 
	{
		char[] tmpMimeStr;
		
	    // Update the address for this specific endpoint.
		EPAddress = NodeAddr;
	    EPPort    = Packet.get_short();
	    
    	Log.w(TAG, String.format("Port: %d\n", EPPort));

	    MicroVersion = (short)Packet.get_short();
	    MinorVersion = Packet.get_byte();
	    MajorVersion = Packet.get_byte();

    	Log.w(TAG, String.format("Version: %d.%d.%d\n", MajorVersion, MinorVersion, MicroVersion));

	    AssociateEPIndex = Packet.get_uint(); 

    	Log.w(TAG, String.format("Assoc EP Index: %d\n", AssociateEPIndex));

	    MimeTypeLength = Packet.get_uint();
	    tmpMimeStr = Packet.get_chars(MimeTypeLength);
    	MimeTypeStr = new String(tmpMimeStr);
    	
    	Log.w(TAG, String.format("Type String: %s\n", MimeTypeStr));
	}

	public String getEPType() 
	{
		return MimeTypeStr;
	}

	public String getEPRevision() 
	{
		return String.format("Version: %d.%d.%d\n", MajorVersion, MinorVersion, MicroVersion);
	}

	public int getEPPort() 
	{	
		return EPPort;
	}

}
