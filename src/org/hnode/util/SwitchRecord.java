package org.hnode.util;

public class SwitchRecord 
{
	private String IDStr;
	private int    Index;
	private String NameStr;
	private int    Capabilities;
	private String UriStr;
	private String UIDStr;
	
	public void setIndex(int getShort) 
	{
		Index = getShort;		
	}

	public void setIDStr(String tmpStr) 
	{
		IDStr = tmpStr;
	}

	public void setNameStr(String tmpStr) 
	{
		NameStr = tmpStr;
	}

	public void setCapabilities(int getUint) 
	{
		Capabilities = getUint;
	}

	public void setNodeUriStr(String UriStr) 
	{
		this.UriStr = UriStr;
	}

	public void setUIDStr(String UIDStr) 
	{
		this.UIDStr = UIDStr;
	}

	public String getIDStr() 
	{
		return IDStr;
	}
	
	public String getNameStr() 
	{
		return NameStr;
	}

	public int getIndex() 
	{
		return Index;
	}

	public int getCapabilities()
	{
		return Capabilities;
	}

	public String getNodeUriStr() 
	{
		return UriStr;
	}

	public String getUIDStr() 
	{
		return UIDStr;
	}
	
}
