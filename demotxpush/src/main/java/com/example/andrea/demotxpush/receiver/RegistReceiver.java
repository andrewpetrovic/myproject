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

public class RegistReceiver extends BroadcastReceiver implements ISubject{

    List<IObserver> list = new ArrayList<IObserver>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getIntExtra(MessageReceiver.KEY_ERROR_CODE, XGPushBaseReceiver.SUCCESS) == XGPushBaseReceiver.SUCCESS){
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_REGISTE)){
                //注册成功
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_REGIST_SUCCESS);
                msg.setToken(intent.getStringExtra(MessageReceiver.KEY_TOKEN));
                msg.setAccessId(intent.getLongExtra(MessageReceiver.KEY_TOKEN, 0));
                msg.setAccount(intent.getStringExtra(MessageReceiver.KEY_ACCOUNT));
                msg.setTicket(intent.getStringExtra(MessageReceiver.KEY_TICKET));
                msg.setTicketType(intent.getShortExtra(MessageReceiver.KEY_TICKET_TYPE, Short.valueOf(String.valueOf(0))));
                notifyObservers(msg);
            }if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_UNREGISTE)){
                //反注册成功
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_UNREGIST_SUCCESS);
                notifyObservers(msg);
            }
        }else {
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_REGISTE)){
                //注册失败
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_REGIST_SUCCESS);
                msg.setToken(intent.getStringExtra(MessageReceiver.KEY_TOKEN));
                msg.setAccessId(intent.getLongExtra(MessageReceiver.KEY_TOKEN, 0));
                msg.setAccount(intent.getStringExtra(MessageReceiver.KEY_ACCOUNT));
                msg.setTicket(intent.getStringExtra(MessageReceiver.KEY_TICKET));
                msg.setTicketType(intent.getShortExtra(MessageReceiver.KEY_TICKET_TYPE, Short.valueOf(String.valueOf(0))));
                notifyObservers(msg);
            }
            if (intent.getStringExtra(MessageReceiver.KEY_TAG).equals(MessageReceiver.KEY_UNREGISTE)){
                //反注册失败
                ObserverMessage msg = new ObserverMessage();
                msg.setMessageType(ObserverMessage.BROADCAST_UNREGIST_FAIL);
                notifyObservers(msg);
            }
        }
    }

    @Override
    public void attachObserver(IObserver observer) {
        list.add(observer);
    }

    @Override
    public void detachObserver(IObserver observer) {
        list.remove(observer);
    }

    @Override
    public void notifyObservers(ObserverMessage message) {
        for (IObserver observer: list){
            observer.handleObserverMessage(message);
        }
    }
}
