package com.itic.mobile.txpush.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

/**
 * XGPushBaseReceiver类提供透传消息的接收和操作结果的反馈
 */
public class MessageReceiver extends XGPushBaseReceiver {
    public static final String LogTag = "MessageReceiver";

    public static final String REGISTER_ACTION = "com.itic.mobile.xg.push.broadcast.REGISTER";
    public static final String TAG_ACTION = "com.itic.mobile.xg.push.broadcast.TAG";
    public static final String NOTIFICATION_ACTION = "com.itic.mobile.xg.push.broadcast.NOTIFICATION";
    public static final String MESSAGE_ACTION = "com.itic.mobile.xg.push.broadcast.MESSAGE";
    public static final String NOTIFICATION_CLICK_ACTION = "com.itic.mobile.xg.push.broadcast.NOTIFICATION_CLICK";

    public static final String KEY_ERROR_CODE = "KEY_ERROR_CODE";

    public static final String KEY_REGISTE = "KEY_REGISTE";
    public static final String KEY_UNREGISTE = "KEY_UNREGISTE";
    public static final String KEY_ACCESSID = "KEY_ACCESSID";
    public static final String KEY_TOKEN = "KEY_TOKEN";
    public static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    public static final String KEY_TICKET = "KEY_TICKET";
    public static final String KEY_TICKET_TYPE = "KEY_ACCESSID";

    public static final String KEY_TAG = "KEY_TAG";
    public static final String KEY_SET_TAG = "KEY_SET_TAG";
    public static final String KEY_DELETE_TAG = "KEY_DELETE_TAG";
    public static final String KEY_TAG_NAME = "KEY_TAG_NAME";


    public static final String KEY_MSG_CONTENT = "KEY_MSG_CONTENT";
//    public static final String KEY_MSG_TITLE = "KEY_MSG_TITLE";

    public static final String KEY_MSG_ID = "KEY_MSG_ID";
    public static final String KEY_NOTIFICATION_ID = "KEY_NOTIFICATION_ID";
    public static final String KEY_NOTIFICATION_TITLE = "KEY_NOTIFICATION_TITLE";
    public static final String KEY_NOTIFICATION_CLICK_ACTION_TYPE = "KEY_NOTIFICATION_CLICK_ACTION_TYPE";
    public static final String KEY_NOTIFICATION_CONTENT = "KEY_NOTIFICATION_CONTENT";
    public static final String KEY_NOTIFICATION_ACTION_TYPE = "KEY_NOTIFICATION_ACTION_TYPE";
    public static final String KEY_NOTIFICATION_ACTIVITY = "KEY_NOTIFICATION_ACTIVITY";

    /**
     * 注册结果
     * @param context 当前app的context
     * @param errorCode 返回码， 0代表调用成功，其他编码请产考 http://developer.xg.qq.com/index.php/%E8%BF%94%E5%9B%9E%E7%A0%81%E6%8F%8F%E8%BF%B0
     * @param message 服务器返回的消息
     */
    @Override
    public void onRegisterResult(Context context, int errorCode,
                                 XGPushRegisterResult message) {
        if (context == null || message == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = message + "注册成功";
            // 在这里拿token
            String token = message.getToken();
        } else {
            text = message + "注册失败，错误码：" + errorCode;
        }
        //发出广播
        Intent intent = new Intent();
        intent.setAction(REGISTER_ACTION);
        intent.putExtra(KEY_ERROR_CODE, errorCode);
        intent.putExtra(KEY_ACCESSID,message.getAccessId());
        intent.putExtra(KEY_TOKEN,message.getToken());
        intent.putExtra(KEY_ACCOUNT,message.getAccount());
        intent.putExtra(KEY_TICKET,message.getTicket());
        intent.putExtra(KEY_TICKET_TYPE,message.getTicketType());
        intent.putExtra(KEY_TAG,KEY_REGISTE);
        context.sendBroadcast(intent);
        Log.d(LogTag, text);
    }

    /**
     * 反注册结果
     * @param context 当前app的context
     * @param errorCode 返回码， 0代表调用成功
     */
    @Override
    public void onUnregisterResult(Context context, int errorCode) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "反注册成功";
        } else {
            text = "反注册失败" + errorCode;
        }
        Log.d(LogTag, text);
        //发出广播
        Intent intent = new Intent();
        intent.setAction(REGISTER_ACTION);
        intent.putExtra(KEY_ERROR_CODE, errorCode);
        intent.putExtra(KEY_TAG,KEY_UNREGISTE);
        context.sendBroadcast(intent);
        Log.d(LogTag, text);
    }

    /**
     * Tag设置结果
     * @param context
     * @param errorCode
     * @param tagName
     */
    @Override
    public void onSetTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "\"" + tagName + "\"设置成功";
        } else {
            text = "\"" + tagName + "\"设置失败,错误码：" + errorCode;
        }
        Log.d(LogTag, text);
        //发出广播
        Intent intent = new Intent();
        intent.setAction(TAG_ACTION);
        intent.putExtra(KEY_ERROR_CODE, errorCode);
        intent.putExtra(KEY_TAG_NAME,tagName);
        intent.putExtra(KEY_TAG,KEY_SET_TAG);
        context.sendBroadcast(intent);
    }

    /**
     * Tag删除结果
     * @param context
     * @param errorCode
     * @param tagName
     */
    @Override
    public void onDeleteTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "\"" + tagName + "\"删除成功";
        } else {
            text = "\"" + tagName + "\"删除失败,错误码：" + errorCode;
        }
        Log.d(LogTag, text);
        //发出广播
        Intent intent = new Intent();
        intent.setAction(TAG_ACTION);
        intent.putExtra(KEY_ERROR_CODE,errorCode);
        intent.putExtra(KEY_TAG_NAME,tagName);
        intent.putExtra(KEY_TAG,KEY_DELETE_TAG);
        context.sendBroadcast(intent);
    }

    /**
     * 接收透传消息
     * @param context app上下文对象
     * @param message 收到的透传消息
     */
    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        String text = "收到消息:" + message.toString();
        Log.d(LogTag, text);
        //发出广播
        Intent intent = new Intent();
        intent.setAction(MESSAGE_ACTION);
        intent.putExtra(KEY_MSG_CONTENT, message.getContent());
        // title暂时没什么用
//        intent.putExtra(KEY_MSG_TITLE,message.getTitle());
        context.sendBroadcast(intent);
    }

    /**
     * 通知点击回调 actionType=1为该消息被清除，actionType=0为该消息被点击
     * @param context app上下文对象
     * @param message 被点击的通知对象，提供读取被点击的通知内容的接口
     */
    @Override
    public void onNotifactionClickedResult(Context context,
                                           XGPushClickedResult message) {
        if (context == null || message == null) {
            return;
        }
        String text = "";
        if (message.getActionType() == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
            // 通知在通知栏被点击啦。。。。。
            // APP自己处理点击的相关动作
            // 这个动作可以在activity的onResume也能监听，请看第3点相关内容
            text = "通知被打开 :" + message;
        } else if (message.getActionType() == XGPushClickedResult.NOTIFACTION_DELETED_TYPE) {
            // 通知被清除啦。。。。
            // APP自己处理通知被清除后的相关动作
            text = "通知被清除 :" + message;
        }
        Log.d(LogTag, text);
        //发出广播
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_CLICK_ACTION);
        intent.putExtra(KEY_MSG_ID, message.getMsgId());
        intent.putExtra(KEY_NOTIFICATION_TITLE, message.getTitle());
        intent.putExtra(KEY_NOTIFICATION_CONTENT,message.getContent());
        intent.putExtra(KEY_NOTIFICATION_CLICK_ACTION_TYPE,Integer.parseInt(String.valueOf(message.getActionType())));
        intent.putExtra(KEY_NOTIFICATION_ACTION_TYPE, message.getNotificationActionType());
        intent.putExtra(KEY_NOTIFICATION_ACTIVITY, message.getActivityName());
        context.sendBroadcast(intent);
    }

    /**
     * 获取被展示通知
     * @param context app上下文对象
     * @param notifiShowedRlt 被显示的通知对象，提供读取被显示的通知内容的接口
     */
    @Override
    public void onNotifactionShowedResult(Context context,
                                          XGPushShowedResult notifiShowedRlt) {
        if (context == null || notifiShowedRlt == null) {
            return;
        }
        String text = "收到通知" + " title: "+ notifiShowedRlt.getTitle() +" content: " + notifiShowedRlt.getContent();
        Log.d(LogTag, text);
        //发出广播
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_ACTION);
        intent.putExtra(KEY_NOTIFICATION_ID, notifiShowedRlt.getNotifactionId());
        intent.putExtra(KEY_MSG_ID,notifiShowedRlt.getMsgId());
        intent.putExtra(KEY_NOTIFICATION_TITLE, notifiShowedRlt.getTitle());
        intent.putExtra(KEY_NOTIFICATION_CONTENT,notifiShowedRlt.getContent());
        intent.putExtra(KEY_NOTIFICATION_ACTION_TYPE,notifiShowedRlt.getNotificationActionType());
        intent.putExtra(KEY_NOTIFICATION_ACTIVITY,notifiShowedRlt.getActivity());
        context.sendBroadcast(intent);
    }
}