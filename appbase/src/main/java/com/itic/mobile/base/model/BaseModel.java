package com.itic.mobile.base.model;

import com.google.gson.Gson;

/**
 * Created by andrew on 2014/8/19.
 */
@Deprecated
public class BaseModel {
    public int res_code;
    public String toJson() {
        return new Gson().toJson(this);
    }
}
