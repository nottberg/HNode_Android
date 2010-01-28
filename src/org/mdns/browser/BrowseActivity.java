package org.mdns.browser;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.hnode.HNodeRecord;
import org.hnode.HNodeBrowser;
import org.hnode.HNodeMPoint;
import org.hnode.HNodeService;
import org.hnode.IHNodeService;
import org.hnode.HNode.Nodes;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

//
//Displays a list of notes. Will display notes from the {@link Uri}
//provided in the intent if there is one, otherwise defaults to displaying the
//contents of the {@link NotePadProvider}
//
public class BrowseActivity extends ListActivity 
{
	public final static String TAG = BrowseActivity.class.toString();
	
	//public final static String HOSTNAME = "droid1";

	// Menu item ids
	public static final int MENU_ITEM_DELETE      = Menu.FIRST;
	public static final int MENU_ITEM_INSERT      = Menu.FIRST + 1;
	public static final int MENU_ITEM_SWITCH_LIST = Menu.FIRST + 2;
	public static final int MENU_ITEM_REDISCOVER  = Menu.FIRST + 3;

	//
	// The columns we are interested in from the database
	//
	private static final String[] PROJECTION = new String[] {
         Nodes._ID, // 0
         Nodes.ENDPOINT, // 1
         Nodes.NODEURI, // 2
         Nodes.SSID     // 3
	};

	// The index of the title column
	private static final int COLUMN_INDEX_TITLE = 1;


	private HNodeBrowser HNBrowser;

    private IHNodeService hnodeService;
    private HNodeServiceConnection conn;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Intent intent = getIntent();
		if (intent.getData() == null) 
		{
			intent.setData(Nodes.CONTENT_URI);
		}

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);
     
		// Perform a managed query. The Activity will handle closing and requerying the cursor
		// when needed.
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, Nodes.DEFAULT_SORT_ORDER);

		// Used to map notes entries from the database to views
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.hnodelist_item, cursor,
             new String[] { Nodes.ENDPOINT, Nodes.SSID }, new int[] { android.R.id.text1, android.R.id.text2 });
		setListAdapter(adapter);

		// Make sure the HNode discovery service is running.
		try 
		{
			// setup and start MyService
			Intent svc = new Intent(this, HNodeService.class);
			startService(svc);
		}
		catch (Exception e)
		{
			//Log.e(TAG, "ui creation problem", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);

		// Add global menu items
		//menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert)
		//			.setShortcut('3', 'a')
		//			.setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_ITEM_SWITCH_LIST, 0, R.string.menu_switch_list)
					.setShortcut('3', 's')
					.setIcon(android.R.drawable.ic_menu_more);
		menu.add(0, MENU_ITEM_REDISCOVER, 0, R.string.menu_rediscover)
					.setShortcut('4', 'r')
					.setIcon(android.R.drawable.ic_menu_revert);

		// Generate any additional actions that can be performed on the
		// overall list.  In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		//Intent intent = new Intent(null, getIntent().getData());
		//intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		//menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		//						new ComponentName(this, NotesList.class), null, intent, 0, null);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		super.onPrepareOptionsMenu(menu);
		final boolean haveItems = getListAdapter().getCount() > 0;

		// If there are any notes in the list (which implies that one of
		// them is selected), then we need to generate the actions that
		// can be performed on the current selection.  This will be a combination
		// of our own specific actions along with any extensions that can be
		// found.
		if (haveItems) 
		{
			// This is the selected item.
			Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

			// Build menu...  always starts with the EDIT action...
			Intent[] specifics = new Intent[1];
			specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
			MenuItem[] items = new MenuItem[1];

			// ... is followed by whatever other actions are available...
			Intent intent = new Intent(null, uri);
			intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
			menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0,
                 items);

			// Give a shortcut to the edit action.
			if (items[0] != null) 
			{
				items[0].setShortcut('1', 'e');
			}
		} 
		else 
		{
			menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
		}

		return true;
	}


    class HNodeServiceConnection implements ServiceConnection 
    {
        public void onServiceConnected(ComponentName className, IBinder boundService ) 
        {
          hnodeService = IHNodeService.Stub.asInterface((IBinder)boundService);
		  Log.d( TAG,"onServiceConnected" );
        }

        public void onServiceDisconnected(ComponentName className) 
        {
          hnodeService = null;
		  Log.d( TAG,"onServiceDisconnected" );
		  //updateServiceStatus();
        }
    };

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
			case MENU_ITEM_SWITCH_LIST:
				// Launch the node list activity.
				Intent myIntent = new Intent(BrowseActivity.this, SwitchActivity.class);
				startActivity(myIntent);
			return true;

			case MENU_ITEM_REDISCOVER:

				if( conn == null ) 
				{
					Log.w(TAG, "Cannot invoke - service not bound");
				} 
				else 
				{
					try 
					{
						hnodeService.resetAndEnumerate();
					} 
					catch( RemoteException ex ) 
					{
						Log.e( TAG, "DeadObjectException",ex );
					}
				}

			return true;
			
			case MENU_ITEM_INSERT:
				// DEBUG.  Add a fake item to test the UI.
				//Uri nodeUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
				ContentValues testVal = new ContentValues();
				testVal.put(Nodes.ENDPOINT, "test_interface");
				getContentResolver().insert(getIntent().getData(), testVal);

				// Launch activity to insert a new item
				//startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
				return true;
				
				
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) 
	{
		
		AdapterView.AdapterContextMenuInfo info;
		try 
		{
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} 
		catch (ClassCastException e) 
		{
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) 
		{
			// For some reason the requested item isn't available, do nothing
			return;
		}

		// Setup the menu header
		menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

		// Add a menu item to delete the note
		menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
	}
     
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		AdapterView.AdapterContextMenuInfo info;
		try 
		{
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} 
		catch (ClassCastException e) 
		{
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		switch (item.getItemId()) 
		{
			case MENU_ITEM_DELETE: 
			{
				// Delete the note that the context menu is for
				Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
				getContentResolver().delete(noteUri, null, null);
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
     
		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) 
		{
			// The caller is waiting for us to return a note selected by
			// the user.  The have clicked on one, so return it now.
			setResult(RESULT_OK, new Intent().setData(uri));
		} 
		else 
		{
			// Launch activity to view/edit the currently selected item
			//startActivity(new Intent(Intent.ACTION_EDIT, uri));
		}
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		
		if( conn != null ) 
		{
			unbindService( conn );	  
			conn = null;
			Log.d( TAG, "unbindService()" );
		} 
		else 
		{
			Log.w(TAG, "Cannot unbind - service not bound");
		}				
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		if( conn == null ) 
		{
			conn = new HNodeServiceConnection();
			Intent i = new Intent();
			i.setClassName( "org.mdns.browser", "org.hnode.HNodeService" );
			bindService( i, conn, Context.BIND_AUTO_CREATE);
			Log.d( TAG, "bindService()" );
		} 
		else 
		{
			Log.w(TAG, "Cannot bind - service already bound");
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
}


