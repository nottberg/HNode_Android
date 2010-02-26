// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode;

import java.util.HashMap;

import org.hnode.HNode.Nodes;

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

public class HNodeProvider extends ContentProvider {
    private static final String TAG = "HNodeProvider";

    private static final String DATABASE_NAME = "hnode.db";
    private static final int DATABASE_VERSION = 3;
    private static final String HNODE_TABLE_NAME = "nodes";

    private static HashMap<String, String> sNodesProjectionMap;

    private static final int NODES = 1;
    private static final int NODE_ID = 2;

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
        	
            db.execSQL("CREATE TABLE " + HNODE_TABLE_NAME + " ("
                    + Nodes._ID + " INTEGER PRIMARY KEY,"
                    + Nodes.ENDPOINT + " TEXT,"
                    + Nodes.REVISION + " TEXT,"
                    + Nodes.UID + " TEXT,"
                    + Nodes.NICKNAME + " TEXT,"
                    + Nodes.SSID + " TEXT,"
                    + Nodes.NODEURI + " TEXT,"
                    + Nodes.EPINDEX + " INTEGER,"
                    + Nodes.UPDATED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + HNODE_TABLE_NAME);
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
	        	case NODES:
	            return Nodes.CONTENT_TYPE;

	        	case NODE_ID:
	            return Nodes.CONTENT_ITEM_TYPE;

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
        if (sUriMatcher.match(uri) != NODES) 
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
        if (values.containsKey(HNode.Nodes.UPDATED_DATE) == false) 
        {
            values.put(HNode.Nodes.UPDATED_DATE, now);
        }

        if (values.containsKey(HNode.Nodes.ENDPOINT) == false) 
        {
            values.put(HNode.Nodes.ENDPOINT, "");
        }

        if (values.containsKey(HNode.Nodes.REVISION) == false) 
        {
            values.put(HNode.Nodes.REVISION, "");
        }

        if (values.containsKey(HNode.Nodes.UID) == false) 
        {
            values.put(HNode.Nodes.UID, "");
        }

        if (values.containsKey(HNode.Nodes.NICKNAME) == false) 
        {
            values.put(HNode.Nodes.NICKNAME, "");
        }

        if (values.containsKey(HNode.Nodes.SSID) == false) 
        {
            values.put(HNode.Nodes.SSID, "");
        }

        if (values.containsKey(HNode.Nodes.NODEURI) == false) 
        {
            values.put(HNode.Nodes.NODEURI, "");
        }

        if (values.containsKey(HNode.Nodes.EPINDEX) == false) 
        {
            values.put(HNode.Nodes.EPINDEX, 0);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(HNODE_TABLE_NAME, Nodes.ENDPOINT, values);

        Log.w(TAG, String.format("Inserting data to nodes: %d", rowId));

        if (rowId > 0) {
            Uri nodeUri = ContentUris.withAppendedId(HNode.Nodes.CONTENT_URI, rowId);
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
        	case NODES:
            qb.setTables(HNODE_TABLE_NAME);
            qb.setProjectionMap(sNodesProjectionMap);
            break;

        	case NODE_ID:
            qb.setTables(HNODE_TABLE_NAME);
            qb.setProjectionMap(sNodesProjectionMap);
            qb.appendWhere(Nodes._ID + "=" + uri.getPathSegments().get(1));
            break;

        	default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = HNode.Nodes.DEFAULT_SORT_ORDER;
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
        	case NODES:
            count = db.update(HNODE_TABLE_NAME, values, where, whereArgs);
            break;

        	case NODE_ID:
            String nodeId = uri.getPathSegments().get(1);
            count = db.update(HNODE_TABLE_NAME, values, Nodes._ID + "=" + nodeId
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
        	case NODES:
            count = db.delete(HNODE_TABLE_NAME, where, whereArgs);
            break;

        	case NODE_ID:
            String nodeId = uri.getPathSegments().get(1);
            count = db.delete(HNODE_TABLE_NAME, Nodes._ID + "=" + nodeId
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
        sUriMatcher.addURI(HNode.EPAUTHORITY, "nodes", NODES);
        sUriMatcher.addURI(HNode.EPAUTHORITY, "nodes/#", NODE_ID);

        sNodesProjectionMap = new HashMap<String, String>();
        sNodesProjectionMap.put(Nodes._ID, Nodes._ID);
        sNodesProjectionMap.put(Nodes.ENDPOINT, Nodes.ENDPOINT);
        sNodesProjectionMap.put(Nodes.REVISION, Nodes.REVISION);
        sNodesProjectionMap.put(Nodes.UID, Nodes.UID);
        sNodesProjectionMap.put(Nodes.SSID, Nodes.SSID);
        sNodesProjectionMap.put(Nodes.NICKNAME, Nodes.NICKNAME);
        sNodesProjectionMap.put(Nodes.NODEURI, Nodes.NODEURI);
        sNodesProjectionMap.put(Nodes.EPINDEX, Nodes.EPINDEX);
        sNodesProjectionMap.put(Nodes.UPDATED_DATE, Nodes.UPDATED_DATE);

    }
}

/*
//
// Provides access to a database of notes. Each note has a title, the note
// itself, a creation date and a modified data.
//
public class NotePadProvider extends ContentProvider {

    private static final String TAG = "NotePadProvider";

    private static final String DATABASE_NAME = "note_pad.db";
    private static final int DATABASE_VERSION = 2;
    private static final String NOTES_TABLE_NAME = "notes";

    private static HashMap<String, String> sNotesProjectionMap;

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;

    private static final UriMatcher sUriMatcher;

    //
    // This class helps open, create, and upgrade the database file.
    //
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NOTES_TABLE_NAME + " ("
                    + Notes._ID + " INTEGER PRIMARY KEY,"
                    + Notes.TITLE + " TEXT,"
                    + Notes.NOTE + " TEXT,"
                    + Notes.CREATED_DATE + " INTEGER,"
                    + Notes.MODIFIED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case NOTES:
            qb.setTables(NOTES_TABLE_NAME);
            qb.setProjectionMap(sNotesProjectionMap);
            break;

        case NOTE_ID:
            qb.setTables(NOTES_TABLE_NAME);
            qb.setProjectionMap(sNotesProjectionMap);
            qb.appendWhere(Notes._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
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

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            return Notes.CONTENT_TYPE;

        case NOTE_ID:
            return Notes.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(NotePad.Notes.CREATED_DATE) == false) {
            values.put(NotePad.Notes.CREATED_DATE, now);
        }

        if (values.containsKey(NotePad.Notes.MODIFIED_DATE) == false) {
            values.put(NotePad.Notes.MODIFIED_DATE, now);
        }

        if (values.containsKey(NotePad.Notes.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(NotePad.Notes.TITLE, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(NotePad.Notes.NOTE) == false) {
            values.put(NotePad.Notes.NOTE, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(NOTES_TABLE_NAME, Notes.NOTE, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            count = db.delete(NOTES_TABLE_NAME, where, whereArgs);
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(NOTES_TABLE_NAME, Notes._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            count = db.update(NOTES_TABLE_NAME, values, where, whereArgs);
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(NOTES_TABLE_NAME, values, Notes._ID + "=" + noteId
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
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);

        sNotesProjectionMap = new HashMap<String, String>();
        sNotesProjectionMap.put(Notes._ID, Notes._ID);
        sNotesProjectionMap.put(Notes.TITLE, Notes.TITLE);
        sNotesProjectionMap.put(Notes.NOTE, Notes.NOTE);
        sNotesProjectionMap.put(Notes.CREATED_DATE, Notes.CREATED_DATE);
        sNotesProjectionMap.put(Notes.MODIFIED_DATE, Notes.MODIFIED_DATE);
    }
}
*/
