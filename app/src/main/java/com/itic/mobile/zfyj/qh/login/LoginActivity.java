package com.itic.mobile.zfyj.qh.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.app.App;
import com.itic.mobile.zfyj.qh.contacts.ui.BrowseContactsActivity;
import com.itic.mobile.zfyj.qh.login.model.LoginModel;
import com.itic.mobile.zfyj.qh.sync.SyncHelper;
import com.itic.mobile.util.string.MD5Utils;
import com.tencent.android.tpush.XGPushConfig;

/**
 * 登录界面
 */
public class LoginActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // UI 组件.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // 设置login form
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        getSupportLoaderManager().initLoader(0, null, this);
    }


    /**
     * 登录流程
     */
    public void attemptLogin() {

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // 获取输入内容
        final String username = mUserNameView.getText().toString();
        final String password = MD5Utils.md5(mPasswordView.getText().toString()).toUpperCase();

        boolean cancel = false;
        View focusView = null;


        // 检查输入内容
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUserNameView.setError(getString(R.string.error_username_field_required));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // 如果输入错误，则在UI上提示，不发起登录
            focusView.requestFocus();
        } else {
            //换出progressdialog，登录成功后，progressdialog消失
            final ProgressDialog pd = ProgressDialog.show(LoginActivity.this, null, getResources().getString(R.string.dialog_login_process));
            LoginAction loginAction = new LoginAction(username, password, XGPushConfig.getToken(getApplicationContext()), MD5Utils.md5(username + password + XGPushConfig.getToken(getApplicationContext()) + Config.sKey),
                    new LoginAction.LoginSuccessCallback() {

                        @Override
                        public void onSuccess(LoginModel mLoginModel) {
                            //用户名密码正确
                            pd.dismiss();
                            //添加账号
                            SyncHelper.CreateSyncAccount(getApplicationContext(), username, password, mLoginModel);
                            startActivity(new Intent(App.getContext(), BrowseContactsActivity.class));
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        }
                    },
                    new LoginAction.LoginFailCallback() {

                        @Override
                        public void onFail() {
                            //用户名密码错误
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    R.string.error_incorrect_password,
                                    Toast.LENGTH_LONG).show();
                        }
                    },
                    new LoginAction.LoginErrorCallback() {

                        @Override
                        public void onError(int errorCode) {
                            pd.dismiss();
                            //服务器异常
                            if (errorCode == Config.RESULT_STATUS_ERROR_SEVER_STATUS) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.error_server, Toast.LENGTH_SHORT)
                                        .show();
                            }
                            //SID错误
                            if (errorCode == Config.RESULT_STATUS_ERROR_SID) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.error_sid, Toast.LENGTH_SHORT)
                                        .show();
                            }
                            //上报token与后台记录不匹配
                            if (errorCode == Config.RESULT_STATUS_ERROR_TOKEN) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.error_token, Toast.LENGTH_SHORT)
                                        .show();
                            }
                            //缺少参数
                            if (errorCode == Config.RESULT_STATUS_ERROR_PARAM) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.error_param, Toast.LENGTH_SHORT)
                                        .show();
                            }
                            //网络连接异常
                            if (errorCode  == Config.RESULT_STATUS_ERROR_CONNECTION) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.error_connection, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }
            );
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserNameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}



