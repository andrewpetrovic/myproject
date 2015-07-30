package com.itic.mobile.zfyj.qh.contacts.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.IntentCompat;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.base.BaseActivityImpl;
import com.itic.mobile.zfyj.qh.contacts.model.ContactTypeMetadata;
import com.itic.mobile.zfyj.qh.provider.Contract;

import java.util.Map;

import static com.itic.mobile.util.logcat.LogUtils.LOGD;
import static com.itic.mobile.util.logcat.LogUtils.LOGW;
import static com.itic.mobile.util.logcat.LogUtils.makeLogTag;

public class SearchActivity extends BaseActivityImpl implements ContactsFragment.Callbacks{
    private static final String TAG = makeLogTag("SearchActivity");
    ContactsFragment mContactsFragment = null;
    private String mQuery;
    SearchView mSearchView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setTitle(R.string.title_search);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUpToFromChild(SearchActivity.this,
                        IntentCompat.makeMainActivity(new ComponentName(SearchActivity.this,
                                BrowseContactsActivity.class)));
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mContactsFragment = (ContactsFragment) fm.findFragmentById(R.id.fragment_container);

        String query = getIntent().getStringExtra(SearchManager.QUERY);
        query = query == null ? "" : query;
        mQuery = query;

        if (mContactsFragment == null) {
            mContactsFragment = new ContactsFragment();
            Bundle args = intentToFragmentArguments(
                    new Intent(Intent.ACTION_VIEW, Contract.Contacts.buildSearchUri(query)));
            mContactsFragment.setArguments(args);
            fm.beginTransaction().add(R.id.fragment_container, mContactsFragment).commit();
        }

        if (mSearchView != null) {
            mSearchView.setQuery(query, false);
        }

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LOGD(TAG, "SearchActivity.onNewIntent: " + intent);
        setIntent(intent);
        String query = intent.getStringExtra(SearchManager.QUERY);
        Bundle args = intentToFragmentArguments(
                new Intent(Intent.ACTION_VIEW, Contract.Contacts.buildSearchUri(query)));
        LOGD(TAG, "onNewIntent() now reloading sessions fragment with args: " + args);
        mContactsFragment.reloadFromArguments(args);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null){
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView view = (SearchView) searchItem.getActionView();
            mSearchView = view;
            if (view == null) {
                LOGW(TAG, "Could not set up search view, view is null.");
            }else {
                view.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                view.setIconified(false);
                view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        view.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        if (null != mContactsFragment) {
                            mContactsFragment.requestQueryUpdate(s);
                        }
                        return true;
                    }
                });
                view.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        finish();
                        return false;
                    }
                });
            }
            if (!TextUtils.isEmpty(mQuery)) {
                view.setQuery(mQuery, false);
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTagMetadataLoaded(ContactTypeMetadata metadata) { }

    @Override
    public void onContactSelected(String contactId, View clickedView) {
        getLUtils().startActivityWithTransition(
                new Intent(Intent.ACTION_VIEW,
                        Contract.Contacts.buildContactUri(contactId)),
                clickedView,
                ContactDetailActivity.TRANSITION_NAME_PHOTO);
    }

    @Override
    public void onUpdateInventory(Map<String, Integer> sections) {

    }
}
