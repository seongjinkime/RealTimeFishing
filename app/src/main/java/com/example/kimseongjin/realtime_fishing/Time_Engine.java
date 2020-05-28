package com.example.kimseongjin.realtime_fishing;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kimseongjin on 2017. 5. 10..
 */

public class Time_Engine implements Interface_Collection.time_Engine {
    private ArrayList<Interface_Collection.time_Observer> observers;
    private time_CounterMachine counterMachine;

    public Time_Engine(){
        observers = new ArrayList<Interface_Collection.time_Observer>();
        counterMachine = new time_CounterMachine();
        counterMachine.execute();
    }

    @Override
    public void add_Time_Observer(Interface_Collection.time_Observer time_observer) {
        observers.add(time_observer);
        Log.e("observer added", ""+observers.indexOf(time_observer));
        Log.e("observers Size:", ""+observers.size());
    }

    @Override
    public void remove_Time_Observer(Interface_Collection.time_Observer time_observer) {
        try {
            Log.e("observer Delete", ""+observers.indexOf(time_observer));
            observers.remove(observers.indexOf(time_observer));
            Log.e("observers Size:", ""+observers.size());
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }

    }

    @Override
    public void notifyTimeChange() {
        if(observers.size()>0) {
            for (Interface_Collection.time_Observer obs : observers) {
                obs.time_update();
            }
        }
    }


    private class time_CounterMachine extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {
                    Thread.sleep(1000);
                    notifyTimeChange();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
}
