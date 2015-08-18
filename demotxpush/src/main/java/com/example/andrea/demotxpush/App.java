package com.example.andrea.demotxpush;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.example.andrea.demotxpush.observer.IObserver;
import com.example.andrea.demotxpush.observer.ObserverMessage;
import com.example.andrea.demotxpush.receiver.MsgReceiver;
import com.example.andrea.demotxpush.receiver.NotificationClickReceiver;
import com.example.andrea.demotxpush.receiver.NotificationReceiver;
import com.example.andrea.demotxpush.receiver.SetDeleteTagReceiver;
import com.itic.mobile.txpush.receiver.MessageReceiver;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGNotifaction;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.XGPushNotifactionCallback;

import java.util.List;

public class App extends Application implements XGIOperateCallback,IObserver {

    private static final String TAG = "App";

    private SetDeleteTagReceiver setDeleteTagReceiver;
    private MsgReceiver msgReceiver;
    private NotificationClickReceiver notificationClickReceiver;
    private NotificationReceiver notificationReceiver;

    public boolean isMainProcess(){
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info:processInfos){
            if (info.pid == myPid && mainProcessName.equals(info.processName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //注册
        XGPushConfig.enableDebug(getApplicationContext(), true);
        XGPushConfig.setAccessId(getApplicationContext(), 2100141006);
        XGPushConfig.setAccessKey(getApplicationContext(), "A72ZJE521IRN");
        XGPushManager.registerPush(getApplicationContext(),this);

        // 在主进程设置信鸽相关的内容
        if (isMainProcess()) {
            // 为保证弹出通知前一定调用本方法，需要在application的onCreate注册
            // 收到通知时，会调用本回调函数。
            // 相当于这个回调会拦截在信鸽的弹出通知之前被截取
            // 一般上针对需要获取通知内容、标题，设置通知点击的跳转逻辑等等
            XGPushManager
                    .setNotifactionCallback(new XGPushNotifactionCallback() {

                        @Override
                        public void handleNotify(XGNotifaction xGNotifaction) {
                            Log.i("test", "处理信鸽通知：" + xGNotifaction);
                            // 获取标签、内容、自定义内容
                            String title = xGNotifaction.getTitle();
                            String content = xGNotifaction.getContent();
                            String customContent = xGNotifaction
                                    .getCustomContent();
                            // 其它的处理
                            // 如果还要弹出通知，可直接调用以下代码或自己创建Notifaction，否则，本通知将不会弹出在通知栏中。
                            xGNotifaction.doNotify();
                        }
                    });
        }

        regiestReceiver();
    }

    @Override
    public void onSuccess(Object data, int errCode) {
        Log.w(TAG,
                "+++ register push sucess. token:" + data);
    }

    @Override
    public void onFail(Object data, int errCode, String msg) {
        Log.w(TAG,
                "+++ register push fail. token:" + data
                        + ", errCode:" + errCode + ",msg:"
                        + msg);
    }

    @Override
    public void handleObserverMessage(ObserverMessage message) {
        int type = message.getMessageType();
        switch (type){
            case ObserverMessage.BROADCAST_SET_TAG_SUCCESS:
                Log.i(TAG,"new tag set success," + "tag name: " + message.getTagName());
                Toast.makeText(getApplicationContext(),
                        "new tag set success," + "tag name: " + message.getTagName(),Toast.LENGTH_SHORT).show();
                break;
            case ObserverMessage.BROADCAST_DELETE_TAG_SUCCESS:
                Log.i(TAG,"tag delete success");
                Toast.makeText(getApplicationContext(),"tag delete success," + "tag name: " + message.getTagName(),Toast.LENGTH_SHORT).show();
                break;
            case ObserverMessage.BROADCAST_SET_TAG_FAIL:
                Log.i(TAG,"new tag set fail," + "tag name: " + message.getTagName());
                Toast.makeText(getApplicationContext(),
                        "new tag set fail," + "tag name: " + message.getTagName(),Toast.LENGTH_SHORT).show();
                break;
            case ObserverMessage.BROADCAST_DELETE_TAG_FAIL:
                Log.i(TAG,"tag delete fail," + "tag name: " + message.getTagName());
                Toast.makeText(getApplicationContext(),
                        "tag delete fail," + "tag name: " + message.getTagName(),Toast.LENGTH_SHORT).show();
                break;
            case ObserverMessage.BROADCAST_MSG:
                Log.i(TAG,"Msg title: " + message.getTitle()
                        + "; Msg content: " + message.getContent());
                Toast.makeText(getApplicationContext(),
                        "Msg title: " + message.getTitle()
                        + "; Msg content: " + message.getContent(),
                        Toast.LENGTH_SHORT).show();
                break;
            case ObserverMessage.BROADCAST_NOTIFICATION:
                Log.i(TAG,
                        "Notification ID: " + message.getNotificationId()
                        + "; Msg ID: " + message.getMsgId()
                        + "; Notification title: " + message.getTitle()
                        + "; Notification content: " + message.getContent()
                        + "; Notification action type: " + message.getNotificationActionType()
                        + "; Notification Activity: " + message.getNotificationActivity());
                Toast.makeText(getApplicationContext(),
                        "Notification ID: " + message.getNotificationId()
                        + "; Msg ID: " + message.getMsgId()
                        + "; Notification title: " + message.getTitle()
                        + "; Notification content: " + message.getContent()
                        + "; Notification action type: " + message.getNotificationActionType()
                        + "; Notification Activity: " + message.getNotificationActivity(),
                        Toast.LENGTH_SHORT).show();
                break;
            case ObserverMessage.BROADCAST_NOTIFICATION_CLICK:
                String text;
                int clickActionType = message.getNotificationClickActionType();
                switch (clickActionType){
                    case XGPushClickedResult.NOTIFACTION_CLICKED_TYPE:
                        text = "Notification is CLICKED!!";
                        break;
                    case XGPushClickedResult.NOTIFACTION_DELETED_TYPE:
                        text = "Notification is DELETED!!";
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown click action type: " + Integer.toString(clickActionType));
                }
                Log.i(TAG,text
                        + "; Msg ID: " + message.getMsgId()
                        + "; Notification title: " + message.getTitle()
                        + "; Notification content: " + message.getContent()
                        + "; Notification action type: " + message.getNotificationActionType()
                        + "; Notification Activity: " + message.getNotificationActivity());
                Toast.makeText(getApplicationContext(),text
                        + "; Msg ID: " + message.getMsgId()
                        + "; Notification title: " + message.getTitle()
                        + "; Notification content: " + message.getContent()
                        + "; Notification action type: " + message.getNotificationActionType()
                        + "; Notification Activity: " + message.getNotificationActivity(),
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                throw new UnsupportedOperationException("Unknown message type: " + Integer.toString(type));
        }
    }

    private void regiestReceiver(){
        setDeleteTagReceiver = new SetDeleteTagReceiver();
        msgReceiver = new MsgReceiver();
        notificationClickReceiver = new NotificationClickReceiver();
        notificationReceiver = new NotificationReceiver();

        setDeleteTagReceiver.attachObserver(this);
        msgReceiver.attachObserver(this);
        notificationClickReceiver.attachObserver(this);
        notificationReceiver.attachObserver(this);

        IntentFilter tagIntentFilter = new IntentFilter();
        tagIntentFilter.addAction(MessageReceiver.TAG_ACTION);
        getApplicationContext().registerReceiver(setDeleteTagReceiver, tagIntentFilter);

        IntentFilter msgIntentFilter = new IntentFilter();
        msgIntentFilter.addAction(MessageReceiver.MESSAGE_ACTION);
        getApplicationContext().registerReceiver(msgReceiver,msgIntentFilter);

        IntentFilter notificationIntentFilter = new IntentFilter();
        notificationIntentFilter.addAction(MessageReceiver.NOTIFICATION_ACTION);
        getApplicationContext().registerReceiver(notificationReceiver,notificationIntentFilter);

        IntentFilter notificationClickIntentFilter = new IntentFilter();
        notificationClickIntentFilter.addAction(MessageReceiver.NOTIFICATION_CLICK_ACTION);
        getApplicationContext().registerReceiver(notificationClickReceiver,notificationClickIntentFilter);
    }
}