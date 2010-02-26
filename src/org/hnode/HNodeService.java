// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hnode.HNode.Nodes;
import org.hnode.HNode.Switch;
import org.hnode.util.HNodeAddress;
import org.hnode.util.SwitchControl;
import org.hnode.util.SwitchRecord;
import org.mdns.browser.BrowseActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class HNodeService extends Service {
	
	public final static String TAG = HNodeService.class.toString();
	
	public final static String HOSTNAME = "droid1";

	protected static final String HNODE_SWITCH_EPTYPE = "hnode-switch-interface";

	public final static int HNS_STATE_IDLE                = 0;
	public final static int HNS_STATE_ACTIVE              = 1;
	public final static int HNS_STATE_ACTIVE_NO_MDNS     = 2;
	
	private int State;
	
	private HNodeBrowser HNBrowser;

	WifiManager wifi;
	MulticastLock MCastLock;
	
	private List<SwitchControl> SwCtlList = new LinkedList<SwitchControl>();

	InetAddress LocalAddress;
	private String SSID;

	private Handler serviceHandler = null;
	int counter;
	
	private void clearData()
	{
		String SelectStr;
		int count;
		
		// Clear the provider information
	    SelectStr = String.format("(%s=\"%s\")", Nodes.SSID, SSID);

	    // delete rows
	    count = getContentResolver().delete(Nodes.CONTENT_URI, SelectStr, null);

	    Log.w(TAG, String.format("Node delete count: %d", count));
	
		// Cleanup switch list
		Iterator<SwitchControl> iter = SwCtlList.iterator();
		while(iter.hasNext())
		{
			SwitchControl tmpSwitch = iter.next();
			tmpSwitch.stop();
			tmpSwitch = null;
		}
		
		// Clear the switch list
		SwCtlList.clear();
		
		// Clear the provider information
	    SelectStr = String.format("(%s=\"%s\")", Switch.SSID, SSID);

	    // delete rows
	    count = getContentResolver().delete(Switch.CONTENT_URI, SelectStr, null);
	    
	    Log.w(TAG, String.format("Switch delete count: %d", count));
	}
	
	/**
	 * @see android.app.Service#onBind(Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) 
	{
		return binder;
	}

	/**
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() 
	{
		counter = 0;
		
		State = HNS_STATE_IDLE;
		
		// Listen for Network events.
		registerForWifiBroadcasts();

		// Get setup to use the WiFi/Network.
		try 
		{			
			// figure out our wifi address, otherwise bail
			wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
			
			WifiInfo wifiinfo = wifi.getConnectionInfo();
			
			SSID = wifiinfo.getSSID();
			if(SSID == null)
				SSID = "hnode_fake_ssid";
				
			int intaddr = wifiinfo.getIpAddress();			
			byte[] byteaddr = new byte[] { (byte)(intaddr & 0xff), (byte)(intaddr >> 8 & 0xff), (byte)(intaddr >> 16 & 0xff), (byte)(intaddr >> 24 & 0xff) };
			LocalAddress = InetAddress.getByAddress(byteaddr);
			
			Log.d(TAG, String.format("found intaddr=%d, addr=%s, ssid=%s", intaddr, LocalAddress.toString(), SSID));

			MCastLock = wifi.createMulticastLock("mylock");
			
			serviceHandler = new Handler();
		}
		catch (Exception e) 
		{
		   Log.d(TAG, String.format("onCreate Error: %s", e.getMessage()));
		}
		
		// Kick off the enumeration of the local hnodes.
		startHNodeEnumeration();
	}

	public void startHNodeEnumeration() 
	{
		// Check the state to determine next actions.
		switch( State )
		{
			case HNS_STATE_ACTIVE:
			break;
		
			case HNS_STATE_ACTIVE_NO_MDNS:
				try
				{
					// Start receiving Multicast packets.
					MCastLock.acquire();	

					// Restart the MDNS discovery for new nodes.
					HNBrowser.startMDNS();

					// Start a timer to stop the enumeration after a reasonable period.
					serviceHandler.removeCallbacks(mUpdateTimeTask);
					serviceHandler.postDelayed( mUpdateTimeTask, 30*1000L );

		 		    // Remember what we are doing.
					State = HNS_STATE_ACTIVE;
				}
				catch (Exception e) 
				{
					Log.d(TAG, String.format("startHNodeEnumeration Error: %s", e.getMessage()));
				}
			break;
			
			case HNS_STATE_IDLE:
				try 
				{	
					// Start receiving Multicast packets.
					MCastLock.acquire();	

					Log.d(TAG, "T1");
					
					// Fire up the hnode enumeration code.
					HNBrowser = HNodeBrowser.create(LocalAddress, HOSTNAME, hnodeAdded);

		  		    Log.d(TAG, "T2");

					HNBrowser.start();

		 		    Log.d(TAG, "T3");

					// Start a timer to stop the enumeration after a reasonable period.
					serviceHandler.removeCallbacks(mUpdateTimeTask);
					serviceHandler.postDelayed( mUpdateTimeTask, 30*1000L );

		 		    Log.d(TAG, "T4");

		 		    // Remember what we are doing.
					State = HNS_STATE_ACTIVE;
				}
				catch (Exception e) 
				{
				   Log.d(TAG, String.format("startHNodeEnumeration Error: %s", e.getMessage()));
				}
			break;
			
		}
		
		// Start up the service browser.
		
	}

	public void stopHNodeEnumeration() 
	{
		// Check the state to determine next actions.
		switch( State )
		{
			case HNS_STATE_ACTIVE:
				// Shutdown the multicast to save battery life.
				MCastLock.release();
				
				// Shutdown the MDNS scan for management hnodes.
				HNBrowser.stopMDNS();

				// MDNS is shutdown now.
				State = HNS_STATE_ACTIVE_NO_MDNS;
			break;
		
			case HNS_STATE_ACTIVE_NO_MDNS:
			case HNS_STATE_IDLE:
			break;
		}
	}


	
	public void resetHNodeEnumeration() 
	{
		// Check the state to determine next actions.
		switch( State )
		{
			case HNS_STATE_ACTIVE:
				// Shutdown the multicast to save battery life.
				MCastLock.release();
				
				// Shutdown the connection to any previously found managers
				HNBrowser.stop();
			
				// Clear out any existing records of nodes, switches, etc.
				clearData();
				HNBrowser = null;
			break;
		
			case HNS_STATE_ACTIVE_NO_MDNS:
				// Shutdown the connection to any previously found managers
				HNBrowser.stop();
			
				// Clear out any existing records of nodes, switches, etc.
				clearData();
				HNBrowser = null;
			break;
			
			case HNS_STATE_IDLE:
			break;
		}

		// Should be in an idle state now.
	    State = HNS_STATE_IDLE;
	}

	/**
	 * @see android.app.Service#onStartCommand(Intent,int,int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{	
		return START_NOT_STICKY;
	}

	private Runnable mUpdateTimeTask = new Runnable() 
	{
		public void run() 
  	  	{
			++counter;
		
			Log.d(TAG, String.format("counter update: %d", counter));

			// Check the state to determine next actions.
			switch( State )
			{
				case HNS_STATE_ACTIVE:
					stopHNodeEnumeration();
					
					// Wake up again in 10 minutes to scan again.
					serviceHandler.removeCallbacks(mUpdateTimeTask);
					serviceHandler.postDelayed( mUpdateTimeTask, 30*1000L );
				break;
			
				case HNS_STATE_ACTIVE_NO_MDNS:
				break;
				
				case HNS_STATE_IDLE:
					startHNodeEnumeration();
				break;
			}
  	  	}
  	};

	public class MyIntentReceiver extends BroadcastReceiver 
	{
		/**
		* @see adroid.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		*/
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.w(TAG, "Broadcast Receiver Test Intent Rx");
			Log.w(TAG, String.format("Action: %s", intent.getAction()));
		}

	} 
	
	
	private void registerForWifiBroadcasts() 
	{
		IntentFilter intentFilter = new IntentFilter();
		MyIntentReceiver intentRx = new MyIntentReceiver();

		intentFilter = new IntentFilter();
		
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
   
	    registerReceiver(intentRx, intentFilter);
	}
		   
		   
	public Handler hnodeAdded = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			int i;
			HNodeRecord Node = (HNodeRecord)msg.obj;
			
			Log.w(TAG, "Service -- hnodeAdded event\n");
			
			for(i = 0; i < Node.getEPCount(); i++)
			{
				ContentValues testVal = new ContentValues();
				testVal.put(Nodes.ENDPOINT, Node.getEPType(i));
				testVal.put(Nodes.REVISION, Node.getEPRevision(i));
				testVal.put(Nodes.UID, Node.getUIDStr());
				testVal.put(Nodes.NICKNAME, Node.getNickName());
				testVal.put(Nodes.SSID, SSID);
				testVal.put(Nodes.EPINDEX, i);
				
				// Format the URI for the endpoint (Change port)
				Uri NodeUri = Uri.parse(Node.getAddressStr());
				String EPUriStr = String.format("%s://%s:%d", NodeUri.getScheme(), NodeUri.getHost(),Node.getEPPort(i));
				testVal.put(Nodes.NODEURI, EPUriStr);
				
			    String SelectStr = String.format("(%s=\"%s\") AND (%s=\"%s\")", 
                        Nodes.UID, Node.getUIDStr(), Nodes.ENDPOINT, Node.getEPType(i));

			    // First try to update and existing row.
			    int rowsUpdated = getContentResolver().update(Nodes.CONTENT_URI, testVal, SelectStr, null);

			    // If that doesn't work then add the row as a new entry.
			    if( rowsUpdated == 0)
			    {
			    	getContentResolver().insert(Nodes.CONTENT_URI, testVal);
			    }
				
				Log.w(TAG, String.format("Service -- Checking for switch type: %s == %s\n", Node.getEPType(i), HNODE_SWITCH_EPTYPE));

				if( Node.getEPType(i).equals(HNODE_SWITCH_EPTYPE) )
				{
					Log.w(TAG, String.format("Service -- Enumerating switch node: %s\n", Node.getAddressStr()));

					HNodeAddress  Addr   = new HNodeAddress(EPUriStr);
					SwitchControl Switch = new SwitchControl(Addr, SwitchReqComplete, Node.getUIDStr());

					Switch.start();
					Switch.RequestSwitchList();
					
					SwCtlList.add(Switch);
				}
			}
			
		}
	};
	
	public Handler SwitchReqComplete = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			int i;
			SwitchRecord SwRec = (SwitchRecord)msg.obj;
			
			Log.w(TAG, "Service -- SwitchReqComplete event\n");
			
		    ContentValues testVal = new ContentValues();
		    testVal.put(Switch.IDSTR, SwRec.getIDStr());
		    testVal.put(Switch.NAME, SwRec.getNameStr());
		    testVal.put(Switch.CAPABILITIES, SwRec.getCapabilities());
		    testVal.put(Switch.REMOTE_ID, SwRec.getIndex());
		    testVal.put(Switch.NODEURI, SwRec.getNodeUriStr());
		    testVal.put(Switch.UID, SwRec.getUIDStr());
		    testVal.put(Switch.SSID, SSID);
		    
		    String SelectStr = String.format("(%s=\"%s\") AND (%s=%d)", 
		    		                          Switch.UID, SwRec.getUIDStr(), Switch.REMOTE_ID, SwRec.getIndex());
		    
		    // First try to update and existing row.
		    int rowsUpdated = getContentResolver().update(Switch.CONTENT_URI, testVal, SelectStr, null);
		    
		    // If that doesn't work then add the row as a new entry.
		    if( rowsUpdated == 0)
		    {
		    	getContentResolver().insert(Switch.CONTENT_URI, testVal);
		    }
		}
	};
	
    /**
     * The IHNodeService is defined through IDL
     */
    private final IHNodeService.Stub binder = 
			new IHNodeService.Stub() 
    		{
    			public int getCounterValue() 
    			{
    				return counter;
    			}

    			public String getNetID()
    			{
    				return SSID;
    			}
    			
    			public void resetAndEnumerate()
    			{
    				resetHNodeEnumeration();
    				startHNodeEnumeration();
    			}

    		};

}
