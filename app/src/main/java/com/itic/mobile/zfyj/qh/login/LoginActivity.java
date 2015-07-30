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
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Auth token type
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.username);
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
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = mEmailView.getText().toString();
        final String password = MD5Utils.md5(mPasswordView.getText().toString()).toUpperCase();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(R.string.error_username_field_required));
            focusView = mEmailView;
            cancel = true;
        }
//        else if (!isEmailValid(username)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //dialog不用设置title
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
//    private boolean isEmailValid(String email) {
//        //TODO: Replace this with your own logic
//        return email.contains("@");
//    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    public void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }

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

        mEmailView.setAdapter(adapter);
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



