package com.example.andrea.demotxpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.andrea.demotxpush.observer.IObserver;
import com.example.andrea.demotxpush.observer.ISubject;
import com.example.andrea.demotxpush.observer.ObserverMessage;
import com.itic.mobile.txpush.receiver.MessageReceiver;

import java.util.ArrayList;
import java.util.List;

public class MsgReceiver extends BroadcastReceiver implements ISubject {

    List<IObserver> list = new ArrayList<IObserver>();

    @Override
    public void onReceive(Context context, Intent intent) {
        ObserverMessage msg = new ObserverMessage();
//        msg.setTitle(intent.getStringExtra(MessageReceiver.KEY_MSG_TITLE));
        msg.setContent(intent.getStringExtra(MessageReceiver.KEY_MSG_CONTENT));
        msg.setMessageType(ObserverMessage.BROADCAST_MSG);
        notifyObservers(msg);
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
        for (IObserver observer:list){
            observer.handleObserverMessage(message);
        }
    }
}
