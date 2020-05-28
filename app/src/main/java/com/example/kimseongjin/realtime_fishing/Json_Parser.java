package com.example.kimseongjin.realtime_fishing;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kimseongjin on 2017. 5. 10..
 */

public class Json_Parser {

    public static Weather_Data get_Weather(String data){

        Weather_Data weather = new Weather_Data();

        try {
            JSONObject jObj = new JSONObject(data);
            JSONArray jArr = jObj.getJSONArray("weather");

            JSONObject JSONWeather = jArr.getJSONObject(0);
            weather.setIcon(getString("icon", JSONWeather));

            JSONObject mainObj = getObject("main", jObj);
            weather.setHumidity(getInt("humidity", mainObj));
            weather.setTemp(getFloat("temp", mainObj));

            JSONObject wObj = getObject("wind", jObj);
            weather.setWindSpeed(getFloat("speed", wObj));
            weather.setWindDeg(getFloat("deg", wObj));

            jObj= null;
            jArr = null;
            mainObj = null;
            wObj = null;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weather;

    }

    /*
    public static Ocean_Data get_OceanData(String[] datas){
        JSONObject jsonObject;
        JSONObject result;
        JSONArray response_array;
        Ocean_Data ocean_data = new Ocean_Data();
        try {

            for(int i = 0 ; i<datas.length-1; i++){
                jsonObject = new JSONObject(datas[i]);
                result = jsonObject.getJSONObject("result");
                response_array = result.getJSONArray("data");
                if(i==0){
                    ocean_data.setTidal_Deg(getFloat("current_direct", (JSONObject)response_array.get((response_array.length()-1))));
                    ocean_data.setTidal_Speed(getFloat("current_speed", (JSONObject)response_array.get((response_array.length()-1))));
                }else if(i==1){
                    ocean_data.setWater_temp(getFloat("water_temp", (JSONObject)response_array.get((response_array.length()-1))));
                }
            }



        }catch (JSONException e){
            e.printStackTrace();
        }

        return ocean_data;
    }*/

    public static String get_GridID(String data){
        JSONObject jsonObject;
        JSONObject result;
        JSONArray response_array;
        JSONArray values_array;
        String id = null;

        try {
            jsonObject = new JSONObject(data);
            result = jsonObject.getJSONObject("result");
            response_array = result.getJSONArray("data");
            id = getString("og_id", (JSONObject)response_array.get(0));
            Log.e("get_GridID", id);

            jsonObject = null;
            result = null;
            response_array = null;
            values_array = null;

            return id;


        }catch (JSONException e1) {
            e1.printStackTrace();
        }

        return id;
    }

    public static ArrayList<Post> get_Posts(String data){
        JSONObject jsonObject;
        JSONObject result;
        JSONArray response_array;
        JSONArray values_array;
        ArrayList<Post> posts = new ArrayList<Post>();

        try {
            jsonObject = new JSONObject(data);
            result = jsonObject.getJSONObject("result");
            response_array = result.getJSONArray("data");

            for(int i = 0 ; i< response_array.length() ; i++){
                Post tmp_post = new Post();
                tmp_post.setId((getString("obs_post_id", (JSONObject)response_array.get(i))));
                tmp_post.setLatitude((getDouble("obs_lat", (JSONObject)response_array.get(i))));
                tmp_post.setLatitude((getDouble("obs_lon", (JSONObject)response_array.get(i))));
                tmp_post.setDatatype((getString("data_type", (JSONObject)response_array.get(i))));
                tmp_post.setName((getString("obs_post_name", (JSONObject)response_array.get(i))));
                posts.add(tmp_post);
                tmp_post = null;
            }
            Log.e("get_posts", ""+posts.size());
            jsonObject = null;
            result = null;
            response_array = null;
            values_array = null;

            return posts;

        }catch (JSONException e1) {
            e1.printStackTrace();
        }

        return posts;


    }

    public static ArrayList<String[]> get_Tide_Pre_Info(String data){
        JSONObject jsonObject;
        JSONObject result;
        JSONArray response_array;
        ArrayList<String[]> result_array  = new ArrayList<String[]>();
        //*만조시간 간조시간 물때 음력 풍향 풍속

        try {
            jsonObject = new JSONObject(data);
            result = jsonObject.getJSONObject("result");
            response_array = result.getJSONArray("data");

            for(int i = 0 ; i< response_array.length() ; i++){
                String [] tmp = new String[3];
                tmp[0] = (getString("hl_code", (JSONObject)response_array.get(i)));
                tmp[1] = ((getString("tph_time", (JSONObject)response_array.get(i))).substring(11, 16));
                tmp[2] = (String.valueOf(getInt("tph_level", (JSONObject)response_array.get(i))));
                result_array.add(tmp);
                tmp = null;
            }

            jsonObject = null;
            result = null;
            response_array = null;

            return result_array;
        }catch (JSONException e1) {
            e1.printStackTrace();
        }

        return result_array;


    }

    public static void get_Ocean_Info(String data, Weather_Data weather_data){
        JSONObject jsonObject;
        JSONObject result;
        JSONArray response_array;

        JSONObject json_ocean_data;
        //*만조시간 간조시간 물때 음력 풍향 풍속

        try {
            jsonObject = new JSONObject(data);
            result = jsonObject.getJSONObject("result");
            json_ocean_data = result.getJSONObject("data");

            weather_data.setWaterTemp (getFloat("water_temp", json_ocean_data));
            weather_data.setTidalSpeed (getFloat("current_speed",  json_ocean_data));
            weather_data.setTidalDeg(getFloat("current_dir",  json_ocean_data));

            jsonObject=null;
            result = null;
            response_array = null;
            json_ocean_data = null;



        }catch (JSONException e1) {
            e1.printStackTrace();
        }



    }

    private static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static Double getDouble(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getDouble(tagName);
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }
}
