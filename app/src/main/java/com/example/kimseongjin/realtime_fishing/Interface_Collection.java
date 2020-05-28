package com.example.kimseongjin.realtime_fishing;

import java.util.ArrayList;

/**
 * Created by kimseongjin on 2017. 5. 10..
 */

public class Interface_Collection {

    public interface time_Observer{
        public void time_update();
    }

    public interface time_Engine{
        public void add_Time_Observer(time_Observer time_observer);
        public void remove_Time_Observer(time_Observer time_observer);
        public void notifyTimeChange();
    }

    public interface weather_Observer{
        public void complete_Upgrade(Weather_Data weather_data);
        public void fail_Upgrade();
    }

    public interface weather_Upgrader{
        public void add_weather_Observer(weather_Observer observer);
        public void remove_weather_Observer(weather_Observer observer);
        public void notify_WeatherUpgrade();
        public void notify_UpgradeFail();
    }

    public interface FB_Cursor_Observer{
        public void get_queriedData(ArrayList<String> datas);
    }

    public interface FB_Cursor{
        public void add_cursor_observer(FB_Cursor_Observer observer);
        public void return_queried_data();
    }

    public interface DB_Observer{
        public void get_Data(String serialized_datam, int command);
    }

    public interface DB_Manager{
        public void pass_Data(String data, int command);
    }

    public interface animation_Observer{
        public void animationTheEnd();
    }

    public interface animation_Engine{
        public void addAnimObserver(animation_Observer observer);
        public void notifyAnimationTheEnd();
    }


}
