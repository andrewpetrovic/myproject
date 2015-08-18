package com.example.andrea.demotxpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.andrea.demotxpush.observer.IObserver;
import com.example.andrea.demotxpush.observer.ISubject;
import com.example.andrea.demotxpush.observer.ObserverMessage;
import com.itic.mobile.txpush.receiver.MessageReceiver;
import com.tencent.android.tpush.XGPushBaseReceiver;

import java.util.ArrayList;
import java.util.List;

public class SetDeleteTagReceiver extends BroadcastReceiver implements ISubject {

    private List<IObserver> observers = new ArrayList<IObserver>();

    /**
     * BroadcastReceiver接收txpushlib的MessageReciver发出的广播
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getIntExtra(MessageReceiver.KEY_ERROR_CODE, XGPushBaseReceiver.SUCCESS) == XGPushBaseReceiver.SUCCESS){
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_DELETE_TAG)){
                //删除tag成功
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_DELETE_TAG_SUCCESS);
                msg.setTagName(intent.getStringExtra(MessageReceiver.KEY_TAG_NAME));
                notifyObservers(msg);
            }if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_SET_TAG)){
                //设置tag成功
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_SET_TAG_SUCCESS);
                msg.setTagName(intent.getStringExtra(MessageReceiver.KEY_TAG_NAME));
                notifyObservers(msg);
            }
        }else{
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_DELETE_TAG)){
                //删除tag失败
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_DELETE_TAG_FAIL);
                msg.setTagName(intent.getStringExtra(MessageReceiver.KEY_TAG_NAME));
                notifyObservers(msg);
            }
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_SET_TAG)){
                //设置tag失败
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_SET_TAG_FAIL);
                msg.setTagName(intent.getStringExtra(MessageReceiver.KEY_TAG_NAME));
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
