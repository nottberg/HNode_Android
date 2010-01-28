package org.hnode.util;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.hnode.HNodeRecord;
import org.hnode.impl.HNodeMPointImpl;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SwitchControl 
{
	public final static String TAG = SwitchControl.class.toString();

	private static final int SW_STATE_WAIT_LIST = 0;
	private static final int SW_STATE_WAIT_ON   = 1;
	private static final int SW_STATE_WAIT_OFF  = 2;

	private static final int SWPKT_SWITCH_LIST_REQUEST            = 1;
	private static final int SWPKT_SWITCH_LIST_REPLY              = 2;
	private static final int SWPKT_SWITCH_STATE_REQUEST           = 3;
	private static final int SWPKT_SWITCH_STATE_REPLY             = 4;
	private static final int SWPKT_SWITCH_CMD_REQUEST             = 5;
	private static final int SWPKT_SWITCH_CMD_REPLY               = 6;
	private static final int SWPKT_SWITCH_REPORT_SETTING_REQUEST  = 7;
	private static final int SWPKT_SWITCH_REPORT_SETTING_REPLY    = 8;
	private static final int SWPKT_SWITCH_SET_SETTING_REQUEST     = 9;
	private static final int SWPKT_SWITCH_SET_SETTING_REPLY       = 10;
	private static final int SWPKT_SWITCH_REPORT_SCHEDULE_REQUEST = 11;
	private static final int SWPKT_SWITCH_REPORT_SCHEDULE_REPLY   = 12;
	private static final int SWPKT_SWITCH_SET_SCHEDULE_REQUEST    = 13;
	private static final int SWPKT_SWITCH_SET_SCHEDULE_REPLY      = 14;

	private static final int SWINF_CMD_TURN_ON  = 1;
	private static final int SWINF_CMD_TURN_OFF = 2;

	HNodeUDPSource ControlSource;

	//private List<SwitchRecord> SwitchList = new LinkedList<SwitchRecord>();

	HNodeAddress NodeAddress;
	Handler reqComplete;
	private int State;
	private int ReqTag;
	private String UIDStr;
	
	public SwitchControl(HNodeAddress NodeAddress, Handler reqComplete, String UIDStr) 
    {
    	Log.w(TAG, "SwitchControl constructor\n");

    	this.UIDStr = UIDStr;
    	this.reqComplete = reqComplete;
    	this.NodeAddress = NodeAddress;
    	
     	ControlSource = new HNodeUDPSource(txhandler, rxhandler);
	}

    /**
     * Start the interaction with the managment node.  Called after the management
     * node is discovered via zeroconf.
     */
    public void start()
    {
    	ControlSource.start();
    		
       	Log.w(TAG, "output packet submitted\n");
    }

    // Define the Handler that receives messages from the thread and update the progress
    final Handler rxhandler = new Handler() 
    {
   	
        public void handleMessage(Message msg)
        {
        	HNodePacket Packet = (HNodePacket) msg.obj;
        	int TmpLen;
        	String TmpStr;
        	int i;
        	int SwitchCount;
        	
        	int length = Packet.length();
        	int type = Packet.get_uint();
        	int tag = Packet.get_uint();
        			
        	switch(type)
        	{
        		case SWPKT_SWITCH_LIST_REPLY:
                	Log.w(TAG, String.format("Switch List Reply\n"));
                	
                    // Number of switch records from this node.
                    SwitchCount = Packet.get_short(); // Number of switch records.

                    for(i = 0; i < SwitchCount; i++)
                    {
                    	SwitchRecord swrec = new SwitchRecord();
                    	
                    	swrec.setNodeUriStr(NodeAddress.getUriStr());
                    	swrec.setUIDStr(UIDStr);
                    	
                        swrec.setIndex(Packet.get_short()); // Local SwitchID

          		        TmpLen = Packet.get_short(); // Switch ID Length
          		        TmpStr = new String(Packet.get_chars(TmpLen)); // Get the id string.
          		        
          		        swrec.setIDStr(TmpStr);
     
          		        TmpLen = Packet.get_short(); // Switch Name Length
          		        TmpStr = new String(Packet.get_chars(TmpLen)); // Get the Name string.
          		        
          		        swrec.setNameStr(TmpStr);

          		        swrec.setCapabilities(Packet.get_uint()); 

          		        reqComplete.sendMessage(Message.obtain(reqComplete, -1, swrec));
                    }
       			break;

        		case SWPKT_SWITCH_CMD_REPLY:
                	Log.w(TAG, String.format("Command Reply\n"));
        		break;

        		case SWPKT_SWITCH_STATE_REPLY:
        		default:
        			Log.w(TAG, String.format("Rx Packet: %d, %d, 0x%x\n", length, type, tag));

       			break;

         	}
        }
    };

    // Define the Handler that receives messages from the thread and update the progress
    final Handler txhandler = new Handler() 
    {
        public void handleMessage(Message msg) 
        {
           	Log.w(TAG, "TxPacket\n");
        }
    };
   
    /**
     * Shutdown the connection to the management point.
     */
    public void stop()
    {
    	ControlSource.stop();    	
    }

    /**
     * Set the address of the associated management node.
     */
    public void setAddress(HNodeAddress Address)
    {
    	NodeAddress = Address;
    }

    /**
     * Get the address of the management HNode
     */
    public HNodeAddress getAddress()
    {
    	return(NodeAddress);
    }

    /**
     * Request a enumeration of the switches managed by the targeted hnode.
     */
    public void RequestSwitchList()
    {
    	State   = SW_STATE_WAIT_LIST;
    	ReqTag += 1;
    	
        // Format the first request packet to the hnode management
        // server.
    	HNodePacket Packet = new HNodePacket();
    	
    	Packet.set_uint(SWPKT_SWITCH_LIST_REQUEST);
    	Packet.set_uint(ReqTag);
    	
    	ControlSource.sendPacket(NodeAddress, Packet);
    }
        
    /**
     * Request that a switch be turned on.
     */
    public void RequestSwitchOn(int SwitchIndex)
    {
    	State   = SW_STATE_WAIT_ON;
    	ReqTag += 1;
    	
        // Format the first request packet to the hnode management
        // server.
    	HNodePacket Packet = new HNodePacket();
    	
    	Packet.set_uint(SWPKT_SWITCH_CMD_REQUEST);
    	Packet.set_uint(ReqTag);
    	Packet.set_short((short)SwitchIndex);
    	Packet.set_short((short)SWINF_CMD_TURN_ON);
    	Packet.set_short((short)0);
    	Packet.set_short((short)0);

    	ControlSource.sendPacket(NodeAddress, Packet);    	
    }

    /**
     * Request that a switch be turned off.
     */
    public void RequestSwitchOff(int SwitchIndex)
    {
    	State   = SW_STATE_WAIT_OFF;
    	ReqTag += 1;
    	
        // Format the first request packet to the hnode management
        // server.
    	HNodePacket Packet = new HNodePacket();
    	
    	Packet.set_uint(SWPKT_SWITCH_CMD_REQUEST);
    	Packet.set_uint(ReqTag);
    	Packet.set_short((short)SwitchIndex);
    	Packet.set_short((short)SWINF_CMD_TURN_OFF);
    	Packet.set_short((short)0);
    	Packet.set_short((short)0);

    	ControlSource.sendPacket(NodeAddress, Packet);    	
    }

    
    public int GetSwitchCount()
    {
    	return 0;
    }
    
    public String GetSwitchID(int SwitchIndex)
    {
    	return "";
    }
 
    public String GetSwitchName(int SwitchIndex)
    {
    	return "";
    }
    
    public String GetSwitchCapabilities(int SwitchIndex)
    {
    	return "";
    }

    
    
}
