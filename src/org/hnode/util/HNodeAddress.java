package org.hnode.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hnode.HNodeService;

import android.net.Uri;
import android.util.Log;

public class HNodeAddress 
{
	public final static String TAG = HNodeAddress.class.toString();

	static final int ADDR_TYPE_NONE = 0x00000000;
	static final int ADDR_TYPE_IPV4 = 0x00000001;
	
	private int AddressType;

	Uri AddrUri;
	
	private InetAddress IPv4Address;
	private int IPv4Port;
	
	public HNodeAddress(InetAddress address, int port) 
	{
		String AddrStr;
		
		AddressType = ADDR_TYPE_IPV4;
		IPv4Address = address;
		IPv4Port    = port;

		Log.w(TAG, String.format("FromAddr -- Addr: %s, Port: %d\n", IPv4Address.toString(), IPv4Port));

		AddrStr = String.format("%s:%d", IPv4Address.getHostAddress(), IPv4Port);
		AddrUri = Uri.fromParts("hnode", AddrStr, null);
		
		Log.w(TAG, String.format("FromAddr -- AddrUri: %s, Addr: %s, Port: %d\n", AddrUri.toString(), IPv4Address.toString(), IPv4Port));
	}

	public HNodeAddress(String addressStr) 
	{
		AddrUri = Uri.parse(addressStr);
		
		try
		{
			IPv4Address = InetAddress.getByName(AddrUri.getHost());
			IPv4Port    = AddrUri.getPort();
		}
		catch(UnknownHostException e)
		{
			
		}
		
		Log.w(TAG, String.format("FromStr -- AddrUri: %s, Addr: %s, Port: %d\n", AddrUri.toString(), IPv4Address.toString(), IPv4Port));
	}

	public InetAddress getIPv4Address() 
	{
		// TODO Auto-generated method stub
		return IPv4Address;
	}

	public int getIPv4Port() 
	{
		// TODO Auto-generated method stub
		return IPv4Port;
	}

	public String getUriStr() 
	{
		// TODO Auto-generated method stub
		return AddrUri.toString();
	}
	
}
