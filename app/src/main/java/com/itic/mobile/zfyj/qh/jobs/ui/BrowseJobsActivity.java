package com.itic.mobile.zfyj.qh.jobs.ui;

import android.accounts.AccountManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.itic.mobile.accounts.AccountUtils;
import com.itic.mobile.base.ui.widget.DrawShadowFrameLayout;
import com.itic.mobile.util.ThrottledContentObserver;
import com.itic.mobile.util.app.PrefUtils;
import com.itic.mobile.util.datetime.DateTimeUtils;
import com.itic.mobile.util.string.MD5Utils;
import com.itic.mobile.util.ui.TextViewUtils;
import com.itic.mobile.util.ui.UIUtils;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.QHYJApi;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.base.BaseActivityImpl;
import com.itic.mobile.zfyj.qh.jobs.model.Job;
import com.itic.mobile.zfyj.qh.provider.Contract;
import com.itic.mobile.zfyj.qh.sync.AppDataHandler;
import com.itic.mobile.zfyj.qh.sync.HttpRequest;
import com.itic.mobile.zfyj.qh.sync.VolleyUtil;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Ji
 */
public class BrowseJobsActivity extends BaseActivityImpl implements OnDateChangedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "BrowseJobsActivity";

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();

    private MaterialCalendarView calendarView;

    private JobsDataAdapter adapter;

    private SimpleListView mDetailsContainer;
    private View mScrollViewChild;
    private TextView mEmptyView;
    private View mLoadingView;

    private DrawShadowFrameLayout mDrawShadowFrameLayout;

    private ThrottledContentObserver mJobsObserver;

    private Date selectedDate = null;

    private Bundle mArguments;
    private Uri mCurrentUri;
    private Cursor mCursor;

    private int mJobsQueryToken;

    private static final int ANIM_DURATION = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_jobs);
        mDetailsContainer = (SimpleListView) findViewById(R.id.jobs_container);
        mScrollViewChild = findViewById(R.id.scroll_view_child);
        mEmptyView = (TextView) findViewById(R.id.empty_text);
        mLoadingView = findViewById(R.id.loading);
        adapter = new JobsDataAdapter();
        mDetailsContainer.setAdapter(adapter);
        // 初始化CalendarView
        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        calendarView.setOnDateChangedListener(this);
        // 设置当前日期
        Calendar calendar = Calendar.getInstance();
        Date currentDate = new CalendarDay(calendar.getTime()).getDate();
        selectedDate = currentDate;
        calendarView.setSelectedDate(calendar.getTime());
        mDrawShadowFrameLayout = (DrawShadowFrameLayout) findViewById(R.id.main_content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AccountManager.get(this).getUserData(AccountUtils.getActiveAccount(this, Config.ACCOUNT_TYPE), AccountUtils.KEY_YHLX_CODE).equals("2")) {
            getMenuInflater().inflate(R.menu.browse_jobs, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add_job:{
                HttpRequest request = new HttpRequest(QHYJApi.ldhdapAdd);
                Map<String, String> map = new HashMap<String, String>();
                map.put(Config.KEY_USERID, AccountManager.get(this).getUserData(AccountUtils.getActiveAccount(this, Config.ACCOUNT_TYPE), AccountUtils.KEY_USER_ID));
                map.put(Config.KEY_USERCATEGORY, AccountManager.get(this).getUserData(AccountUtils.getActiveAccount(this, Config.ACCOUNT_TYPE), AccountUtils.KEY_YHLX_CODE));
                map.put(Config.KEY_SID, MD5Utils.md5(map.get(Config.KEY_USERID) + map.get(Config.KEY_USERCATEGORY) + Config.sKey));
                request.setParams(map);
                request.setListener(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response);
                        Gson gson = new Gson();
                        PreAddResponse pre = gson.fromJson(response, PreAddResponse.class);
                        if (pre.res_code.equals("0")){
                            Intent i = new Intent(getApplicationContext(),JobEditorActivity.class);
                            i.putExtra("ldid",pre.ldid);
                            i.putExtra("name",pre.name);
                            startActivity(i);
                        }
                    }
                });
                VolleyUtil.getRequestQueue().add(request);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class PreAddResponse{
        public String name;
        public String res_code;
        public String ldid;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateViewsTopClearance() {
        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        int gridPadding = getResources().getDimensionPixelSize(R.dimen.explore_grid_padding);
        mDrawShadowFrameLayout.setShadowTopOffset(actionBarClearance);
        mScrollViewChild.setPadding(calendarView.getPaddingLeft(), actionBarClearance + gridPadding, calendarView.getPaddingRight(), calendarView.getPaddingBottom());
    }

    //一级界面必须覆盖这个方法，返回值为BaseActivityImpl中定义的与该acivity对应的NAVDRAWER_ITEM值
    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SHOW_LEADER_JOBS;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActionBarToolbar().setTitle(FORMATTER.format(selectedDate));
        mJobsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                onJobsContentChanged();
            }
        });
        getContentResolver().registerContentObserver(Contract.Jobs.CONTENT_URI, true, mJobsObserver);
        updateViewsTopClearance();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Bundle args = intentToFragmentArguments(getIntent());
        reloadFromArguments(args);
    }

    private void onJobsContentChanged() {
        //getLoaderManager.restartLoader
        reloadJobsData();
    }

    private void hideEmptyView() {
        mEmptyView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.GONE);
            mDetailsContainer.setVisibility(View.VISIBLE);
//            animateReload();
    }

    private void showEmptyView() {
            mEmptyView.setText(R.string.no_matching_jobs);
            mEmptyView.setVisibility(View.VISIBLE);
            mLoadingView.setVisibility(View.GONE);
        mDetailsContainer.setVisibility(View.GONE);
    }

    private void showLoadingView(){
        mEmptyView.setVisibility(View.GONE);
        mDetailsContainer.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
    }

    private void reloadFromArguments(Bundle args) {
        if (args == null) {
            args = new Bundle();
        } else {
            args = (Bundle) args.clone();
        }

        mArguments = args;
        mCurrentUri = mArguments.getParcelable("_uri");
        if (mCurrentUri == null) {
            args = intentToFragmentArguments(
                    new Intent(Intent.ACTION_VIEW, Contract.Jobs.buildDateFilgerUri(DateTimeUtils.dateToLongString(selectedDate))));
            reloadFromArguments(args);
        }
        mJobsQueryToken = JobsQuery.JOBS_TOKEN;
        reloadJobsData();
        reloadFromNetwork();
    }

    private void reloadFromNetwork() {
        HttpRequest request = new HttpRequest(QHYJApi.ldhdap);
        Map<String, String> map = new HashMap<String, String>();
        map.put(Config.KEY_USERID, AccountManager.get(this).getUserData(AccountUtils.getActiveAccount(this, Config.ACCOUNT_TYPE), AccountUtils.KEY_USER_ID));
        map.put(Config.KEY_USERCATEGORY, AccountManager.get(this).getUserData(AccountUtils.getActiveAccount(this, Config.ACCOUNT_TYPE), AccountUtils.KEY_YHLX_CODE));
        map.put(Config.KEY_DATE, DateTimeUtils.dateToLongString(selectedDate));
        request.setParams(map);
        request.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, response);
                AppDataHandler dataHandler = new AppDataHandler(getApplicationContext());
                try {
                    dataHandler.applyConferenceData(new String[]{response}, dataHandler.getDataTimestamp(), false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        VolleyUtil.getRequestQueue().add(request);
        showLoadingView();
    }


    private void reloadJobsData() {
        getLoaderManager().restartLoader(mJobsQueryToken, mArguments, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        final Intent intent = fragmentArgumentsToIntent(data);
        Uri jobsUri = intent.getData();
        Loader<Cursor> loader = null;
        if (id == JobsQuery.JOBS_TOKEN) {
            loader = new CursorLoader(this, jobsUri, JobsQuery.JOBS_PROJECTION, null, null, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int token = loader.getId();
        if (token == JobsQuery.JOBS_TOKEN) {
            if (mCursor != null && mCursor != cursor) {
                mCursor.close();
            }
            mCursor = cursor;
            mCursor.moveToPosition(-1);
            if (mCursor.getCount() == 0){
                showEmptyView();
                adapter.changeCursor(mCursor);
            }else{
                hideEmptyView();
                adapter.changeCursor(mCursor);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void animateReload() {
        mDetailsContainer.setAlpha(0);
        mDetailsContainer.animate().alpha(1).setDuration(ANIM_DURATION).setInterpolator(new DecelerateInterpolator());
    }

    @Override
    public void onDateChanged(MaterialCalendarView widget, CalendarDay date) {
        selectedDate = date.getDate();
        getActionBarToolbar().setTitle(FORMATTER.format(selectedDate));
        reloadFromCalendarView();
    }

    private void reloadFromCalendarView() {
        Bundle args = intentToFragmentArguments(new Intent(Intent.ACTION_VIEW, Contract.Jobs.buildDateFilgerUri(DateTimeUtils.dateToLongString(selectedDate))));
        Log.d(TAG, "selectedDate:" + DateTimeUtils.dateToLongString(selectedDate));
        reloadFromArguments(args);
        animateReload();
    }

    private interface JobsQuery {
        int JOBS_TOKEN = 0x1;

        String[] JOBS_PROJECTION = {
                BaseColumns._ID,
                Contract.Jobs.JOB_ID,
                Contract.Jobs.NAME,
                Contract.Jobs.AM_JOB,
                Contract.Jobs.PM_JOB,
                Contract.Jobs.NOTE
        };
    }

    private class JobsDataAdapter extends CursorAdapter {

        public JobsDataAdapter() {
            super(getApplicationContext(), null, false);
        }

        @Override
        public Job getItem(int position) {
            if (mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }
            final String jobId = mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.JOB_ID));
            final String name = mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.NAME));
            final String amJob = mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.AM_JOB));
            final String pmJob = mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.PM_JOB));

            Job mJob = new Job();
            mJob.job_id = jobId;
            mJob.name = name;
            mJob.am_job = amJob;
            mJob.pm_job = pmJob;
            return mJob;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater mInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return mInflater.inflate(R.layout.list_item_jobs, null);
        }

        @Override
        public void bindView(final View view, final Context context, Cursor cursor) {
            Holder holder = getHolder(view);
            holder.nameView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.NAME))));
            holder.amJobView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.AM_JOB))));
            holder.pmJobView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.PM_JOB))));
            if (AccountManager.get(getApplicationContext()).getUserData(AccountUtils.getActiveAccount(getApplicationContext(), Config.ACCOUNT_TYPE), AccountUtils.KEY_YHLX_CODE).equals("2")) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getLUtils().startActivityWithTransition(new Intent(Intent.ACTION_VIEW, Contract.Jobs.buildJobUri(mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.JOB_ID)))), view, null);
                    }
                });
            }
        }

        private Holder getHolder(final View view) {
            Holder holder = (Holder) view.getTag();
            if (holder == null) {
                holder = new Holder(view);
                view.setTag(holder);
            }
            return holder;
        }
    }

    private static class Holder {
        public TextView nameView;
        public TextView amJobView;
        public TextView pmJobView;

        public Holder(View view) {
            nameView = (TextView) view.findViewById(R.id.tv_leader_name);
            amJobView = (TextView) view.findViewById(R.id.tv_am_job);
            pmJobView = (TextView) view.findViewById(R.id.tv_pm_job);
        }
    }
}
