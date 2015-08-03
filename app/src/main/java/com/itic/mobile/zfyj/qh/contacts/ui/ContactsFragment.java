package com.itic.mobile.zfyj.qh.contacts.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.itic.mobile.base.ui.widget.CollectionView;
import com.itic.mobile.base.ui.widget.CollectionViewCallbacks;
import com.itic.mobile.util.observer.ThrottledContentObserver;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.base.AbstractBaseActivity;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.contacts.model.ContactTypeMetadata;
import com.itic.mobile.zfyj.qh.provider.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.itic.mobile.util.logcat.LogUtils.LOGD;
import static com.itic.mobile.util.logcat.LogUtils.LOGV;
import static com.itic.mobile.util.logcat.LogUtils.LOGW;

/**
 * ContactsFragment
 */
public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, CollectionViewCallbacks {

    private static final String TAG = "BrowseContactsFragment";
    public static final String EXTRA_NO_TRACK_BRANDING = "com.itic.mobile.zfyj.qh.extra.NO_TRACK_BRANDING";
    private static final String STATE_CONTACT_QUERY_TOKEN = "contact_query_token";
    private static final String STATE_ARGUMENTS = "arguments";
    private int mContactQueryToken;

    /**
     * The handler message for updating the search query.
     */
    private static final int MESSAGE_QUERY_UPDATE = 1;
    private static final int ANIM_DURATION = 250;
    private static final int QUERY_UPDATE_DELAY_MILLIS = 100;

    private boolean mWasPaused = false;

    private TextView mEmptyView;
    private View mLoadingView;
    private CollectionView mCollectionView;

    private Bundle mArguments;

    private Context mAppContext;

    private Uri mCurrentUri = null;

    private int mHeaderColor = 0; // 0 means not customized

    private static Callbacks sDummyCallbacks = new Callbacks() {
        /**
         * 方法不需要被实现
         * @param metadata
         */
        @Override
        public void onTagMetadataLoaded(ContactTypeMetadata metadata) {
        }

        @Override
        public void onContactSelected(String contactId, View clickedView) {

        }

        @Override
        public void onUpdateInventory(Map<String, Integer> sections) {

        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    private boolean mNoTrackBranding;

    private boolean mContactDataIsFullReload;
    private ContactTypeMetadata mContactTypeMetadata;
    private Cursor mCursor;
    private boolean mIsSearchCursor;
    private int mMaxDataIndexAnimated;
    private ThrottledContentObserver mTypesObserver;
    private ThrottledContentObserver mContactsObserver;


    public void setContentTopClearance(int topClearance) {
        mCollectionView.setContentTopClearance(topClearance);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_QUERY_UPDATE) {
                String query = (String) msg.obj;
                reloadFromArguments(AbstractBaseActivity.intentToFragmentArguments(
                        new Intent(Intent.ACTION_SEARCH, Contract.Contacts.buildSearchUri(query))));
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mContactQueryToken = savedInstanceState.getInt(STATE_CONTACT_QUERY_TOKEN);
            mArguments = savedInstanceState.getParcelable(STATE_ARGUMENTS);
            if (mArguments != null) {
                mCurrentUri = mArguments.getParcelable("_uri");
                mNoTrackBranding = mArguments.getBoolean(EXTRA_NO_TRACK_BRANDING);
            }
            if (mContactQueryToken > 0) {
                // 只有当config change才需要initloader
                // 其他, loader在 reloadFromArguments被调用时init.
                getLoaderManager().initLoader(mContactQueryToken, null, ContactsFragment.this);
            }
        }
        reloadTagMetadata();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAppContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_contacts, container, false);
        mEmptyView = (TextView) root.findViewById(R.id.empty_text);
        mLoadingView = root.findViewById(R.id.loading);
        mCollectionView = (CollectionView) root.findViewById(R.id.contacts_collection_view);
        progressDialog = new ProgressDialog(getActivity());
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new ClassCastException("Activity must implement fragment's callbacks.");
        }
        mAppContext = getActivity().getApplicationContext();
        mCallbacks = (Callbacks) activity;

        mContactsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                onContactsContentChanged();
            }
        });
        mTypesObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                onTypesContentChanged();
            }
        });

        activity.getContentResolver().registerContentObserver(Contract.ContactTypes.CONTENT_URI, true, mTypesObserver);
        activity.getContentResolver().registerContentObserver(Contract.Contacts.CONTENT_URI, true, mContactsObserver);
    }

    private void onContactsContentChanged() {
        LOGD(TAG, "ThrottledContentObserver fired (contacts). Content changed.");
        if (!isAdded()) {
            LOGD(TAG, "Ignoring ContentObserver event (Fragment not added).");
            return;
        }

        LOGD(TAG, "Requesting contacts cursor reload as a result of ContentObserver firing.");
        reloadContactData(false);
    }

    private void onTypesContentChanged() {
        LOGD(TAG, "ThrottledContentObserver fired (types). Content changed.");
        if (!isAdded()) {
            LOGD(TAG, "Ignoring ContentObserver event (Fragment not added).");
            return;
        }
        LOGD(TAG, "Requesting tags cursor reload as a result of ContentObserver firing.");
        reloadTagMetadata();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
        getActivity().getContentResolver().unregisterContentObserver(mContactsObserver);
        getActivity().getContentResolver().unregisterContentObserver(mTypesObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        mWasPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWasPaused) {
            mWasPaused = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CONTACT_QUERY_TOKEN, mContactQueryToken);
        outState.putParcelable(STATE_ARGUMENTS, mArguments);
    }

    /**
     * @param args
     */
    void reloadFromArguments(Bundle args) {
        if (args == null) {
            args = new Bundle();
        } else {
            args = (Bundle) args.clone();
        }

        mArguments = args;
        Log.i(TAG, "BrowseContactsFragment reload from arguments: " + mArguments);

        mCurrentUri = mArguments.getParcelable("_uri");
        if (mCurrentUri == null) {
            //如果uri为空，默认为单位通讯录URI
            Log.i(TAG, "BrowseContactsFragment did not get a URL, default to TYPE_DWTXL");
            String[] mFilterTags = {Config.ContactsTypes.CATEGORY_DWTXL, "", ""};
            args = AbstractBaseActivity.intentToFragmentArguments(
                    new Intent(Intent.ACTION_VIEW, Contract.Contacts.buildTypeFilterUri(mFilterTags)));
            reloadFromArguments(args);
        }
        mNoTrackBranding = mArguments.getBoolean(EXTRA_NO_TRACK_BRANDING);
        if (Contract.Contacts.isSearchUri(mCurrentUri)) {
            mContactQueryToken = ContactsQuery.SEARCH_TOKEN;
        } else {
            mContactQueryToken = ContactsQuery.NORMAL_TOKEN;
        }
        Log.d(TAG, "ContactsFragment reloading, uri=" + mCurrentUri);
        reloadContactData(true);
        if (mContactTypeMetadata == null) {
            reloadTagMetadata();
        }
    }

    private void reloadTagMetadata() {
        getLoaderManager().restartLoader(TAG_METADATA_TOKEN, null, ContactsFragment.this);
    }

    private void reloadContactData(boolean fullReload) {
        Log.d(TAG, "Reloading session data: " + (fullReload ? "FULL RELOAD" : "light refresh"));
        mContactDataIsFullReload = fullReload;
        getLoaderManager().restartLoader(mContactQueryToken, mArguments, ContactsFragment.this);
    }

    private boolean useExpandedMode() {
        if (mCurrentUri != null && Contract.Contacts.CONTENT_URI.equals(mCurrentUri)) {
            // If showing all contacts (landing page) do not use expanded mode,
            // show info as condensed as possible
            return false;
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        Log.d(TAG, "onCreateLoader, id=" + id + ", data=" + data);
        final Intent intent = AbstractBaseActivity.fragmentArgumentsToIntent(data);
        Uri contactsUri = intent.getData();
        Loader<Cursor> loader = null;
        if (id == TAG_METADATA_TOKEN) {
            Log.d(TAG, "Creating metadata loader");
            loader = ContactTypeMetadata.createCursorLoader(getActivity());
        } else if (id == ContactsQuery.NORMAL_TOKEN) {
            Log.d(TAG, "Creating contacts loader for " + contactsUri);
            loader = new CursorLoader(getActivity(), contactsUri, ContactsQuery.NORMAL_PROJECTION, null, null, Contract.Contacts.SORT_KEY + " ASC");
        } else if (id == ContactsQuery.SEARCH_TOKEN) {
            loader = new CursorLoader(getActivity(), contactsUri, ContactsQuery.SEARCH_PROJECTION, null, null, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }
        int token = loader.getId();
        Log.d(TAG, "Loader finished: " + (token == ContactsQuery.NORMAL_TOKEN ? "contacts" :
                token == ContactsQuery.SEARCH_TOKEN ? "search" : token == TAG_METADATA_TOKEN ? "tags" :
                        "unknown"));
        if (token == ContactsQuery.NORMAL_TOKEN || token == ContactsQuery.SEARCH_TOKEN) {
            if (mCursor != null && mCursor != cursor) {
                mCursor.close();
            }
            mCursor = cursor;
            mIsSearchCursor = token == ContactsQuery.SEARCH_TOKEN;
            Log.d(TAG, "Cursor has " + mCursor.getCount() + " items. Will now update list view.");
            updateCollectionView(token);
        } else if (token == TAG_METADATA_TOKEN) {
            mContactTypeMetadata = new ContactTypeMetadata(cursor);
            cursor.close();
            updateCollectionView(token);
            mCallbacks.onTagMetadataLoaded(mContactTypeMetadata);
        }
    }

    private void updateCollectionView(int token) {
        if (mCursor == null || mContactTypeMetadata == null) {
            Log.d(TAG, "updateCollectionView: not ready yet... " + (mCursor == null ? "no cursor." :
                    "no tag metadata."));
            // not ready!
            return;
        }
        Log.d(TAG, "ContactssFragment updating CollectionView... " + (mContactDataIsFullReload ?
                "(FULL RELOAD)" : "(light refresh)"));

        mCursor.moveToPosition(-1);
        int itemCount = mCursor.getCount();

        mMaxDataIndexAnimated = 0;

        CollectionView.Inventory inv;

        if (itemCount == 0) {
            showEmptyView();
            inv = new CollectionView.Inventory();
        } else {
            hideEmptyView();
            inv = prepareInventory(token);
        }

        Map<String, Integer> mSections = new HashMap<String, Integer>();

        int position = 0;
        for (int i = 0; i < inv.getGroups().size(); i++) {
            CollectionView.InventoryGroup group = inv.getGroups().get(i);
            String label = group.getHeaderLabel();
//            position += i;
            mSections.put(label, position);
            position += group.getRowCount();
        }

        Parcelable state = null;
        if (!mContactDataIsFullReload) {
            // it's not a full reload, so we want to keep scroll position, etc
            state = mCollectionView.onSaveInstanceState();
        }
        LOGD(TAG, "Updating CollectionView with inventory, # groups = " + inv.getGroupCount()
                + " total items = " + inv.getTotalItemCount());
        mCollectionView.setCollectionAdapter(this);
        mCallbacks.onUpdateInventory(mSections);
        mCollectionView.updateInventory(inv, mContactDataIsFullReload);
        if (state != null) {
            mCollectionView.onRestoreInstanceState(state);
        }
        mContactDataIsFullReload = false;
    }

    private CollectionView.Inventory prepareInventory(int token) {
        LOGD(TAG, "Preparing collection view inventory");
        CollectionView.Inventory inventory = new CollectionView.Inventory();
        HashMap<String, CollectionView.InventoryGroup> map = new HashMap<String, CollectionView.InventoryGroup>();
        ArrayList<CollectionView.InventoryGroup> list = new ArrayList<CollectionView.InventoryGroup>();

        //cursor 位置和dataIndex位置保持一致
        mCursor.moveToPosition(-1);
        int dataIndex = -1;

        //如果Contacts没有分组，只需要建立一个组，如果按照字母分组，则最多需要建立26+1个分组
        int nextGroupId = 1000;
        final boolean expandedMode = useExpandedMode();
        final int displayCols = getResources().getInteger(expandedMode ?
                R.integer.explore_2nd_level_grid_columns : R.integer.explore_1st_level_grid_columns);
        LOGD(TAG, "Using " + displayCols + " columns.");

        while (mCursor.moveToNext()) {
            ++dataIndex;
            //创建一个Group，groupLabel 置为空串
            String sort_key;
            if (token != ContactsQuery.SEARCH_TOKEN) {
                sort_key = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.SORT_KEY));
            } else {
                sort_key = "";
            }
            CollectionView.InventoryGroup group;
            if (!map.containsKey(sort_key)) {
                LOGV(TAG, "Creating new group: " + sort_key);
                group = new CollectionView.InventoryGroup(nextGroupId++)
                        .setDisplayCols(displayCols)
                        .setShowHeader(token != ContactsQuery.SEARCH_TOKEN)
                        .setHeaderLabel(!TextUtils.isEmpty(sort_key) ? sort_key : "#");
                map.put(sort_key, group);
                list.add(group);
            } else {
                LOGV(TAG, "Adding to existing group: " + sort_key);
                group = map.get(sort_key);
            }
            LOGV(TAG, "...adding to group '" + sort_key + "' with custom data index " + dataIndex);
            group.addItemWithCustomDataIndex(dataIndex);
        }
        ArrayList<CollectionView.InventoryGroup> groups = new ArrayList<CollectionView.InventoryGroup>();
        groups.addAll(list);
        LOGD(TAG, "Total:  " + list.size() + " list groups");
        for (CollectionView.InventoryGroup g : groups) {
            inventory.addGroup(g);
        }
        return inventory;
    }

    private ProgressDialog progressDialog;

    private void hideEmptyView() {
        mEmptyView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        progressDialog.dismiss();
    }

    private void showEmptyView() {
        if (mCurrentUri.equals(Contract.Contacts.buildTypeFilterUri(new String[]{Config.ContactsTypes.CATEGORY_DWTXL, "", ""}))) {
            mEmptyView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);

//            pd = ProgressDialog.show(getActivity(), null, getResources().getString(R.string.dialog_loading_process));
            progressDialog.setMessage(getString(R.string.dialog_loading_process));
            progressDialog.show();
        } else {
            mEmptyView.setText(R.string.no_matching_contacts);
            mEmptyView.setVisibility(View.VISIBLE);
            mLoadingView.setVisibility(View.GONE);
            progressDialog.dismiss();

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public View newCollectionHeaderView(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.list_item_contact_header, parent, false);
    }

    @Override
    public void bindCollectionHeaderView(Context context, View view, int groupId, String groupLabel) {
        TextView tv = (TextView) view.findViewById(R.id.letter_index);
        if (tv != null) {
            tv.setText(groupLabel);
        }
    }

    @Override
    public View newCollectionItemView(Context context, int groupId, int dataIndex, ViewGroup parent) {
        if (mCursor == null || !mCursor.moveToPosition(dataIndex)) {
            LOGW(TAG, "Can't new collection view item, dataIndex=" + dataIndex +
                    (mCursor == null ? ": cursor is null" : ": bad data index."));
            return null;
        }
        if (mIsSearchCursor) {
            return getActivity().getLayoutInflater().inflate(R.layout.list_item_contact_normal, parent, false);
        } else {
            if (mCursor.getString(mCursor.getColumnIndex(Contract.ContactTypes.TYPE_ID)).equals("TYPE_BMTXL")) {
                return getActivity().getLayoutInflater().inflate(R.layout.list_item_contact_special, parent, false);
            } else {
                return getActivity().getLayoutInflater().inflate(R.layout.list_item_contact_normal, parent, false);
            }
        }
    }

    @Override
    public void bindCollectionItemView(Context context, View view, int groupId, int indexInGroup, int dataIndex, Object tag) {
        if (mCursor == null || !mCursor.moveToPosition(dataIndex)) {
            LOGW(TAG, "Can't bind collection view item, dataIndex=" + dataIndex +
                    (mCursor == null ? ": cursor is null" : ": bad data index."));
            return;
        }

        final String contactId = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.CONTACT_ID));
        final String contactName = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.CONTACT_NAME));
        final int contactColor = Integer.valueOf(mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.CONTACT_COLOR))).intValue();
        final String post = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.POST));
        final String orgName = mCursor.getString(mCursor.getColumnIndex(Contract.Contacts.ORG_NAME));

        final TextView contactNameView = (TextView) view.findViewById(R.id.xm);
        final TextView orgNameView = (TextView) view.findViewById(R.id.szdw);
        final TextView postView = (TextView) view.findViewById(R.id.zw);
        final View contactTargetView = view.findViewById(R.id.contact_item_target);
        final ImageView imgView = (ImageView) view.findViewById(R.id.contact_icon);

        GradientDrawable grad = (GradientDrawable) imgView.getBackground();
        grad.setColor(contactColor);
        contactNameView.setText(contactName == null ? "?" : contactName);
        if (orgNameView != null) {
            orgNameView.setText(orgName == null ? "?" : orgName);
        }
        postView.setText(post == null ? "?" : post);
        contactTargetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onContactSelected(contactId, contactTargetView);
            }
        });
    }

    public boolean canSwipeRefreshChildScrollUp() {
        boolean b = ViewCompat.canScrollVertically(mCollectionView, -1);
        Log.i("canSwipviewcompat", Boolean.toString(b));
        return b;
    }

    public void setSelection(int position) {
        mCollectionView.setSelection(position);
    }

    void requestQueryUpdate(String query) {
        mHandler.removeMessages(MESSAGE_QUERY_UPDATE);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_QUERY_UPDATE, query),
                QUERY_UPDATE_DELAY_MILLIS);
    }

    public interface Callbacks {
        public void onTagMetadataLoaded(ContactTypeMetadata metadata);

        public void onContactSelected(String contactId, View clickedView);

        public void onUpdateInventory(Map<String, Integer> sections);
    }

    public void animateReload() {
        mCollectionView.setAlpha(0);
        mCollectionView.animate().alpha(1).setDuration(ANIM_DURATION).setInterpolator(new DecelerateInterpolator());
    }

    private interface ContactsQuery {
        int NORMAL_TOKEN = 0x1;
        int SEARCH_TOKEN = 0x3;

        String[] NORMAL_PROJECTION = {
                BaseColumns._ID,
                Contract.Contacts.CONTACT_ID,
                Contract.ContactTypes.TYPE_ID,
                Contract.Contacts.CONTACT_NAME,
                Contract.Contacts.POST,
                Contract.Contacts.ORG_NAME,
                Contract.Contacts.CONTACT_COLOR,
                Contract.Contacts.SORT_KEY,
        };

        String[] SEARCH_PROJECTION = {
                BaseColumns._ID,
                Contract.Contacts.CONTACT_ID,
                Contract.Contacts.CONTACT_NAME,
                Contract.Contacts.POST,
                Contract.Contacts.ORG_NAME,
                Contract.Contacts.CONTACT_COLOR,
        };
    }

    private static final int TAG_METADATA_TOKEN = 0x4;
}
