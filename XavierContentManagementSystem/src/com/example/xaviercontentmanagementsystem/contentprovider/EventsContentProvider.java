package com.example.xaviercontentmanagementsystem.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.xaviercontentmanagementsystem.database.*;

public class EventsContentProvider extends ContentProvider 
{
	private EventDatabaseHelper database;
	
	private static final int EVENT = 10;
	private static final int EVENT_ID = 20;
	
	private static final String AUTHORITY = "com.example.xaviercontentmanagementsystem.contentprovider";
	private static final String BASE_PATH = "xaviercontentmanagementsystem";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + "BASE_PATH");
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_PATH;
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static
	{
		sUriMatcher.addURI(AUTHORITY, BASE_PATH, EVENT);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", EVENT_ID);
	}
	
	@Override
	public boolean onCreate()
	{
		database = new EventDatabaseHelper(getContext());
		return false;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		checkColumns(projection);
		
		queryBuilder.setTables(EventTable.TABLE_EVENTS);
		
		int uriType = sUriMatcher.match(uri);
		
		switch(uriType)
		{
		case EVENT:
			break;
		case EVENT_ID:
			queryBuilder.appendWhere(EventTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		SQLiteDatabase sqldb = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(sqldb,projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}
	
	@Override
	public String getType(Uri uri)
	{
		return null;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqldb = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch(uriType)
		{
		case EVENT:
			rowsUpdated = sqldb.update(EventTable.TABLE_EVENTS, values, selection, selectionArgs);
			break;
		case EVENT_ID:
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection))
			{
				rowsUpdated = sqldb.update(EventTable.TABLE_EVENTS, values, EventTable.COLUMN_ID + "=" + id, null);
			}
			else
			{
				rowsUpdated = sqldb.update(EventTable.TABLE_EVENTS, values, EventTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	private void checkColumns(String[] projection)
	{
		String[] available = { EventTable.COLUMN_DESCRIPTION, EventTable.COLUMN_DUE_DATE, EventTable.COLUMN_FLAGS, EventTable.COLUMN_ID, EventTable.COLUMN_PRIORITY, EventTable.COLUMN_SUMMARY };
		if(projection != null)
		{
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			//Check if all columns which are requested are available
			if(!availableColumns.containsAll(requestedColumns))
			{
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
	
