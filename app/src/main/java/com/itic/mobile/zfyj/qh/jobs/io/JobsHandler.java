package com.itic.mobile.zfyj.qh.jobs.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.itic.mobile.util.database.JSONHandler;
import com.itic.mobile.zfyj.qh.jobs.model.Job;
import com.itic.mobile.zfyj.qh.provider.Contract;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * JobsHandler
 */
public class JobsHandler extends JSONHandler {
    private HashMap<String, Job> mJobs = new HashMap<String, Job>();

    public JobsHandler(Context context) {
        super(context);
    }

    @Override
    public void process(JsonElement element) {
        for (Job job : new Gson().fromJson(element, Job[].class)) {
            mJobs.put(job.job_id, job);
        }
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        //当mJobs为空，没必要重新生成contact 数据
        Uri uri = Contract.addCallerIsSyncAdapterParameter(
                Contract.Jobs.CONTENT_URI);
        // 清空缓存数据
        list.add(ContentProviderOperation.newDelete(uri).build());

        //将新获取的数据插入数据库
        int updateJobs = 0;
        for (Job job : mJobs.values()) {
            ++updateJobs;
            buildJob(true, job, list);
            buildMyJob(job, list);
        }
    }

    private void buildMyJob(Job job, ArrayList<ContentProviderOperation> list) {
        final Uri uri = Contract.addCallerIsSyncAdapterParameter(Contract.Jobs.buildMyJobDirUri(job.job_id));
        //删除已存在的mapping
        list.add(ContentProviderOperation.newDelete(uri).build());
        //插入mapping
        list.add(ContentProviderOperation.newInsert(uri).withValue(Contract.MyJobs.JOB_ID, job.job_id).build());
    }

    private void buildJob(boolean isInsert, Job job, ArrayList<ContentProviderOperation> list) {
        ContentProviderOperation.Builder builder;
        Uri allJobUri = Contract
                .addCallerIsSyncAdapterParameter(Contract.Jobs.CONTENT_URI);
        Uri thisJobUri = Contract
                .addCallerIsSyncAdapterParameter(Contract.Jobs.buildJobUri(
                        job.job_id));

        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allJobUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisJobUri);
        }

        builder.withValue(Contract.Jobs.JOB_ID, job.job_id);
        builder.withValue(Contract.Jobs.LDID, job.ldid);
        builder.withValue(Contract.Jobs.NAME, job.name);
        builder.withValue(Contract.Jobs.DATE, job.date);
        builder.withValue(Contract.Jobs.AM_JOB, job.am_job);
        builder.withValue(Contract.Jobs.PM_JOB, job.pm_job);
        builder.withValue(Contract.Jobs.NOTE, job.note);
        builder.withValue(Contract.Jobs.JOB_IMPORT_HASHCODE, job.getImportHashCode());
        list.add(builder.build());
    }
}
