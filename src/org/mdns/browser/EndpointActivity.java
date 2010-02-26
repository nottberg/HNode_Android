// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.mdns.browser;

import org.hnode.HNode.Nodes;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;


public class EndpointActivity extends Activity 
{
	public final static String TAG = EndpointActivity.class.toString();

	//
	// The columns we are interested in from the database
	//
	private static final String[] PROJECTION = new String[] {
	     Nodes._ID,         // 0
	     Nodes.ENDPOINT,    // 1
	     Nodes.REVISION,    // 2
	     Nodes.UID,         // 3
	     Nodes.NICKNAME,    // 4
	     Nodes.SSID,        // 5
	     Nodes.NODEURI,     // 6
	     Nodes.EPINDEX,     // 7
	     Nodes.UPDATED_DATE // 8
	};

    /**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Inflate our UI from its XML layout description.
        setContentView(R.layout.endpoint_info);

		// Perform a managed query. The Activity will handle closing and requerying the cursor
		// when needed.
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, Nodes.DEFAULT_SORT_ORDER);

        cursor.moveToFirst();

	    // Nodes._ID,         // 0
	    // Nodes.ENDPOINT,    // 1
	    // Nodes.REVISION,    // 2
	    // Nodes.UID,         // 3
	    // Nodes.NICKNAME,    // 4
	    // Nodes.SSID,        // 5
	    // Nodes.NODEURI,     // 6
	    // Nodes.EPINDEX,     // 7
	    // Nodes.UPDATED_DATE // 8

        // Find the text editor view inside the layout, because we to
        // set it. text2 URI
        TextView tmpView = (TextView) findViewById(android.R.id.text2);        
        tmpView.setText(cursor.getString(6));
        
        // text4 Mime Type
        tmpView = (TextView) findViewById(R.id.text4);        
        tmpView.setText(cursor.getString(1));

        // text6 UID
        tmpView = (TextView) findViewById(R.id.text6);        
        tmpView.setText(cursor.getString(3));
        
        // text8 NickName
        tmpView = (TextView) findViewById(R.id.text8);        
        tmpView.setText(cursor.getString(4));
        
        // text10 SSID
        tmpView = (TextView) findViewById(R.id.text10);        
        tmpView.setText(cursor.getString(5));
        
        // text12 REVISION
        tmpView = (TextView) findViewById(R.id.text12);        
        tmpView.setText(cursor.getString(2));
        
        // text14 LAST UPDATE
        tmpView = (TextView) findViewById(R.id.text14);
        Time tmpTime = new Time();
        Log.w(TAG, String.format("%d", cursor.getLong(8)));
        tmpTime.set(cursor.getLong(8));
        tmpView.setText(tmpTime.format("%m/%d/%Y  %H:%M:%S"));
        
	}
	
}
