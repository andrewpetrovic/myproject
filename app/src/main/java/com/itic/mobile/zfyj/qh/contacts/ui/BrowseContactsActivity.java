package com.itic.mobile.zfyj.qh.contacts.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.droideep.indexbar.IndexBar;
import com.itic.mobile.base.ui.widget.CollectionView;
import com.itic.mobile.base.ui.widget.DrawShadowFrameLayout;
import com.itic.mobile.util.app.PrefUtils;
import com.itic.mobile.util.ui.UIUtils;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.base.BaseActivityImpl;
import com.itic.mobile.zfyj.qh.contacts.model.ContactTypeMetadata;
import com.itic.mobile.zfyj.qh.provider.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BrowseContactsActivity
 *
 * @author Andrea Ji
 */
public class BrowseContactsActivity extends BaseActivityImpl implements ContactsFragment.Callbacks {

    private static final String TAG = "ContactsActivity";

    private static final int MODE_CONTACTS = 0;

    private static final String STATE_FILTER_0 = "STATE_FILTER_0";
    private static final String STATE_FILTER_1 = "STATE_FILTER_1";
    private static final String STATE_FILTER_2 = "STATE_FILTER_2";

    public static final String EXTRA_FILTER_TAG = "com.itic.mobile.zfyj.qh.extra.FILTER_TAG";

    private int mMode = MODE_CONTACTS;

    private ContactsFragment mContactsFragment = null;
    private boolean mSpinnerConfigured = false;
    private SpinnerAdapter mSpinnerAdapter = new SpinnerAdapter(true);
    private ContactTypeMetadata mTypeMetadata;
    private String[] mFilterTags = {"", "", ""};
    private String[] mFilterTagsToRestore = {null, null, null};
    private int mHeaderColor;
    private DrawShadowFrameLayout mDrawShadowFrameLayout;
    private IndexBar mIndexBar;
    private TextView mPreviewText;
//    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        overridePendingTransition(0, 0);
        Toolbar toolbar = getActionBarToolbar();

        if (mMode == MODE_CONTACTS) {
            toolbar.setTitle(null);
        }

        if (savedInstanceState != null) {
            mFilterTagsToRestore[0] = mFilterTags[0] = savedInstanceState.getString(STATE_FILTER_0);
            mFilterTagsToRestore[1] = mFilterTags[1] = savedInstanceState.getString(STATE_FILTER_1);
            mFilterTagsToRestore[2] = mFilterTags[2] = savedInstanceState.getString(STATE_FILTER_2);
        } else if (getIntent() != null && getIntent().hasExtra(EXTRA_FILTER_TAG)) {
            mFilterTagsToRestore[0] = getIntent().getStringExtra(EXTRA_FILTER_TAG);
        }

        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        mIndexBar = (IndexBar) findViewById(R.id.index_bar);
        mPreviewText = (TextView) findViewById(R.id.previewText);
        mPreviewText.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkShowStaleDataButterBar();
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BrowseContactsActivity.this);
//        sp.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
//        if (!PrefUtils.isDataBootstrapDone(getApplicationContext())){
//            pd = ProgressDialog.show(BrowseContactsActivity.this, null, getResources().getString(R.string.dialog_login_process));
//        }
    }

//    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener =
//            new SharedPreferences.OnSharedPreferenceChangeListener() {
//                @Override
//                public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
//                    if (PrefUtils.PREF_DATA_BOOTSTRAP_DONE.equals(key)) {
//                        if(PrefUtils.isDataBootstrapDone(getApplicationContext())){
//                            pd.dismiss();
//                        }
//                    }
//                }
//            };

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
//        if (mContactsFragment != null) {
//            return mContactsFragment.canSwipeRefreshChildScrollUp();
//        }
//        return super.canSwipeRefreshChildScrollUp();
        //禁用下拉刷新
        return true;
    }

    private void checkShowStaleDataButterBar() {
        updateFragContentTopClearance();
    }

    private void updateFragContentTopClearance() {
        ContactsFragment frag = mContactsFragment = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.contacts_fragment);
        if (frag == null) {
            return;
        }
        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        int gridPadding = getResources().getDimensionPixelSize(R.dimen.explore_grid_padding);
        setProgressBarTopWhenActionBarShown(actionBarClearance);
        mDrawShadowFrameLayout.setShadowTopOffset(actionBarClearance);
        //设置fragment 的padding top
        frag.setContentTopClearance(actionBarClearance + gridPadding);
    }

    //一级界面必须覆盖这个方法，返回值为BaseActivityImpl中定义的与该acivity对应的NAVDRAWER_ITEM值
    @Override
    protected int getSelfNavDrawerItem() {
        return mMode == MODE_CONTACTS ? NAVDRAWER_ITEM_SHOW_CONTACTS : NAVDRAWER_ITEM_INVALID;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        CollectionView collectionView = (CollectionView) findViewById(R.id.collapseActionView);
        if (collectionView != null) {
            enableActionBarAutoHide(collectionView);
        }
        mContactsFragment = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.contacts_fragment);
        // 初始化mContactsFragment时先给一个空的intent，由mContactsFragments生成默认的查询url
        if (mContactsFragment != null && savedInstanceState == null) {
            Bundle args = intentToFragmentArguments(getIntent());
            mContactsFragment.reloadFromArguments(args);
        }
        registerHideableHeaderView(findViewById(R.id.headerbar));
    }

    @Override
    public void onTagMetadataLoaded(ContactTypeMetadata metadata) {
        mTypeMetadata = metadata;
        if (mSpinnerConfigured) {
            // we need to reconfigure the spinner, so we need to remember our current filter
            // and try to restore it after we set up the spinner again.
            mSpinnerConfigured = false;
            mFilterTagsToRestore[0] = mFilterTags[0];
            mFilterTagsToRestore[1] = mFilterTags[1];
            mFilterTagsToRestore[2] = mFilterTags[2];
        }
        trySetUpActionBarSpinner();
    }

    @Override
    public void onContactSelected(String contactId, View clickedView) {
        getLUtils().startActivityWithTransition(new Intent(Intent.ACTION_VIEW, Contract.Contacts.buildContactUri(contactId)),
                clickedView,
                ContactDetailActivity.TRANSITION_NAME_PHOTO);
    }

    @Override
    public void onUpdateInventory(final Map<String, Integer> sections) {
        mIndexBar.setSections(alphabets());
        mIndexBar.setIndexBarFilter(new IndexBar.IIndexBarFilter() {
            @Override
            public void filterList(float sideIndex, int position, String previewText) {
                Integer selection = sections.get(previewText);
                if (selection != null) {
                    mPreviewText.setVisibility(View.VISIBLE);
                    mPreviewText.setText(previewText);
                    mContactsFragment.setSelection(selection);
                } else {
                    mPreviewText.setVisibility(View.GONE);
                }
            }
        });
    }

    private String[] alphabets() {
        StringBuffer sb = new StringBuffer();
        sb.append("#");
        int length = 26;
        char c = 'A';
        for (int i = 0; i < length; i++) {
            sb.append(String.valueOf(c++));
        }
        char[] s = sb.toString().toCharArray();
        String[] alphabets = new String[s.length];
        for (int i = 0; i < s.length; i++) {
            alphabets[i] = String.valueOf(s[i]);
        }

        return alphabets;
    }

    private void trySetUpActionBarSpinner() {
        Toolbar toolbar = getActionBarToolbar();
        if (mSpinnerConfigured || mTypeMetadata == null || toolbar == null) {
            Log.i(TAG, "Not configuring Action Bar spinner.");
            return;
        }
        Log.i(TAG, "Configuring Action Bar spinner.");
        mSpinnerConfigured = true;
        mSpinnerAdapter.clear();

        mSpinnerAdapter.addItem("", "单位通讯录", false, "");

        int itemToSelect = -1;
        if (mTypeMetadata.getTypeList().size() != 0) {
            mSpinnerAdapter.clear();
            for (ContactTypeMetadata.Type type : mTypeMetadata.getTypeList()) {
                Log.d(TAG, "Adding item to spinner: " + type.getID() + " --> " + type.getName());
                mSpinnerAdapter.addItem(type.getID(), type.getName(), true, type.getColor());
                if (!TextUtils.isEmpty(mFilterTagsToRestore[0]) && type.getID().equals(mFilterTagsToRestore[0])) {
                    mFilterTagsToRestore[0] = null;
                    itemToSelect = mSpinnerAdapter.getCount() - 1;
                }
            }
        }

        mFilterTagsToRestore[0] = null;

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.actionbar_spinner,
                toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);
        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onTopLevelTagSelected(mSpinnerAdapter.getTag(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (itemToSelect >= 0) {
            spinner.setSelection(itemToSelect);
        }
    }


    private void onTopLevelTagSelected(String tag) {
        ContactsFragment frag = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.contacts_fragment);
        if (frag == null) {
            return;
        }
        if (tag.equals(mFilterTags[0])) {
            // nothing to do
            return;
        }
        mFilterTags[0] = tag;
        // Reset secondary filters
        for (int i = 1; i < mFilterTags.length; i++) {
            mFilterTags[i] = "";
        }
        updateHeaderColor();
        reloadFromFilters();
    }

    private void updateHeaderColor() {
        mHeaderColor = 0;
        for (String tag : mFilterTags) {
            if (tag != null) {
                ContactTypeMetadata.Type typeObj = mTypeMetadata.getType(tag);
                if (typeObj != null) {
                    mHeaderColor = Color.parseColor(typeObj.getColor());
                }
            }
        }
        findViewById(R.id.headerbar).setBackgroundColor(mHeaderColor == 0 ? getResources().getColor(R.color.theme_primary) : mHeaderColor);
        setNormalStatusBarColor(mHeaderColor == 0 ?
                getThemedStatusBarColor() : UIUtils.scaleColor(mHeaderColor, 0.8f, false));
    }

    private void reloadFromFilters() {
        ContactsFragment frag = (ContactsFragment) getSupportFragmentManager().findFragmentById(
                R.id.contacts_fragment);
        if (frag == null) {
            return;
        }

        Bundle args = intentToFragmentArguments(
                new Intent(Intent.ACTION_VIEW, Contract.Contacts.buildTypeFilterUri(mFilterTags))
                        .putExtra(ContactsFragment.EXTRA_NO_TRACK_BRANDING, mHeaderColor != 0));
        frag.reloadFromArguments(args);
        frag.animateReload();
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_contacts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(STATE_FILTER_0, mFilterTags[0]);
        outState.putString(STATE_FILTER_1, mFilterTags[1]);
        outState.putString(STATE_FILTER_2, mFilterTags[2]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private class SpinnerItem {
        boolean isHeader;
        String tag, title;
        boolean indented;
        String color;

        SpinnerItem(boolean isHeader, String tag, String title, boolean indented, String color) {
            this.isHeader = isHeader;
            this.tag = tag;
            this.title = title;
            this.indented = indented;
            this.color = color;
        }
    }

    private class SpinnerAdapter extends BaseAdapter {
        private int mDotSize;
        private boolean mTopLevel;

        private SpinnerAdapter(boolean topLevel) {
            this.mTopLevel = topLevel;
        }

        private ArrayList<SpinnerItem> mItems = new ArrayList<SpinnerItem>();

        public void clear() {
            mItems.clear();
        }

        public void addItem(String tag, String title, boolean indented, String color) {
            mItems.add(new SpinnerItem(false, tag, title, indented, color));
        }

        public void addHeader(String title) {
            mItems.add(new SpinnerItem(true, "", title, false, ""));
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private boolean isHeader(int position) {
            return position >= 0 && position < mItems.size()
                    && mItems.get(position).isHeader;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || !convertView.getTag().toString().equals("DROPDOWN")) {
                convertView = getLayoutInflater().inflate(R.layout.explore_spinner_item_dropdown,
                        parent, false);
                convertView.setTag("DROPDOWN");
            }
            TextView headerTextView = (TextView) convertView.findViewById(R.id.header_text);
            View dividerView = convertView.findViewById(R.id.divider_view);
            TextView normalTextView = (TextView) convertView.findViewById(android.R.id.text1);

            if (isHeader(position)) {
                headerTextView.setText(getTitle(position));
                headerTextView.setVisibility(View.VISIBLE);
                normalTextView.setVisibility(View.GONE);
                dividerView.setVisibility(View.VISIBLE);
            } else {
                headerTextView.setVisibility(View.GONE);
                normalTextView.setVisibility(View.VISIBLE);
                dividerView.setVisibility(View.GONE);

                setUpNormalDropdownView(position, normalTextView);
            }

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {
                convertView = getLayoutInflater().inflate(mTopLevel
                                ? R.layout.explore_spinner_item_actionbar
                                : R.layout.explore_spinner_item,
                        parent, false);
                convertView.setTag("NON_DROPDOWN");
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(getTitle(position));
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        private void setUpNormalDropdownView(int position, TextView textView) {
            textView.setText(getTitle(position));
            ShapeDrawable colorDrawable = (ShapeDrawable) textView.getCompoundDrawables()[2];
            String color = getColor(position);
            if (color.equals("")) {
                if (colorDrawable != null) {
                    textView.setCompoundDrawables(null, null, null, null);
                } else {
                    if (mDotSize == 0) {
                        mDotSize = getResources().getDimensionPixelSize(
                                R.dimen.tag_color_dot_size);
                    }
                    if (colorDrawable == null) {
                        colorDrawable = new ShapeDrawable(new OvalShape());
                        colorDrawable.setIntrinsicWidth(mDotSize);
                        colorDrawable.setIntrinsicHeight(mDotSize);
                        colorDrawable.getPaint().setStyle(Paint.Style.FILL);
                        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, colorDrawable, null);
                    }
                    colorDrawable.getPaint().setColor(Color.parseColor(color));
                }
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return !isHeader(position);
        }

        private String getTitle(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position).title : "";
        }

        private String getTag(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position).tag : "";
        }

        private String getColor(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position).color : "";
        }
    }
}
