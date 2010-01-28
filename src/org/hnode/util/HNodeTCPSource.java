package org.hnode.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

public class HNodeTCPSource extends HNodePacketSource 
{
	HNodeAddress RemoteAddr;
	
	Socket client;
    DataInputStream input;
    DataOutputStream output;
	private RxThread RecvThread;

	public HNodeTCPSource( Handler TxComplete, Handler RxPacket, HNodeAddress Address)
	{
		super(TxComplete, RxPacket);
	
		RemoteAddr = Address;
	}

	/**
    * Get the address of the management HNode
    */
    public InetAddress getAddress()
    {
    	return(RemoteAddr.getIPv4Address());
    }

	public void sendPacket(HNodePacket Packet)
	{
        // Ensure that the transmission can be performed.
        if (client == null || input == null || output == null)
        	return;

    	try 
    	{
    		// Write the data into the socket.
    		output.write(Packet.getPayload(), 0, Packet.length());

            Log.w(TAG, "packet sent\n");

        }
        catch (UnknownHostException e)
        {
        	Log.w(TAG, "Trying to connect to unknown host: " + e);
        } 
        catch (IOException e) 
        {
        	Log.w(TAG, "IOException:  " + e);
        }        		
	}
	
	public void start()
	{
    	Log.w(TAG, String.format("address: %s, port: %d\n", RemoteAddr.toString(), RemoteAddr.getIPv4Port()));

        try 
        {
               client = new Socket(RemoteAddr.getIPv4Address(), RemoteAddr.getIPv4Port());
               input  = new DataInputStream(client.getInputStream());
               output = new DataOutputStream(client.getOutputStream());
        }
        catch (IOException e) 
        {
           	Log.w(TAG, String.format("Socket Open failed: %s\n", e));
        }

       	Log.w(TAG, String.format("TCP Socket open: %s, %s, %s\n", client.toString(), input.toString(), output.toString()));

        RecvThread = new RxThread(input);
        RecvThread.start();

    }
	
	public void stop()
	{
		RecvThread.setState(RxThread.STATE_DONE);
		
		try
		{
            output.close();
            input.close();
			client.close();
		}
		catch (IOException e) 
		{
			Log.w(TAG, String.format("Socket Open failed: %s\n", e));
		}

	}
	
    // Nested class that monitors the socket for data, forms packets, and dispatches formed packets.
    private class RxThread extends Thread 
    {
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        int mState;
        int total;
        int length;
        DataInputStream input;
        
        RxThread(DataInputStream inputStream)
        {
        	input = inputStream;
        }
        
        public void run() 
        {
            mState = STATE_RUNNING;   
            total = 0;
            while (mState == STATE_RUNNING) 
            {
                try 
                {
                	length = input.readInt();

                	Log.w(TAG, String.format("packet rx first: %d\n",length));
            
                	HNodePacket Packet = new HNodePacket(length);
                	Packet.set_uint(length);
                	
                	byte [] rxdata = new byte[length];
                	int pcount = input.read(rxdata, 0, (length-4));
                	
                	Log.w(TAG, String.format("packet rx second: %d\n", pcount));

                	Packet.set_bytes(rxdata, (length-4));
                	Packet.reset();
                	
                    RxPacketNotify(Packet);
                } 
                catch (IOException e) 
                {
                	Log.w(TAG, String.format("Socket Rx failed: %s\n", e));
                	mState = STATE_DONE;
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
