// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode.util;

import org.hnode.impl.HNodeMPointImpl;

import android.os.Handler;
import android.os.Message;

public class HNodePacketSource 
{
	public final static String TAG = HNodePacketSource.class.toString();

	Handler TxComplete;
	Handler RxPacket;
	
	public HNodePacketSource( Handler TxComplete, Handler RxPacket )
	{
		this.TxComplete = TxComplete;
		this.RxPacket   = RxPacket;
	}
	
	public void SendPacket(HNodePacket Packet)
	{
		
	}
	
	public void Start()
	{
		
	}
	
	public void Stop()
	{
		
	}
	
	protected void RxPacketNotify(HNodePacket Packet)
	{
		RxPacket.sendMessage(Message.obtain(RxPacket, -1, Packet));
	}

	protected void TxPacketNotify(HNodePacket Packet)
	{
		TxComplete.sendMessage(Message.obtain(TxComplete, -1, Packet));
	}

}
