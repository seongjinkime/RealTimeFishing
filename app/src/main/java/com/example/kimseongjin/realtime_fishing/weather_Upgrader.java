package com.example.kimseongjin.realtime_fishing;

import android.util.Log;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ChineseCalendar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kimseongjin on 2017. 5. 10..
 */

public class weather_Upgrader implements Interface_Collection.weather_Upgrader,Callback {

    private String BASE_OCEAN_DATA_URL = "";
    private String BASE_URL = "";
    private String APP_ID = "";
    private String OCEAN_SERVICE_KEY ="";
    private String GET_LEVEL_ONE_GRID_KEY = "";
    private String GET_OBS_INFO_KEY = "";
    private String GET_TIDE_PRE_INFO_KEY = "";
    //
    private static String GET_OCEAN_INFO_KEY = "";

    OkHttpClient client;
    Post tide_post, ocean_stastion_post;
    private double cLatitude, cLongtitude;
    private int command;
    String url_ForRequest;
    private ArrayList <String []> pre_TideInfo;
    private Weather_Data weatherData;
    private ArrayList<Interface_Collection.weather_Observer> observers;
    private boolean taskIsComplete = false;
    private String toDay;

    public weather_Upgrader(){
        client = new OkHttpClient();
        observers = new ArrayList<Interface_Collection.weather_Observer>();
        toDay = new SimpleDateFormat("yyyy-mm-dd").format(new Date());

    }
    @Override
    public void add_weather_Observer(Interface_Collection.weather_Observer observer) {
        if(observers==null){
            observers = new ArrayList<Interface_Collection.weather_Observer>();
        }
        observers.add(observer);
    }

    @Override
    public void remove_weather_Observer(Interface_Collection.weather_Observer observer) {
        try {
            observers.remove(observers.indexOf(observer));
        }catch (IndexOutOfBoundsException i){
            //i.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    public void excute_Upgrade(double latitude, double longtitude){
        this.cLatitude = latitude;
        this.cLongtitude = longtitude;
        command = 0;
        url_ForRequest = get_URL_Find_Grid(cLatitude, cLongtitude);
        taskIsComplete = false;
        request_JSON();
    }

    public void invalidateWeatherInfo(){
        if(weatherData==null ){
            Log.e("weather Upgrader Error","Ocean Station or WeatherData is NULL");
            return;
        }
        url_ForRequest = get_URL_Get_Weather_Info(cLatitude, cLongtitude);
        command = 3;
        taskIsComplete = false;
        request_JSON();
    }

    public void invalidateOceanInfo(){
        if(ocean_stastion_post==null || weatherData == null){
            Log.e("weather Upgrader Error","Ocean Station or WeatherData is NULL");
            return;
        }
        url_ForRequest = get_URL_Ocean_Info(ocean_stastion_post.getId());
        command=4;
        request_JSON();
    }

    private void request_JSON(){
        if(url_ForRequest!=null) {
            Request request = new Request.Builder()
                    .url(url_ForRequest)
                    .build();

            client.newCall(request).enqueue(this);
            url_ForRequest = null;
        }
    }

    public boolean isTaskComplete(){
        return this.taskIsComplete;
    }

    public Weather_Data getWeatherData(){
       return weatherData;
    }

    public ArrayList<String[]> get_preTide_Info(){
        return this.pre_TideInfo;
    }

    public Post get_TidePost(){
        return tide_post;
    }

    public Post get_OceanPost(){
        return ocean_stastion_post;
    }


    @Override
    public void onResponse(Call call, Response response) throws IOException {

        switch (command){
            case 0:
                String grid = response.body().string();
                Log.e("grid scopes", grid);
                String gridCode = Json_Parser.get_GridID(grid);
                if(gridCode == null){
                    return;
                }
                Log.e("grid code", gridCode);
                command=1;
                url_ForRequest = get_URL_Find_Obs(gridCode);
                request_JSON();
                grid = null;
                gridCode = null;
                break;

            case 1:
                String posts_Json = response.body().string();
                ArrayList<Post> posts;
                posts = Json_Parser.get_Posts(posts_Json);
                finding_nearest_OBS(posts);
                try{
                    url_ForRequest  = get_URL_Get_Tide_PRE_Info(tide_post.getId());
                }catch (NullPointerException e){
                    e.printStackTrace();
                    notify_UpgradeFail();
                    command = 99;
                    return;
                }
                command=2;
                request_JSON();
                posts_Json = null;
                break;

            case 2:
                String pre_data = response.body().string();
                pre_TideInfo = Json_Parser.get_Tide_Pre_Info(pre_data);
                url_ForRequest = get_URL_Get_Weather_Info(cLatitude, cLongtitude);
                command = 3;
                request_JSON();
                break;

            case 3:
                String pre_weather_data = response.body().string();
                weatherData = Json_Parser.get_Weather(pre_weather_data);
                url_ForRequest = get_URL_Ocean_Info(ocean_stastion_post.getId());
                command=4;
                request_JSON();
                break;

            case 4:
                String pre_Ocean_info = response.body().string();
                Json_Parser.get_Ocean_Info(pre_Ocean_info, weatherData);
                check_results();
                break;
        }

    }

    private String cal_LunaDate(String toDay){
        ChineseCalendar chinaCal = new ChineseCalendar();
        Calendar cal = Calendar.getInstance() ;


        cal.set(Calendar.YEAR, Integer.parseInt(toDay.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(toDay.substring(5, 7)) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(toDay.substring(8,10)));
        chinaCal.setTimeInMillis(cal.getTimeInMillis());


        int chinaYY = chinaCal.get(ChineseCalendar.EXTENDED_YEAR) - 2637 ;
        int chinaMM = chinaCal.get(ChineseCalendar.MONTH) + 1;
        int chinaDD = chinaCal.get(ChineseCalendar.DAY_OF_MONTH);




        String runaDate = "" ;     // 음력 날짜

        runaDate += chinaYY ;      // 년
        runaDate += "-" ;          // 연도 구분자


        if(chinaMM < 10)         // 월
            runaDate += "0" + Integer.toString(chinaMM) ;
        else
            runaDate += Integer.toString(chinaMM) ;


        runaDate += "-" ;          // 날짜 구분자


        if(chinaDD < 10)         // 일
            runaDate += "0" + Integer.toString(chinaDD) ;
        else
            runaDate += Integer.toString(chinaDD) ;

        return runaDate;

    }

    private String get_tide_time(String lunar_Date){
        String result = "";
        int originalnum = Integer.valueOf(lunar_Date.substring(9,10));
        int divide_num = originalnum % 15;
        int tide_num = divide_num + 7;
        if(tide_num==15){
            result = "조금";
            return result;
        }else if(tide_num>=16){
            tide_num = tide_num-15;
        }
        return String.valueOf(tide_num) +"물";

    }

    private void check_results(){

            Log.e("check", "check");
            weatherData.setLunar_Date(cal_LunaDate(toDay));
            weatherData.setTide_time(get_tide_time(weatherData.getLunar_Date()));
            weatherData.setPre_TideInfo(this.pre_TideInfo);
            weatherData.setOceanPostName(this.ocean_stastion_post.getName());
            weatherData.setPreTidePostName(this.tide_post.getName());
            taskIsComplete= true;
            notify_WeatherUpgrade();

    }

    @Override
    public void onFailure(Call call, IOException e) {

    }


    @Override
    public void notify_WeatherUpgrade() {

        if(weatherData==null){
            return;
        }
        if(observers.size()>0){
            for(Interface_Collection.weather_Observer obs : observers){
                obs.complete_Upgrade(weatherData);
            }
        }
    }

    @Override
    public void notify_UpgradeFail() {
        if(observers.size()>0){
            for(Interface_Collection.weather_Observer obs : observers){
                obs.fail_Upgrade();
            }
        }
    }


    private void finding_nearest_OBS(ArrayList<Post>posts){
        float tide_post_minimum_distance = -999.0f;
        float tidal_post_minimum_distance = -999.0f;
        float distance;
        int tide_result_index=-1;
        int ocean_stastion_post_result_index=-1;
        for(int i = 0 ; i< posts.size() ; i++ ){
            if(posts.get(i).getDatatype().equals("조위관측소")){
                Post p = posts.get(i);
                double lat = p.getLatitude();
                double lot = p.getLongtitude();
                distance = distance_Calculator (cLatitude, lat, cLongtitude, lot);
                if(Float.compare(tide_post_minimum_distance,-999.0f)==0){
                    tide_post_minimum_distance = distance;
                    tide_result_index = i;
                    continue;
                }
                if(Float.compare(distance, tide_post_minimum_distance) < 0 ){
                    tide_post_minimum_distance = distance;
                    tide_result_index = i;
                }
            }else if(posts.get(i).getDatatype().equals("해양관측부이")){
                distance = distance_Calculator (cLatitude, posts.get(i).getLatitude(), cLongtitude, posts.get(i).getLongtitude());
                if(Float.compare(tidal_post_minimum_distance,-999.0f)==0){
                    tidal_post_minimum_distance = distance;
                    ocean_stastion_post_result_index = i;
                    continue;
                }
                if(Float.compare(tidal_post_minimum_distance,-999.0f)<0){
                    tidal_post_minimum_distance = distance;
                    ocean_stastion_post_result_index = i;
                }
            }
        }
        try {
            register_posts(posts.get(tide_result_index), posts.get(ocean_stastion_post_result_index));
            posts.clear();
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }

        posts = null;
    }

    private void register_posts(Post tide_post, Post ocean_stastion_post){
        this.tide_post = tide_post;
        this.ocean_stastion_post = ocean_stastion_post;
        Log.e("register Reuslt", "tide post: " + tide_post.getName() +" tidal post: " + ocean_stastion_post.getName());
    }

    private float distance_Calculator(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }


    private String get_URL_Get_Weather_Info(double latitude, double longtiude){
        String Latitude = String.valueOf(latitude);
        String Longtitude = String.valueOf(longtiude);

        String URL = BASE_URL + "lat=" + Latitude + "&lon=" + Longtitude + "&APPID=" + APP_ID;
        Log.e("getWeather",URL);

        return URL;
    }
    private String get_URL_Find_Obs(String gridcode){
        String URL = GET_OBS_INFO_KEY + gridcode + "&ResultType=json" + "&ServiceKey=" + OCEAN_SERVICE_KEY;
        Log.e("getOceanData",URL);
        return URL;
    }

    private String get_URL_Get_Tide_PRE_Info(String obscode){
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String URL = GET_TIDE_PRE_INFO_KEY + obscode +"&Date=" + date + "&ResultType=json" + "&ServiceKey=" + OCEAN_SERVICE_KEY;
        Log.e("getOceanData",URL);
        return URL;
    }

    private String get_URL_Find_Grid(double latitude, double longtiude){
        String Latitude = String.valueOf(latitude);
        String Longtitude = String.valueOf(longtiude);
        String URL = GET_LEVEL_ONE_GRID_KEY + Longtitude +","+Latitude+"&ResultType=json";
        Log.e("getOceanData",URL);
        return URL;
    }


    private String get_URL_Ocean_Info(String obscode){
        //http://www.khoa.go.kr/oceangrid/grid/api/buObsRecent/search.do?ServiceKey=wldhxng34hkddbsgm81lwldhxng34hkddbsgm81l==&ObsCode=TW_0062&ResultType=json
        String URL = GET_OCEAN_INFO_KEY + OCEAN_SERVICE_KEY + "&ObsCode=" + obscode + "&ResultType=json";
        Log.e("getOceanData",URL);
        return URL;
    }



}

