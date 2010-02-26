// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.mdns.browser;

import org.hnode.HNodeBrowser;
import org.hnode.HNodeService;
import org.hnode.HNode.Nodes;
import org.hnode.HNode.Switch;
import org.hnode.util.HNodeAddress;
import org.hnode.util.SwitchControl;
import org.hnode.util.SwitchRecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SwitchActivity extends ListActivity 
{

	public final static String TAG = SwitchActivity.class.toString();
	
	//public final static String HOSTNAME = "droid1";

	// Menu item ids
	public static final int MENU_ITEM_ON  = Menu.FIRST;
	public static final int MENU_ITEM_OFF = Menu.FIRST + 1;
	public static final int MENU_ITEM_NODE_LIST = Menu.FIRST + 2;
	
	private Uri ActiveSwitchUri;

	//
	// The columns we are interested in from the database
	//
	private static final String[] PROJECTION = new String[] {
         Switch._ID, // 0
         Switch.NAME, // 1
         Switch.NODEURI, //2
         Switch.REMOTE_ID, // 3
	};

	// The index of the title column
	private static final int COLUMN_INDEX_TITLE = 1;

	private static final int DIALOG_SWITCH_ACTION_ID = 1;

	private static final int SWITCH_ACTION_ON  = 0;
	private static final int SWITCH_ACTION_OFF = 1;
	private static final int SWITCH_ACTION_INFO = 2;
	final CharSequence[] items = {"On", "Off", "Info"};



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
			intent.setData(Switch.CONTENT_URI);
		}

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);
     
		// Perform a managed query. The Activity will handle closing and requerying the cursor
		// when needed.
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, Nodes.DEFAULT_SORT_ORDER);

		// Used to map notes entries from the database to views
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.switchlist_item, cursor,
             new String[] { Switch.NAME, Switch.NODEURI }, new int[] { android.R.id.text1, android.R.id.text2 });
		setListAdapter(adapter);

		// Make sure the HNode/Switch discovery service is running.
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

		// This is our one standard application action -- inserting a
		// new note into the list.
		menu.add(0, MENU_ITEM_NODE_LIST, 0, R.string.menu_node_list)
					.setShortcut('3', 'n')
					.setIcon(android.R.drawable.ic_menu_more);

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
			case MENU_ITEM_NODE_LIST:
			// Launch the node list activity.
			Intent myIntent = new Intent(SwitchActivity.this, BrowseActivity.class);
			//	CurrentActivity.this.startActivity(myIntent);
			startActivity(myIntent);
			return true;

			case MENU_ITEM_OFF:
				// DEBUG.  Add a fake item to test the UI.
				//Uri nodeUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
				//ContentValues testVal = new ContentValues();
				//testVal.put(Nodes.ENDPOINT, "test_interface");
				//getContentResolver().insert(getIntent().getData(), testVal);

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
		menu.add(0, MENU_ITEM_ON, 0, R.string.menu_on);
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
			case MENU_ITEM_ON: 
			{
				// Delete the note that the context menu is for
				//Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
				//getContentResolver().delete(noteUri, null, null);
				return true;
			}
		}
		
		return false;
	}

	public Handler switchReqComplete = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			int i;
			Log.w(TAG, "SwitchReqComplete event -- On finished\n");
		}
	};


	protected void PerformSwitchAction(int Action)
	{
		// Perform a managed query. The Activity will handle closing and requerying the cursor
		// when needed.
		Cursor cursor = managedQuery(ActiveSwitchUri, PROJECTION, null, null, Switch.DEFAULT_SORT_ORDER);

		int SwUriIdx = cursor.getColumnIndexOrThrow(Switch.NODEURI);
		int SwIDIdx  = cursor.getColumnIndexOrThrow(Switch.REMOTE_ID);

		Log.w(TAG, String.format("Index: %d, %d", SwUriIdx, SwIDIdx));
		Log.w(TAG, String.format("t0: %d", cursor.getColumnCount()));
		Log.w(TAG, String.format("t1: %d", cursor.getCount()));

		Log.w(TAG, String.format("t2: %b", cursor.isFirst()));
		Log.w(TAG, String.format("t3: %b", cursor.isLast()));
		Log.w(TAG, String.format("t4: %b", cursor.isAfterLast()));
		Log.w(TAG, String.format("t5: %b", cursor.isBeforeFirst()));

		cursor.moveToFirst();
		Log.w(TAG, String.format("SwitchIndex: %d", cursor.getInt(SwIDIdx)));
		Log.w(TAG, String.format("URI: %s", cursor.getString(SwUriIdx)));
		
		
		HNodeAddress SwitchAddress = new HNodeAddress(cursor.getString(SwUriIdx));
		SwitchControl swctl = new SwitchControl(SwitchAddress, switchReqComplete, null);
		
		swctl.start();
		
		switch(Action)
		{
			case SWITCH_ACTION_ON:
				swctl.RequestSwitchOn(cursor.getInt(SwIDIdx));	
			break;
			
			case SWITCH_ACTION_OFF:
				swctl.RequestSwitchOff(cursor.getInt(SwIDIdx));	
			break;
			
		}
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
	
		ActiveSwitchUri = uri;
		
		Log.w(TAG, String.format("URI: %s", uri.toString()));

		showDialog(DIALOG_SWITCH_ACTION_ID);

		
		/*
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
		*/
	}
	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
	    Dialog dialog;
	    switch(id) {
	    case DIALOG_SWITCH_ACTION_ID:

	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle("Select an Action");
	    	builder.setItems(items, new DialogInterface.OnClickListener() 
	    	{
	    	    public void onClick(DialogInterface dialog, int item) 
	    	    {
					if( item == SWITCH_ACTION_INFO )
					{
						// Launch the node list activity.
						Intent myIntent = new Intent(SwitchActivity.this, SwitchDataActivity.class);
						myIntent.setData(ActiveSwitchUri);
						
						startActivity(myIntent);
					}
					else
					{
						SwitchActivity.this.PerformSwitchAction(item);
					}
	    	    }
	    	});
	    	dialog = builder.create();
	    	break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) 
	{
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog);
	}

}
