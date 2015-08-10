package com.example.andrea.demodata;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.itic.mobile.util.observer.ThrottledContentObserver;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView lv;
//    private ThrottledContentObserver mContactsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mContactsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
//            @Override
//            public void onThrottledContentObserverFired() {
//
//            }
//        });
//        getContentResolver().registerContentObserver(Contract.Contacts.CONTENT_URI, true, mContactsObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        getContentResolver().unregisterContentObserver(mContactsObserver);
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
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
