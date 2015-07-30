package com.itic.mobile.txpush.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * XGPushBaseReceiver类提供透传消息的接收和操作结果的反馈
 */
public class MessageReceiver extends XGPushBaseReceiver {

    public static final String LogTag = "MessageReceiver";

    public static final String REGISTER_ACTION = "com.itic.mobile.xg.push.broadcast.REGISTER";
    public static final String TAG_ACTION = "com.itic.mobile.xg.push.broadcast.TAG";
    public static final String NOTIFICATION_ACTION = "com.itic.mobile.xg.push.broadcast.NOTIFICATION";
    public static final String MESSAGE_ACTION = "com.itic.mobile.xg.push.broadcast.MESSAGE";

    public static final String KEY_ERROR_CODE = "KEY_ERROR_CODE";
    public static final String KEY_TAG = "KEY_TAG";
    public static final String KEY_SET_TAG = "KEY_SET_TAG";
    public static final String KEY_DELETE_TAG = "KEY_DELETE_TAG";

    /**
     * 注册结果
     * @param context 当前app的context
     * @param errorCode 返回码， 0代表调用成功，其他编码请产考 http://developer.xg.qq.com/index.php/%E8%BF%94%E5%9B%9E%E7%A0%81%E6%8F%8F%E8%BF%B0
     * @param message 服务器返回的消息
     */
    @Override
    public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult message) {

    }

    /**
     * 反注册结果
     * @param context 当前app的context
     * @param errorCode 返回码， 0代表调用成功
     */
    @Override
    public void onUnregisterResult(Context context, int errorCode) {

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
        Intent intent = new Intent();
        intent.setAction(TAG_ACTION);
        intent.putExtra(KEY_ERROR_CODE,errorCode);
        intent.putExtra(KEY_TAG,KEY_SET_TAG);
        context.sendBroadcast(intent);
        Log.d(LogTag, text);
    }

    /**
     * Tag删除结果
     * @param context
     * @param errorCode
     * @param tagName
     */
    @Override
    public void onDeleteTagResult(Context context, int errorCode, String tagName) {
        Log.i(LogTag,"onDeleteTagResult is called");
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "\"" + tagName + "\"删除成功";
        } else {
            text = "\"" + tagName + "\"删除 失败,错误码：" + errorCode;
        }
        Intent intent = new Intent();
        intent.setAction(TAG_ACTION);
        intent.putExtra(KEY_ERROR_CODE,errorCode);
        intent.putExtra(KEY_TAG,KEY_DELETE_TAG);
        context.sendBroadcast(intent);
        Log.d(LogTag, text);
    }

    /**
     * 接收透传消息
     * @param context app上下文对象
     * @param message 收到的透传消息
     */
    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        if (context == null || message == null){
            return;
        }
        // 获取自定义key-value
        String customContent = message.getCustomContent();
        if (customContent != null && customContent.length() != 0) {
            try {
                JSONObject obj = new JSONObject(customContent);
                // key1为前台配置的key
                if (!obj.isNull("key")) {
                    String value = obj.getString("key");
                    Log.d(LogTag, "get custom value:" + value);
                }
                // ...
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // APP自主处理消息的过程...
        Log.d(LogTag, "Receive msg:" + message.toString());
    }

    /**
     *
     * @param context app上下文对象
     * @param notifiClickedRlt 被点击的通知对象，提供读取被点击的通知内容的接口
     */
    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult notifiClickedRlt) {
        if (context == null || notifiClickedRlt == null) {
            return;
        }
        String text = "";
        if (notifiClickedRlt.getActionType() == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
            // 通知在通知栏被点击
            // APP自己处理点击的相关动作
            // 这个动作可以在activity的onResume也能监听，请看第3点相关内容
            text = "通知被打开 :" + notifiClickedRlt;
        } else if (notifiClickedRlt.getActionType() == XGPushClickedResult.NOTIFACTION_DELETED_TYPE) {
            // 通知被清除
            // APP自己处理通知被清除后的相关动作
            text = "通知被清除 :" + notifiClickedRlt;
        }
        Toast.makeText(context, "广播接收到通知被点击:" + notifiClickedRlt.toString(),
                Toast.LENGTH_SHORT).show();
        // 获取自定义key-value
        String customContent = notifiClickedRlt.getCustomContent();
        if (customContent != null && customContent.length() != 0) {
            try {
                JSONObject obj = new JSONObject(customContent);
                // key1为前台配置的key
                if (!obj.isNull("key")) {
                    String value = obj.getString("key");
                    Log.d(LogTag, "get custom value:" + value);
                }
                // ...
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // APP自主处理的过程。。。
        Log.d(LogTag, text);
    }

    /**
     * 获取被展示通知
     * @param context app上下文对象
     * @param notifiShowedRlt 被显示的通知对象，提供读取被显示的通知内容的接口
     */
    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult notifiShowedRlt) {
        if(context == null || notifiShowedRlt == null){
            return;
        }
        String text = "通知被展示，title:" + notifiShowedRlt.getTitle() + ",content:" + notifiShowedRlt.getContent() + ",custom_content:" + notifiShowedRlt.getCustomContent();
        Log.d(LogTag,text);
    }
}
