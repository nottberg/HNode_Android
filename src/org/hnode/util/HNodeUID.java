package org.hnode.util;

public class HNodeUID 
{
	static final String FORMAT_STR = "%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x";
	byte[] UID = new byte[16];
	
	public HNodeUID(byte[] v)
	{
		System.arraycopy(v,0,UID,0,16);
	}

	public void setUID(byte[] v)
	{
		System.arraycopy(v,0,UID,0,16);
	}

	public String getUIDStr() 
	{
		// "0102:0304:0506:0708:fffe:fdfc:fbfa:f9f8";
		
		// TODO Auto-generated method stub
		return String.format(FORMAT_STR,
				             UID[0], UID[1], UID[2], UID[3], UID[4], UID[5], UID[6], UID[7],
				             UID[8], UID[9], UID[10], UID[11], UID[12], UID[13], UID[14], UID[15]);
	}
	
}
