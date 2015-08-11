package com.example.andrea.demodata;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itic.mobile.util.app.PrefUtils;
import com.itic.mobile.util.database.JSONHandler;
import com.itic.mobile.util.datetime.DateTimeUtils;
import com.itic.mobile.util.observer.ThrottledContentObserver;
import com.itic.mobile.util.ui.TextViewUtils;

import java.io.IOException;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MainActivity";

    private ListView lv;
    private ContactAdapter adapter;
    private Cursor mCursor;
    private ThrottledContentObserver mContactsObserver;
    Thread mDataBootstrapThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.listView);
        adapter = new ContactAdapter();
        lv.setAdapter(adapter);
        getLoaderManager().initLoader(ContactsQuery.NORMAL_TOKEN,null,MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!PrefUtils.isDataBootstrapDone(this) && mDataBootstrapThread == null){
            performDataBootstrap();
        }
    }

    private void performDataBootstrap() {
        mDataBootstrapThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String bootstrapJson = JSONHandler.parseResource(getApplicationContext(), R.raw.bootstrap_data);
                            AppDataHandlerImpl dataHandler = new AppDataHandlerImpl(getApplicationContext());
                            dataHandler.applyConferenceData(new String[]{bootstrapJson}, Long.toString(DateTimeUtils.stringToDateTime("2015-02-09 00:00:00")), false);
                            Log.i(TAG, "End of bootstrap -- successful. Marking boostrap as done.");
                            PrefUtils.markDataBootstrapDone(getApplicationContext());
                        } catch (IOException e) {
                            PrefUtils.markDataBootstrapDone(getApplicationContext());
                        }
                        mDataBootstrapThread = null;
                    }
                }
        );
        mDataBootstrapThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContactsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                getLoaderManager().restartLoader(ContactsQuery.NORMAL_TOKEN,null,MainActivity.this);
            }
        });
        getContentResolver().registerContentObserver(Contract.Contacts.CONTENT_URI, true, mContactsObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mContactsObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;
        if (id == ContactsQuery.NORMAL_TOKEN){
            loader = new CursorLoader(this,Contract.Contacts.CONTENT_URI,ContactsQuery.NORMAL_PROJECTION,null,null,null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int token = loader.getId();
        if (token == ContactsQuery.NORMAL_TOKEN){
            if (mCursor != null && mCursor != cursor){
                mCursor.close();
            }
            mCursor = cursor;
            mCursor.moveToPosition(-1);
            int i = mCursor.getCount();
            adapter.changeCursor(mCursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class ContactAdapter extends CursorAdapter{

        public ContactAdapter(){
            super(getApplicationContext(),null,false);
        }

        @Override
        public Contact getItem(int position) {
            if (mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }
            final String xm = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.CONTACT_NAME));
            final String zw = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.POST));
            final String szdw = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.ORG_NAME));

            Contact contact = new Contact();
            contact.contact_name = xm;
            contact.org_name = szdw;
            contact.post = zw;

            return contact;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater mInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return mInflater.inflate(R.layout.list_item_contact, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Holder holder = getHolder(view);
            holder.xmView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.CONTACT_NAME))));
            holder.szdwView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.ORG_NAME))));
            holder.zwView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.POST))));
        }

        private Holder getHolder(final View view){
            Holder holder = (Holder)view.getTag();
            if (holder == null){
                holder = new Holder(view);
                view.setTag(holder);
            }
            return holder;
        }
    }

    private static class Holder {
        public TextView xmView;
        public TextView zwView;
        public TextView szdwView;

        public Holder(View view) {
            xmView = (TextView) view.findViewById(R.id.xm);
            zwView = (TextView) view.findViewById(R.id.zw);
            szdwView = (TextView) view.findViewById(R.id.szdw);
        }
    }

    private interface ContactsQuery {
        int NORMAL_TOKEN = 0x1;

        String[] NORMAL_PROJECTION = {
                BaseColumns._ID,
                Contract.Contacts.CONTACT_NAME,
                Contract.Contacts.POST,
                Contract.Contacts.ORG_NAME,
        };
    }
}
