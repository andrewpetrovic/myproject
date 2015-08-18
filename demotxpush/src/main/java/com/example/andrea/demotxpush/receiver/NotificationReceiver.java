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

public class NotificationReceiver extends BroadcastReceiver implements ISubject {

    List<IObserver> list = new ArrayList<IObserver>();

    @Override
    public void onReceive(Context context, Intent intent) {
        ObserverMessage msg = new ObserverMessage();
        msg.setMsgId(intent.getLongExtra(MessageReceiver.KEY_MSG_ID, 0));
        msg.setNotificationId(intent.getIntExtra(MessageReceiver.KEY_NOTIFICATION_ID, 0));
        msg.setTitle(intent.getStringExtra(MessageReceiver.KEY_NOTIFICATION_TITLE));
        msg.setContent(intent.getStringExtra(MessageReceiver.KEY_NOTIFICATION_CONTENT));
        msg.setNotificationActionType(intent.getIntExtra(MessageReceiver.KEY_NOTIFICATION_ACTION_TYPE, 0));
        msg.setNotificationActivity(intent.getStringExtra(MessageReceiver.KEY_NOTIFICATION_ACTIVITY));
        msg.setMessageType(ObserverMessage.BROADCAST_NOTIFICATION);
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
