package org.hnode.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

public class HNodeUDPSource extends HNodePacketSource 
{

	private DatagramSocket socket; 
	private RxThread RecvThread;
	    
	public HNodeUDPSource( Handler TxComplete, Handler RxPacket )
	{
		super(TxComplete, RxPacket);
	}
	
	public void start()
	{
		try
		{
			socket = new DatagramSocket();
			socket.setSoTimeout( 0 );
		}
		catch(SocketException e)
		{
			Log.w(TAG, String.format("Socket Open failed: %s\n", e));			
		}
		
        RecvThread = new RxThread();
        RecvThread.start();

	}
	
	public void stop()
	{
		RecvThread.setState(RxThread.STATE_DONE);
		socket.close();	
	}

	public InetAddress getAddress() throws UnknownHostException 
	{
		return InetAddress.getLocalHost();
	}

	public void sendPacket(HNodeAddress nodeAddress, HNodePacket packet) 
	{
	    DatagramPacket dgram = new DatagramPacket(packet.getPayload(), packet.length(), nodeAddress.getIPv4Address(), nodeAddress.getIPv4Port());

	    try
	    {
	    	socket.send( dgram );
		}
		catch(SocketException e)
		{
			Log.w(TAG, String.format("Socket Send failed: %s\n", e));			
		} 
		catch (IOException e) 
		{
			Log.w(TAG, String.format("Socket Send failed: %s\n", e));			
			e.printStackTrace();
		}

	}
	
    // Nested class that monitors the socket for data, forms packets, and dispatches formed packets.
    private class RxThread extends Thread 
    {
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        
        int mState;
        DatagramPacket dgram;
        byte[] tmpbuf = new byte[1500];
        
        public void run() 
        {
        	dgram = new DatagramPacket(tmpbuf, 1500);        	
            mState = STATE_RUNNING;   
            
            while (mState == STATE_RUNNING) 
            {
                try 
                {
            	    socket.receive( dgram );

                	Log.w(TAG, String.format("udp packet rx: %d\n", dgram.getLength()));
            
                	HNodePacket Packet = new HNodePacket(dgram.getLength());
                	Packet.set_bytes(dgram.getData(), dgram.getLength());
                	Packet.reset();
                	
                    RxPacketNotify(Packet);
                } 
                catch (IOException e) 
                {
                	Log.w(TAG, String.format("Socket Rx failed: %s\n", e));
                }

            }
        }
        
         // sets the current state for the thread,
         // used to stop the thread 
        public void setState(int state) 
        {
            mState = state;
        }
    }

}
