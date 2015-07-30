package com.itic.mobile.zfyj.qh.login;

import com.google.gson.Gson;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.QHYJApi;
import com.itic.mobile.zfyj.qh.login.model.LoginModel;
import com.itic.mobile.net.HttpMethod;
import com.itic.mobile.net.NetConnection;

/**
 * login
 */
public class LoginAction {

    public LoginAction(String userName, String loginPwd, String token, String sid, final LoginSuccessCallback successCallback, final LoginFailCallback failCallback, final LoginErrorCallback errorCallback) {
        new NetConnection(true, QHYJApi.LoginInterfaceService, HttpMethod.POST,
//        new NetConnection(true, QHYJApi.LoginInterfaceService, HttpMethod.GET,
                new NetConnection.Callback() {

                    @Override
                    public void onSuccess(String result) {
                        LoginModel response = new Gson().fromJson(result, LoginModel.class);
                        if (response.res_code == Config.RESULT_STATUS_SUCCESS) {
                            successCallback.onSuccess(response);
                        } else if (response.res_code == Config.RESULT_STATUS_ERROR_SEVER_STATUS) {
                            errorCallback.onError(Config.RESULT_STATUS_ERROR_SEVER_STATUS);
                        } else if (response.res_code == Config.RESULT_STATUS_ERROR_SID) {
                            errorCallback.onError(Config.RESULT_STATUS_ERROR_SID);
                        } else if (response.res_code == Config.RESULT_STATUS_ERROR_USERNAME_PWD) {
                            failCallback.onFail();
                        } else if (response.res_code == Config.RESULT_STATUS_ERROR_TOKEN) {
                            errorCallback.onError(Config.RESULT_STATUS_ERROR_TOKEN);
                        } else if (response.res_code == Config.RESULT_STATUS_ERROR_PARAM) {
                            errorCallback.onError(Config.RESULT_STATUS_ERROR_PARAM);
                        }
                    }

                    @Override
                    public void onFail() {
                        if (errorCallback != null) {
                            errorCallback.onError(Config.RESULT_STATUS_ERROR_CONNECTION);
                        }
                    }

                    @Override
                    public void onLoading() {
                        //这里什么都不用做
                    }
                },
                Config.KEY_USERNAME,
                userName,
                Config.KEY_PWD,
                loginPwd,
                Config.KEY_TOKEN,
                token,
                Config.KEY_SID,
                sid
        );
    }

    public static interface LoginSuccessCallback {
        void onSuccess(LoginModel mLoginModel);
    }

    public static interface LoginFailCallback {
        void onFail();
    }

    public static interface LoginErrorCallback {
        void onError(int errorCode);
    }
}
