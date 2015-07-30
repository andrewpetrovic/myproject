package com.itic.mobile.zfyj.qh.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.observer.IObserver;
import com.itic.mobile.zfyj.qh.observer.ISubject;
import com.itic.mobile.zfyj.qh.observer.ObserverMessage;
import com.itic.mobile.txpush.receiver.MessageReceiver;
import com.tencent.android.tpush.XGPushBaseReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JEEKR on 2015/1/28.
 */
public class SetDeleteTagReceiver extends BroadcastReceiver implements ISubject {

    private List<IObserver> observers = new ArrayList<IObserver>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getIntExtra(MessageReceiver.KEY_ERROR_CODE, XGPushBaseReceiver.SUCCESS) == XGPushBaseReceiver.SUCCESS){
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_DELETE_TAG)){
                //删除tag成功
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(Config.BROADCAST_DELETE_TAG_SUCCESS);
                notifyObservers(msg);
            }
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_SET_TAG)){
                //设置tag成功
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(Config.BROADCAST_SET_TAG_SUCCESS);
                notifyObservers(msg);
            }
        }else{
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_DELETE_TAG)){
                //删除tag失败
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(Config.BROADCAST_DELETE_TAG_FAIL);
                notifyObservers(msg);
            }
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_SET_TAG)){
                //设置tag失败
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(Config.BROADCAST_SET_TAG_FAIL);
                notifyObservers(msg);
            }
        }
    }

    @Override
    public void attachObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detachObserver(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(ObserverMessage message) {
        for (IObserver observer:observers){
            observer.handleObserverMessage(message);
        }
    }
}
