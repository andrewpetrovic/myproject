package com.itic.mobile.zfyj.qh.login;

/**
 * Created by JEEKR on 2015/1/28.
 */
public interface Callback {
    public void onAuthSuccess(String accountName,boolean newlyAuthenticate);
    public void onAuthFailure(String accountName,int errorCode);
}

