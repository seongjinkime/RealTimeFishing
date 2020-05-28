package com.example.kimseongjin.realtime_fishing;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import net.daum.mf.map.api.MapPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kimseongjin on 2017. 5. 24..
 */
/*FB Hierarchy
                    realtime_fishing
                            |
                            |
           ----------------------------------
           |                |               |
           |                |               |
          user           mapPoints     fishingRecord
           |                |               |
           |                |               |
      -----------          UiD             UiD
      |    |    |           |               |
    email Name UiD         Date            Date
                            |               |
                            -               -
                            |               |
                         point list       Record
 */

public class DB_Manager implements Interface_Collection.DB_Manager{

    private DatabaseReference database;
    private FirebaseUser user;

    private Interface_Collection.DB_Observer output;
    private ValueEventListener eventListener;

    private final String userTable = "user";
    private final String pointTable = "mapPoints";
    private final String recordTable = "fishingRecord";
    private final String historyTable = "fishingHistory";
    private final String weatherTable = "weatherInformation";

    private String baseSentence;
    private String date;
    private int parsingCommand;

    private Gson gson;

    public DB_Manager(){
        database = FirebaseDatabase.getInstance().getReference();
        Log.e("DB Created", "Basic Mode");
    }

    public DB_Manager(String date){
        this.date = date.substring(0,10);
        Log.e("DbMAnagerDate", this.date);
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        baseSentence = "/"+user.getUid()+"/"+this.date;
        gson = new Gson();
        setEventListener();
        Log.e("DB Created", "Standard Mode");
    }

    public void setOutput(Interface_Collection.DB_Observer observer){
        this.output = observer;
    }

    private void setEventListener(){
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parsingData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void parsingData(DataSnapshot dataSnapshot){
        String data;
        switch (parsingCommand){ //0 is parsing mapPoints
            case 0:

                List<MapPoint.GeoCoordinate> pointList;
                List<List<MapPoint.GeoCoordinate>> list_container = new ArrayList<List<MapPoint.GeoCoordinate>>();
                int pointCount = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    pointList = (ArrayList<MapPoint.GeoCoordinate>)snapshot.getValue();
                    list_container.add(pointList);
                    pointList = null;
                }
                Collection<List<MapPoint.GeoCoordinate>> listCollection = list_container;
                data = gson.toJson(listCollection);
                pass_Data(data, 0);
                pointList = null;
                list_container = null;
                listCollection = null;
                data = null;

                break;
            case 1:
                List<FishingRecord> records = new ArrayList<FishingRecord>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String json = (String)((HashMap<String, Object>)snapshot.getValue()).get("Record");
                    records.add(gson.fromJson(json, FishingRecord.class));
                }

                Collection<FishingRecord> fishingRecordCollection = records;
                data = gson.toJson(fishingRecordCollection);
                pass_Data(data, 1);

                records = null;
                fishingRecordCollection = null;
                data = null;

                break;
            case 2:
                try {
                    HashMap<String, Object> originalData = (HashMap<String, Object>) dataSnapshot.getValue();
                    Weather_Data weather_data = gson.fromJson(((String) originalData.get("weatherData")), Weather_Data.class);
                    data = gson.toJson(weather_data);
                    pass_Data(data, 2);
                    originalData = null;
                    weather_data = null;

                }catch (NullPointerException e){
                    e.printStackTrace();
                    Log.e("JustCheck", "Check");
                    queryData(3);
                }
                break;

            case 3:
                HashMap<String, Object> historyHash = (HashMap<String, Object>)dataSnapshot.getChildren().iterator().next().getValue();
                data = gson.toJson(historyHash);
                pass_Data(data, 3);

                historyHash = null;
                break;
        }
        System.gc();
    }

    public void queryData(int tableType){

        String key = null;
        DatabaseReference reference = null;
        Query query;

        switch (tableType){
            //0 is query mapPoints & 1 is query fishingRecord
            case 0:
                key = pointTable+baseSentence;
                break;
            case 1:
                key = recordTable+baseSentence;
                break;
            case 2:
                key = weatherTable+baseSentence;
                break;
            case 3:
                key = historyTable+baseSentence;
                break;

        }
        try {
            reference = database.child(key);
            query = reference.orderByKey();
            query.addValueEventListener(eventListener);
            parsingCommand = tableType;
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }
    public void checkUser(final FirebaseAuth auth, final String name){
        DatabaseReference reference = database.child(userTable);
        Query query = reference.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            HashMap<String, Object> users;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    users = (HashMap<String, Object>)snapshot.getValue();
                    if (auth.getCurrentUser().getUid().equals(users.get("Uid"))){
                        return;
                    }
                }
                registerNewUser(auth, name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    public void registerNewUser(FirebaseAuth auth, String name){

        final String hierarchy = userTable;
        final String key_name = "Name";
        final String key_email = "Email";
        final String key_uid = "Uid";
        HashMap<String, Object> newUser = new HashMap<>();
        if (name == null) {
            newUser.put(key_name, auth.getCurrentUser().getDisplayName());
        }else {
            newUser.put(key_name, name);
        }
        newUser.put(key_email, auth.getCurrentUser().getEmail());
        newUser.put(key_uid, auth.getCurrentUser().getUid());

        Log.e("DB Manager", "FB Register New User");

        database.child(hierarchy).push().setValue(newUser);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cleanDB();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        newUser = null;
    }

    public void uploadMapPoints(ArrayList<MapPoint.GeoCoordinate> pointList){
        if(user==null || pointList == null){
            Log.e("DB Manager", "FB Has Error or pointList is NULL");
            return;
        }

        Log.e("DB Manager", "FB Upload point List");

        final String hierarchy = pointTable + baseSentence;
        database.child(hierarchy).push().setValue(pointList);
        pointList.clear();
        pointList = null;
        System.gc();
    }

    public void uploadFishingHistory(int totalFishNum, String startDate, String endDate, ArrayList<String[]> courseData){
        try {
            Log.e("DB Manager", "FB Upload fishing record");
            final String hierarchy = historyTable + baseSentence;
            HashMap<String, Object> newHistory = new HashMap<>();
            newHistory.put("totalFishNum", totalFishNum);
            newHistory.put("startDate", startDate);
            newHistory.put("endDate", endDate);
            newHistory.put("courseData", gson.toJson(courseData));
            database.child(hierarchy).push().setValue(newHistory);
        }catch (NullPointerException e){
            e.printStackTrace();
            return;
        }

    }

    public void uploadWeatherInformation(Weather_Data data){
        if(user==null || data == null){
            Log.e("DB Manager", "FB has Error or record is NULL");
            return;
        }
        Log.e("DB Manager", "FB Upload fishing record");
        final String hierarchy = weatherTable + baseSentence;
        HashMap<String, Object> weatherInfo = new HashMap<>();
        weatherInfo.put("weatherData", gson.toJson(data));
        database.child(hierarchy).setValue(weatherInfo);
        data = null;
        weatherInfo = null;
    }

    public void uploadFishingRecord(FishingRecord record){
        if(user==null || record == null){
            Log.e("DB Manager", "FB has Error or record is NULL");
            return;
        }

        Log.e("DB Manager", "FB Upload fishing record");
        final String hierarchy = recordTable + baseSentence;
        HashMap<String, Object> newRecord = new HashMap<>();
        newRecord.put("Record", gson.toJson(record));
        database.child(hierarchy).push().setValue(newRecord);
        record = null;
        newRecord = null;
    }



    public void cleanDB(){
        database = null;
        if(eventListener!=null && user!=null) {
            eventListener = null;
            user = null;
        }
        System.gc();
        Log.e("DB MAnager", "Cleaning DB");
    }


    @Override
    public void pass_Data(String data, int command) {
        if(output!=null)

            output.get_Data(data, command);
    }


}
