// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode;

import android.content.ContentProvider;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.HashMap;

import org.hnode.HNode.Switch;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class SwitchProvider extends ContentProvider {
    private static final String TAG = "SwitchProvider";

    private static final String DATABASE_NAME = "hnode_switch.db";
    private static final int DATABASE_VERSION = 3;
    private static final String SWITCH_TABLE_NAME = "switches";

    private static HashMap<String, String> sNodesProjectionMap;

    private static final int SWITCHES = 1;
    private static final int SWITCH_ID = 2;

    private static final UriMatcher sUriMatcher;

    //
    // This class helps open, create, and upgrade the database file.
    //
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	
            db.execSQL("CREATE TABLE " + SWITCH_TABLE_NAME + " ("
                    + Switch._ID + " INTEGER PRIMARY KEY,"
                    + Switch.REMOTE_ID + " INTEGER,"
                    + Switch.IDSTR + " TEXT,"
                    + Switch.NAME + " TEXT,"
                    + Switch.CAPABILITIES + " TEXT,"
                    + Switch.NODEURI + " TEXT,"
                    + Switch.UID + " TEXT,"
                    + Switch.SSID + " TEXT,"
                    + Switch.UPDATED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SWITCH_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;


	/**
	 * @see android.content.ContentProvider#getType(Uri)
	 */
	@Override
	public String getType(Uri uri) 
	{
	       switch (sUriMatcher.match(uri)) 
	       {
	        	case SWITCHES:
	            return Switch.CONTENT_TYPE;

	        	case SWITCH_ID:
	            return Switch.CONTENT_ITEM_TYPE;

	        	default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
	        }
	}

	/**
	 * @see android.content.ContentProvider#insert(Uri,ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) 
	{
        // Validate the requested uri
        if (sUriMatcher.match(uri) != SWITCHES) 
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) 
        {
            values = new ContentValues(initialValues);
        } 
        else 
        {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(HNode.Switch.UPDATED_DATE) == false) 
        {
            values.put(HNode.Switch.UPDATED_DATE, now);
        }

        if (values.containsKey(HNode.Switch.REMOTE_ID) == false) 
        {
            values.put(HNode.Switch.REMOTE_ID, "");
        }

        if (values.containsKey(HNode.Switch.IDSTR) == false) 
        {
            values.put(HNode.Switch.IDSTR, "");
        }

        if (values.containsKey(HNode.Switch.UID) == false) 
        {
            values.put(HNode.Switch.UID, "");
        }

        if (values.containsKey(HNode.Switch.NAME) == false) 
        {
            values.put(HNode.Switch.NAME, "");
        }

        if (values.containsKey(HNode.Switch.CAPABILITIES) == false) 
        {
            values.put(HNode.Switch.CAPABILITIES, "");
        }

        if (values.containsKey(HNode.Switch.SSID) == false) 
        {
            values.put(HNode.Switch.SSID, "");
        }

        if (values.containsKey(HNode.Switch.NODEURI) == false) 
        {
            values.put(HNode.Switch.NODEURI, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(SWITCH_TABLE_NAME, Switch.IDSTR, values);
        if (rowId > 0) {
            Uri nodeUri = ContentUris.withAppendedId(HNode.Switch.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(nodeUri, null);
            return nodeUri;
        }

        throw new SQLException("Failed to insert row into " + uri);	
    }

	/**
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() 
	{
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	/**
	 * @see android.content.ContentProvider#query(Uri,String[],String,String[],String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) 
	{
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) 
        {
        	case SWITCHES:
            qb.setTables(SWITCH_TABLE_NAME);
            qb.setProjectionMap(sNodesProjectionMap);
            break;

        	case SWITCH_ID:
            qb.setTables(SWITCH_TABLE_NAME);
            qb.setProjectionMap(sNodesProjectionMap);
            qb.appendWhere(Switch._ID + "=" + uri.getPathSegments().get(1));
            break;

        	default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = HNode.Switch.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;	
    }

	/**
	 * @see android.content.ContentProvider#update(Uri,ContentValues,String,String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) 
	{
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) 
        {
        	case SWITCHES:
            count = db.update(SWITCH_TABLE_NAME, values, where, whereArgs);
            break;

        	case SWITCH_ID:
            String nodeId = uri.getPathSegments().get(1);
            count = db.update(SWITCH_TABLE_NAME, values, Switch._ID + "=" + nodeId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        	default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	/**
	 * @see android.content.ContentProvider#delete(Uri,String,String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs)
	{
		Log.w(TAG, String.format("delete node URI: %s  where: %s", uri.toString(), where));

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) 
        {
        	case SWITCHES:
            count = db.delete(SWITCH_TABLE_NAME, where, whereArgs);
            break;

        	case SWITCH_ID:
            String nodeId = uri.getPathSegments().get(1);
            count = db.delete(SWITCH_TABLE_NAME, Switch._ID + "=" + nodeId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        	default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(HNode.SWAUTHORITY, "switches", SWITCHES);
        sUriMatcher.addURI(HNode.SWAUTHORITY, "switches/#", SWITCH_ID);

        sNodesProjectionMap = new HashMap<String, String>();
        sNodesProjectionMap.put(Switch._ID, Switch._ID);
        sNodesProjectionMap.put(Switch.REMOTE_ID, Switch.REMOTE_ID);
        sNodesProjectionMap.put(Switch.IDSTR, Switch.IDSTR);
        sNodesProjectionMap.put(Switch.UID, Switch.UID);
        sNodesProjectionMap.put(Switch.NODEURI, Switch.NODEURI);
        sNodesProjectionMap.put(Switch.NAME, Switch.NAME);
        sNodesProjectionMap.put(Switch.CAPABILITIES, Switch.CAPABILITIES);
        sNodesProjectionMap.put(Switch.SSID, Switch.SSID);
        sNodesProjectionMap.put(Switch.UPDATED_DATE, Switch.UPDATED_DATE);
    }
}

/*
public class SwitchProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri
			.parse("content://org.hnode.switchprovider");

	//
	 // @see android.content.ContentProvider#delete(Uri,String,String[])
	 //
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Put your code here
		return 0;
	}

	//
	// @see android.content.ContentProvider#getType(Uri)
	//
	@Override
	public String getType(Uri uri) {
		// TODO Put your code here
		return null;
	}

	//
	//  @see android.content.ContentProvider#insert(Uri,ContentValues)
	// 
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Put your code here
		return null;
	}

	//
	//  @see android.content.ContentProvider#onCreate()
	// 
	@Override
	public boolean onCreate() {
		// TODO Put your code here
		return false;
	}

	//
	// @see android.content.ContentProvider#query(Uri,String[],String,String[],String)
	//
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Put your code here
		return null;
	}

	//
	// @see android.content.ContentProvider#update(Uri,ContentValues,String,String[])
	//
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Put your code here
		return 0;
	}
}
*/