/**
 * 
 */
package org.hnode.impl;

import org.hnode.HNodeMPoint;
import org.hnode.util.HNodeEndPoint;
import org.hnode.util.HNodePacket;
import org.hnode.util.HNodeUID;
import org.hnode.HNodeRecord;

import android.util.Log;

import java.net.InetAddress;

/**
 * @author cnottberg
 *
 */
public class HNodeRecordImpl extends HNodeRecord {

	public final static String TAG = HNodeRecord.class.toString();

	InetAddress NodeAddress;
	int         NodePort;
	
	int   NodeID;
	int   ServObjID;
	int   Port;
	int   ConfigPort;
	
	short MicroVersion;
	byte  MinorVersion;
	byte  MajorVersion;
	
	HNodeUID NodeUID;
	
	int    AddrType;
	String AddrString;

	int EndPointCount;
	HNodeEndPoint [] EndPointArray;
	
	/**
	 * 
	 */
	public HNodeRecordImpl(HNodeMPoint MPoint) 
	{
		// TODO Auto-generated constructor stub
	}

    public void parseUpdatePacket(HNodePacket Packet)
    {
    	byte[] tmpUID;
    	char[] tmpAddress;
    	
    	int    AddrStrLength;
    	int    CurEPIndx;
    	
    	NodeID        = Packet.get_uint();
    	ServObjID     = Packet.get_uint();
    	Port          = Packet.get_short();
    	ConfigPort    = Packet.get_short();

    	Log.w(TAG, String.format("SOID: 0x%x, Port: %d, ConfigPort: %d\n", ServObjID, Port, ConfigPort));
    	
    	MicroVersion = (short)Packet.get_short();
    	MinorVersion = Packet.get_byte();
    	MajorVersion = Packet.get_byte();
   
    	Log.w(TAG, String.format("Version: %d.%d.%d\n", MajorVersion, MinorVersion, MicroVersion));

    	tmpUID = Packet.get_bytes(16);
    	NodeUID = new HNodeUID(tmpUID);
    	Log.w(TAG, String.format("UID: %s\n", NodeUID.getUIDStr()));

    	EndPointCount = Packet.get_uint();

    	Log.w(TAG, String.format("EP Count: %d\n", EndPointCount));

    	AddrType      = Packet.get_uint();
    	AddrStrLength = Packet.get_uint();
       	Log.w(TAG, String.format("AddrStrLen: %d", AddrStrLength));

    	tmpAddress = Packet.get_chars(AddrStrLength);

    	Log.w(TAG, String.format("AddrStrLen: %d, %c %c %c %c\n", AddrStrLength, tmpAddress[0],
    			tmpAddress[1], tmpAddress[2], tmpAddress[3]));

    	AddrString = new String(tmpAddress);

       	Log.w(TAG, String.format("AddrStr: %s\n", AddrString));

    	if(EndPointCount > 0)
    	{
    		EndPointArray = new HNodeEndPoint[EndPointCount];
    		
    		for(CurEPIndx=0; CurEPIndx < EndPointCount; CurEPIndx++)
    		{
    	       	Log.w(TAG, String.format("Parsing EP #%d\n", CurEPIndx));
    	       	EndPointArray[CurEPIndx] = new HNodeEndPoint(); 
    			EndPointArray[CurEPIndx].parseUpdatePacket(Packet, NodeAddress);
    		}
    	}
    }

    /**
     * Get the InetAddress of the service.
     */
    public InetAddress getInetAddress()
    {
    	return(NodeAddress);
    }

    /**
     * Get the port for the service.
     */
    public int getPort()
    {
    	return(NodePort);
    }


    public String getNiceTextString()
    {
        return("Bob1");	
    }

	@Override
	public int getEPCount() 
	{
		return EndPointCount;
	}

	@Override
	public String getEPType(int EPIndex) 
	{
		if( EPIndex >= EndPointCount )
			return null;
		
		return EndPointArray[EPIndex].getEPType();
	}

	@Override
	public String getEPRevision(int EPIndex) 
	{
		if( EPIndex >= EndPointCount )
			return null;
		
		return EndPointArray[EPIndex].getEPRevision();
	}

	@Override
	public int getEPPort(int EPIndex)
	{
		if( EPIndex >= EndPointCount )
			return 0;
		
		return EndPointArray[EPIndex].getEPPort();
	}

	@Override
	public String getNickName() 
	{
		return "testnick";
	}

	@Override
	public String getUIDStr() 
	{
		return NodeUID.getUIDStr(); 
	}
	
	public String getAddressStr()
	{
		return AddrString;
	}
}
