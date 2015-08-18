package com.example.andrea.demotxpush.observer;

/**
 * Created by JEEKR on 2015/1/29.
 */
public interface ISubject {
    public void attachObserver(IObserver observer);
    public void detachObserver(IObserver observer);
    public void notifyObservers(ObserverMessage message);
}
