package com.itic.mobile.zfyj.qh.jobs.model;

import com.itic.mobile.util.model.HashUtils;

/**
 * Created by JEEKR on 2015/3/24.
 */
public class Job {
    public String job_id;
    public String ldid;
    public String name;
    public long date;
    public String am_job;
    public String pm_job;
    public String note;

    public String getImportHashCode(){
        StringBuilder sb = new StringBuilder();
        sb.append("job_id").append(job_id == null ? "" : job_id)
                .append("ldid").append(ldid == null? "":ldid)
                .append("name").append(name == null ? null : name)
                .append("date").append(date == 0 ? "" : date)
                .append("am_job").append(am_job == null ? "" : am_job)
                .append("pm_job").append(pm_job == null ? "" : pm_job)
                .append("note").append(note == null ? "":note);
        return HashUtils.computeWeakHash(sb.toString());
    }

    public boolean isDelete() {
        return isDelete;
    }

    public boolean isDelete;

    public boolean isFromServer(){
        return isFromServer;
    }

    public boolean isFromServer;
}
