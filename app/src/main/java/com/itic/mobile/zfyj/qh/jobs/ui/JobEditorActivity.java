package com.itic.mobile.zfyj.qh.jobs.ui;

import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.itic.mobile.accounts.AccountUtils;
import com.itic.mobile.util.datetime.DateTimeUtils;
import com.itic.mobile.util.string.MD5Utils;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobEditorActivity extends BaseActivityImpl implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "JobEditorActivity";
    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();

    private String jobId;
    private EditText ed_name;
    private String name;
    private EditText ed_am_job;
    private String am_job;
    private EditText ed_pm_job;
    private String note;
    private EditText ed_note;
    private String pm_job;
    private TextView tv_date;
    private String date;
    private String ldid;

    private Uri mJobUri;

    private Button submitButton;

    private boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_editor);

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mJobUri = getIntent().getData();

        if (mJobUri != null){
            isNew = false;
            LoaderManager manager = getLoaderManager();
            manager.initLoader(JobQuery.JOBS_TOKEN,null,this);
        }else{
            isNew = true;
            Intent intent = getIntent();
            name = intent.getStringExtra("name");
            ldid = intent.getStringExtra("ldid");
        }

        ed_name = (EditText) findViewById(R.id.ed_name);
        ed_am_job = (EditText) findViewById(R.id.ed_am_job);
        ed_pm_job = (EditText) findViewById(R.id.ed_pm_job);
        ed_note = (EditText) findViewById(R.id.note);
        tv_date = (TextView) findViewById(R.id.tv_date);

        tv_date.setText(DateTimeUtils.dateToString(DateTimeUtils.dateToLang(new CalendarDay().getDate())));
        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNew){
                    showDatePickerDialog(JobEditorActivity.this,new CalendarDay(),new DatePickerDialog.OnDateSetListener(){

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            tv_date.setText(DateTimeUtils.dateToString(DateTimeUtils.dateToLang(new CalendarDay(year,monthOfYear,dayOfMonth).getDate())));
                        }
                    });
                }
            }
        });

        submitButton = (Button) findViewById(R.id.button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit(0);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isNew){
            getMenuInflater().inflate(R.menu.job_editor, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_del_job:
                submit(1);
        }
        return super.onOptionsItemSelected(item);
    }

    private void submit(final int subOrDel){
        HttpRequest request = new HttpRequest(QHYJApi.ldhdapModify, Request.Method.POST);
        final Map<String, String> map = new HashMap<String, String>();
        map.put(Config.KEY_USERID, AccountManager.get(this).getUserData(AccountUtils.getActiveAccount(this, Config.ACCOUNT_TYPE), AccountUtils.KEY_USER_ID));
        if (jobId == null || jobId.equals("")){
            map.put(Config.KEY_FORM_ID,"");
        }else{
            map.put(Config.KEY_FORM_ID,jobId);
        }
        map.put(Config.KEY_SUBMIT_TYPE, Integer.toString(subOrDel));
        map.put(Config.KEY_DATE, Long.toString(DateTimeUtils.stringToLong(tv_date.getText().toString())));
        map.put(Config.KEY_AM_JOB,subOrDel == 0?ed_am_job.getText().toString():"");
        map.put(Config.KEY_PM_JOB,subOrDel == 0?ed_pm_job.getText().toString():"");
        map.put(Config.KEY_NOTE,subOrDel == 0?ed_note.getText().toString():"");

        map.put(Config.KEY_SID, MD5Utils.md5(map.get(Config.KEY_USERID) + map.get(Config.KEY_SUBMIT_TYPE) + map.get(Config.KEY_DATE) + Config.sKey));
        map.put(Config.KEY_LDID,subOrDel == 0?ldid:"");
        map.put(Config.KEY_NAME,subOrDel == 0?ed_name.getText().toString():"");
        request.setParams(map);
        request.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, response);
                Gson gson = new Gson();
                ModifyResponse modifyResult = gson.fromJson(response, ModifyResponse.class);
                if (modifyResult.res_code.equals("0") && subOrDel == 0){
                    // 编辑后不变更数据库了，finish后在主界面从network reload
                    finish();
                }else if(modifyResult.res_code.equals("0") && subOrDel == 1){
                    // 删除后不变更数据库了，finish后在主界面从network reload
                    finish();
                }else if (modifyResult.res_code.equals("1")){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.error_server),Toast.LENGTH_SHORT).show();
                }else if (modifyResult.res_code.equals("2")){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.error_sid),Toast.LENGTH_SHORT).show();
                }else if (modifyResult.res_code.equals("3")){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.error_sid),Toast.LENGTH_SHORT).show();
                }else if (modifyResult.res_code.equals("4")){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.jobs_error_has_submit),Toast.LENGTH_SHORT).show();
                }
            }
        });
        VolleyUtil.getRequestQueue().add(request);
    }

    private class ModifyResponse {
        public String res_code;
        public String l;
    }

    private class Jobs{
        public List<Job> jobs;
        public Jobs(){
            jobs = new ArrayList<Job>();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (isNew){
            getActionBarToolbar().setTitle(getResources().getString(R.string.title_activity_new_job));
            if (!TextUtils.isEmpty(name)){
                ed_name.setText(name);
            }
        }else{
            getActionBarToolbar().setTitle(getResources().getString(R.string.title_activity_edit_job));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == JobQuery.JOBS_TOKEN){
            loader = new CursorLoader(this,mJobUri,JobQuery.JOBS_PROJECTION,null,null,null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == JobQuery.JOBS_TOKEN){
            if (!data.moveToFirst()){
                finish();
                return;
            }
            jobId = data.getString(data.getColumnIndex(Contract.Jobs.JOB_ID));
            ldid = data.getString(data.getColumnIndex(Contract.Jobs.LDID));
            name = data.getString(data.getColumnIndex(Contract.Jobs.NAME));
            date = DateTimeUtils.dateToString(Long.valueOf(data.getString(data.getColumnIndex(Contract.Jobs.DATE))).longValue());
            am_job = data.getString(data.getColumnIndex(Contract.Jobs.AM_JOB));
            pm_job = data.getString(data.getColumnIndex(Contract.Jobs.PM_JOB));
            note = data.getString(data.getColumnIndex(Contract.Jobs.NOTE));

            if (!TextUtils.isEmpty(name)){
                ed_name.setText(name);
            }

            if (!TextUtils.isEmpty(date)){
                tv_date.setText(date);
            }

            if (!TextUtils.isEmpty(am_job)){
                ed_am_job.setText(am_job);
            }

            if (!TextUtils.isEmpty(pm_job)){
                ed_pm_job.setText(pm_job);
            }
            if (!TextUtils.isEmpty(note)){
                ed_note.setText(note);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public static void showDatePickerDialog(Context context, CalendarDay day,
                                            DatePickerDialog.OnDateSetListener callback) {
        if(day == null) {
            day = new CalendarDay();
        }
        DatePickerDialog dialog = new DatePickerDialog(
                context, 0, callback, day.getYear(), day.getMonth(), day.getDay()
        );
        dialog.show();
    }

    private interface JobQuery {
        int JOBS_TOKEN = 0x1;

        String[] JOBS_PROJECTION = {
                BaseColumns._ID,
                Contract.Jobs.JOB_ID,
                Contract.Jobs.NAME,
                Contract.Jobs.DATE,
                Contract.Jobs.AM_JOB,
                Contract.Jobs.PM_JOB,
                Contract.Jobs.NOTE,
                Contract.Jobs.LDID
        };
    }
}
