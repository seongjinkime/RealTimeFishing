package com.example.kimseongjin.realtime_fishing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.daum.mf.map.api.MapPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kimseongjin on 2017. 5. 28..
 */

public class FB_Cursor implements Interface_Collection.DB_Observer, Interface_Collection.FB_Cursor{
    DB_Manager db_manager;
    ArrayList<Interface_Collection.FB_Cursor_Observer> observers;
    Gson gson;
    private String mapPointListJSON, mapPolitemsJSON, weatherDataJSON, historyDataJson;

    public FB_Cursor(){
        gson = new Gson();
        observers = new ArrayList<Interface_Collection.FB_Cursor_Observer>();
    }
    public void selectAllDataFromFB_At(String date){
        if(db_manager!=null){
            db_manager = null;
        }
        db_manager = new DB_Manager(date);
        db_manager.setOutput(this);
        db_manager.queryData(0);

    }

    @Override
    public void add_cursor_observer(Interface_Collection.FB_Cursor_Observer observer) {
        if(observers!=null) {
            observers.add(observer);
        }
    }


    @Override
    public void get_Data(String data, int command) {
        switch (command){
            case 0:
                mapPointListJSON = data;
                db_manager.queryData(1);
                break;
            case 1:
                mapPolitemsJSON = data;
                db_manager.queryData(2);
                break;
            case 2:
                weatherDataJSON = data;
                db_manager.queryData(3);
                break;
            case 3:
                historyDataJson = data;
                return_queried_data();


        }
    }

    @Override
    public void return_queried_data() {
        ArrayList<String> datas = new ArrayList<>();
        try {
            datas.add(mapPointListJSON);
            datas.add(mapPolitemsJSON);
            datas.add(weatherDataJSON);
            datas.add(historyDataJson);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        for(Interface_Collection.FB_Cursor_Observer obs : observers){
            obs.get_queriedData(datas);
        }
    }
}
