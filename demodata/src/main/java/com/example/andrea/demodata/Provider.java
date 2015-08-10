package com.example.andrea.demodata;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.itic.mobile.util.database.SelectionBuilder;

public class Provider extends ContentProvider{

    private static final int CONTACTS = 400;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private Database mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "contacts", CONTACTS);
        return matcher;
    }

    /**
     * 建库/表
     * @return
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new Database(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * 查
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder = buildSelection(uri, match);
        Cursor cursor = builder.where(selection,selectionArgs).query(db,projection,sortOrder);
        Context context = getContext();
        if (null != context){
            cursor.setNotificationUri(context.getContentResolver(),uri);
        }
        return cursor;
    }

    private SelectionBuilder buildSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case CONTACTS:
                return builder.table(Database.Tables.CONTACTS);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private SelectionBuilder buildSelection(Uri uri) {
        final int match = sUriMatcher.match(uri);
        return buildSelection(uri,match);
    }

    /**
     * 增
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match){
            case CONTACTS:{
                db.insertOrThrow(Database.Tables.CONTACTS, null, values);
                notifyChange(uri);
                return Contract.Contacts.buildContactUri(values.getAsString(Contract.Contacts.CONTACT_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }
    /**
     * 删
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uri == Contract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    private void notifyChange(Uri uri) {
        Context context = getContext();
        context.getContentResolver().notifyChange(uri, null);
    }

    private void deleteDatabase() {
        // 等待content provider option 完成，然后删除数据库
        mOpenHelper.close();
        Context context = getContext();
        Database.delectDatabase(context);
        mOpenHelper = new Database(getContext());
    }
    /**
     * 改
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder = buildSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }
}
