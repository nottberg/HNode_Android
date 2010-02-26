// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode.util;

import java.net.InetAddress;

import org.hnode.impl.HNodeBrowserImpl;

import android.util.Log;

public class HNodePacket 
{
	public final static String TAG = HNodePacket.class.toString();

	private byte[] payload;
	private int alloc, used;
	private static final int grow_size = 100;
	private static final int def_initsize = 100;

	/**
	 * Initializes the ByteArray with size 100 
	 */
	public HNodePacket() 
	{ 
		initialize(def_initsize); 
	}
	
	/**
	 * Initialized the ByteArray with given size
	 * @param init_size Initial size for the array.
	 */
	public HNodePacket(int init_size) 
	{ 
		initialize(init_size); 
	}

	/**
	 * Direct access to the packet data.
	 * @return
	 */
	public byte[] getPayload()
	{
		return payload;
	}
	
	/**
	 * Set the destination address for protocols that are not connection oriented. (ie. UDP)
	 * @param Address The destination address
	 * @param Port The desitnation port
	 */
	public void setAddress(InetAddress Address, int Port)
	{
		
	}
	
	/**
	 * Returns the length of the payload.
	 */
	public int length()
	{
		return used;
	}
	
	/**
	 * Reset the payload back to the initial insertion point.
	 */
	public void reset()
	{
		used = 0;
	}

	
	/**
	 * Set the int at the current position and increment
	 * @param v value to set
	 */
	public void set_uint(int v) 
	{ 
		if((used+4) >= alloc) grow(grow_size);
		
		payload[used++] = (byte)(0xff & (v >> 24));
		payload[used++] = (byte)(0xff & (v >> 16));
		payload[used++] = (byte)(0xff & (v >>  8));
		payload[used++] = (byte)(0xff & v);
		
	}

	/**
	 * Get the int at the current position and increment.
	 */
	public int get_uint() 
	{ 
		debugPrint();
		if( (used+4) >= alloc ) return 0;
		
		int v = ( ((payload[used++] & 0xff) << 24)
				| ((payload[used++] & 0xff) << 16) 
				| ((payload[used++] & 0xff) << 8) 
				|  (payload[used++] & 0xff) );

		return v;
	}

	/**
	 * Set the short at the current position and incrment.
	 * @param v value to set
	 */
	public void set_short(short v) 
	{ 
		if((used+2) >= alloc) grow(grow_size);
		
		payload[used++] = (byte)(0xff & (v >>  8));
		payload[used++] = (byte)(0xff & v);
	}

	/**
	 * Return the short at the current position and increment.
	 */
	public int get_short() 
	{ 
		debugPrint();
		if( (used+2) >= alloc ) return 0;
		
		int v = (int)(  ((payload[used++] & 0xff) << 8) 
			   	      |  (payload[used++] & 0xff) );

		return v;
	}

	/**
	 * Set the byte at the current position and increment.
	 * @param v value to set
	 */
	public void set_byte(byte v) 
	{ 
		if((used+1) >= alloc) grow(grow_size);
		
		payload[used++] = (byte)(0xff & v);
	}
	
	/**
	 * Return the byte at the current position and increment.
	 */
	public byte get_byte() 
	{ 
		debugPrint();
		if( (used+1) >= alloc ) return 0;
		
		byte v = (byte)(payload[used++] & 0xff);

		return v;
	}

	/**
	 * Add an array of bytes at the current position.
	 * @param v byte array to append
	 * @param length Number of bytes to copy
	 */
	public void set_bytes(byte[] v, int length) 
	{ 
		if( (used+length) >= alloc ) grow( (length+grow_size) );
		
		System.arraycopy(v,0,payload,used,length);
		used += length;
	}

	/**
	 * Return an array of bytes starting from the current position.
	 * @param length Number of bytes to copy
	 */
	public byte[] get_bytes(int length) 
	{ 
		debugPrint();
		if((used+length) >= alloc) return null;
		
		byte[] v = new byte[length];
		System.arraycopy(payload, used, v, 0, length);
		used += length;
		return v;
	}

	/**
	 * Return an array of chars starting from the current position.
	 * @param length Number of bytes to copy
	 */
	public char[] get_chars(int length) 
	{ 
		int i;
		debugPrint();
		if((used+length) >= alloc) return null;
		
		char[] v = new char[length];
		
		for(i = 0; i < length; i++)
		{
			v[i] = (char)payload[(i+used)];
		}
		used += length;
		return v;
	}

	/**
	 * Move the current position forward.
	 * @param length Number of bytes to skip
	 */
	public void skip_bytes(int length) 
	{ 
		if( (used+length) >= alloc ) grow( (length+grow_size) );
		used += length;
	}


	private void grow(int x) 
	{
		alloc += x;
		byte [] n = new byte[alloc];
		System.arraycopy(payload, 0, n, 0, used);
		payload = n;
	}
	
	private void initialize(int sz) 
	{
		payload = new byte[sz];
		alloc   = sz;
		used    = 0;
	}

	public void debugPrint()
	{
		//Log.w(TAG, String.format("used: %d, alloc: %d",used,alloc));
		//Log.w(TAG, String.format("data: %x %x %x %x", payload[used], payload[used+1], payload[used+2], payload[used+3]));
	}
}
