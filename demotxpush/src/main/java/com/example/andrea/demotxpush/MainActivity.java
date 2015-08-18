package com.example.andrea.demotxpush;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.tencent.android.tpush.XGPushManager;

public class MainActivity extends ActionBarActivity{

    private EditText editTag;
    private EditText editAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTag = (EditText) findViewById(R.id.edit_tag);
        editAccount = (EditText) findViewById(R.id.edit_account);
    }

    public void setTag(View view){
        String tagName = editTag.getText().toString();
        XGPushManager.setTag(getApplicationContext(),tagName);
    }

    public void delTag(View view){
        String tagName = editTag.getText().toString();
        XGPushManager.deleteTag(getApplicationContext(), tagName);
    }

    public void regAccount(View view){
        String accountName = editAccount.getText().toString();
        XGPushManager.registerPush(getApplicationContext(),accountName);
    }

    public void unRegAccount(View view){
        XGPushManager.registerPush(getApplicationContext(),"*");
    }

    public void unReg(View view){
        XGPushManager.unregisterPush(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}