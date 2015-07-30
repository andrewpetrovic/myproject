package com.itic.mobile.zfyj.qh.test;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.itic.mobile.base.ui.widget.DrawShadowFrameLayout;
import com.itic.mobile.util.ThrottledContentObserver;
import com.itic.mobile.util.datetime.DateTimeUtils;
import com.itic.mobile.util.ui.TextViewUtils;
import com.itic.mobile.util.ui.UIUtils;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.base.BaseActivityImpl;
import com.itic.mobile.zfyj.qh.jobs.model.Job;
import com.itic.mobile.zfyj.qh.jobs.ui.SimpleListView;
import com.itic.mobile.zfyj.qh.provider.Contract;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Andrea Ji
 */
public class TestActivity2 extends BaseActivityImpl implements OnDateChangedListener, LoaderManager.LoaderCallbacks<Cursor> {

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
    private boolean mJobsDataIsFullReload;
    private boolean mJobsDataIsFinishReload;

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
    protected void onResume() {
        super.onResume();
    }

    private void updateViewsTopClearance() {
        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        int gridPadding = getResources().getDimensionPixelSize(R.dimen.explore_grid_padding);
        mDrawShadowFrameLayout.setShadowTopOffset(actionBarClearance);
        mScrollViewChild.setPadding(calendarView.getPaddingLeft(), actionBarClearance + gridPadding, calendarView.getPaddingRight(), calendarView.getPaddingBottom());
//        calendarView.setPadding(calendarView.getPaddingLeft(), actionBarClearance + gridPadding, calendarView.getPaddingRight(), calendarView.getPaddingBottom());

//        mDetailsContainer.setPadding(mDetailsContainer.getPaddingLeft(), actionBarClearance + gridPadding + calendarView.getHeight(), mDetailsContainer.getPaddingRight(), mDetailsContainer.getPaddingBottom());
    }

    //一级界面必须覆盖这个方法，返回值为BaseActivityImpl中定义的与该acivity对应的NAVDRAWER_ITEM值
    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SHOW_LEADER_ON_JOB_STATES;
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
        Bundle args = intentToFragmentArguments(getIntent());
        reloadFromArguments(args);
        getContentResolver().registerContentObserver(Contract.Jobs.CONTENT_URI, true, mJobsObserver);
        updateViewsTopClearance();
    }

    private void onJobsContentChanged() {
        //getLoaderManager.restartLoader
        reloadJobsData(false);
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
        mJobsDataIsFinishReload = false;
        mJobsQueryToken = JobsQuery.JOBS_TOKEN;
        reloadJobsData(true);
    }

    private void reloadJobsData(boolean fullReload) {
        mJobsDataIsFullReload = fullReload;
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
            adapter.changeCursor(mCursor);
            animateReload();
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
                Contract.Jobs.PM_JOB
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
        public void bindView(View view, final Context context, Cursor cursor) {
            Holder holder = getHolder(view);
            holder.nameView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.NAME))));
            holder.amJobView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.AM_JOB))));
            holder.pmJobView.setText(TextViewUtils.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Jobs.PM_JOB))));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,"test",Toast.LENGTH_SHORT).show();
                }
            });
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
