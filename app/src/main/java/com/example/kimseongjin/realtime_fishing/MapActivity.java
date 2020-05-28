package com.example.kimseongjin.realtime_fishing;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import net.daum.mf.map.api.CameraUpdate;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by kimseongjin on 2017. 5. 9..
 */

public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
                                                                GoogleApiClient.OnConnectionFailedListener,
                                                                LocationListener, Interface_Collection.time_Observer, Interface_Collection.weather_Observer,MapView.OpenAPIKeyAuthenticationResultListener,
                                                                MapView.MapViewEventListener,MapView.POIItemEventListener, Interface_Collection.DB_Observer, Interface_Collection.animation_Observer,MapReverseGeoCoder.ReverseGeoCodingResultListener

{

    private final int TAG_RecordMarker = 1;
    private final int TAG_DestinationMarker = 2;
    private final int ChangeLifeCycle = -99;

    private GoogleApiClient googleApiClient;
    private Gson gson;

    private LocationRequest locationRequest;
    private weather_Upgrader weather_upgrader;
    private DB_Manager db_manager;

    private int count_TimeNum;
    private String start_Date;
    private boolean timeIsUP=false;

    private MapView mapView;
    private MapReverseGeoCoder mapReverseGeoCoder;
    private MapPolyline polyLine;
    private MapPolyline destination_Guideline;

    private MapPoint point;
    private boolean isFirstTime=true;
    private int map_command;
    private MapPOIItem boatMarker;
    MapPOIItem hook_marker;
    private MapPOIItem destination_Marker=null;
    private ArrayList<MapPoint.GeoCoordinate> pointList;

    private TextView tv_kph, tv_knot;
    private ImageView iv_boat_status;
    private LinearLayout info_Container;
    private UIelementSet uIelementSet;
    private FloatingActionButton fab_hook;


    private double pre_lat = -99 , pre_lot = -99;
    private boolean add_position;

    private ImageView reel;

    private ViewGroup mapViewContainer;

    private ProgressDialog loadingDialog;

    private WeatherInfoFragment weatherInfoFragment;
    private FishingPanelFragment fishingPanelFragment;
    private CourseLogFragment courseLogFragment;
    private FishingRecordFragment fishingRecordFragment;

    private PopupWindow dynamicDialog;

    private ArrayList<MapPOIItem> fishNumRecords;
    Time_Engine time_engine;
    private boolean infoToggled = false;
    private boolean notifyWeatherAtFirst = false;
    private ArrayList<String[]> courseLogs;
    int totalFishNum;
    private boolean realTimeMode;
    private boolean polItemTouched = false;
    private int pointCount;
    ArrayList<Fragment>fragmentsContainer;
    ArrayList<String>pagetitles_Container;


    @Override
    public void onBackPressed() {
        if(realTimeMode){
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.realtime_map);
        setMapView();
        polyLine = new MapPolyline();
        polyLine.setLineColor(Color.RED);
        fab_hook = (FloatingActionButton)findViewById(R.id.fab_hook);
        mapViewContainer = (ViewGroup)findViewById(R.id.map_View_Container);
        mapViewContainer.addView(mapView);
        gson = new Gson();
        setInfoUI();
        fishNumRecords = new ArrayList<MapPOIItem>();
        map_command = -1;
        pointCount = 0;
        Bundle preloadData = null;
        if(getIntent()!=null){
            preloadData = getIntent().getExtras();
        }
        if (preloadData.getBoolean("isRealTimeMode")) {
            pointList = new ArrayList<MapPoint.GeoCoordinate>();

            courseLogs = new ArrayList<String[]>();
            this.totalFishNum = 0;
            reel = (ImageView)findViewById(R.id.iv_reel);
            reel.setScaleType(ImageView.ScaleType.MATRIX);
            mapViewContainer.bringChildToFront(fab_hook);
            start_Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            time_engine = new Time_Engine();
            time_engine.add_Time_Observer(this);
            weather_upgrader = new weather_Upgrader();
            weather_upgrader.add_weather_Observer(this);
            setDb_manager();
            realTimeMode = true;

        }else {
            //
            fab_hook.hide();

            //weatherInfoFragment = new WeatherInfoFragment();
            realTimeMode = false;
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(realTimeMode) {
            getMenuInflater().inflate(R.menu.map_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.complete_fising:
                showDynamicDialog(1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMapView(){
        mapView = new MapView(this);
        mapView.setDaumMapApiKey(getString(R.string.access_token));
        mapView.setOpenAPIKeyAuthenticationResultListener(this);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);

    }

    private void setInfoUI(){
        uIelementSet = new UIelementSet();
        uIelementSet.addAnimObserver(this);
        info_Container = (LinearLayout)this.findViewById(R.id.info_Container);
        uIelementSet.setNestedInfoLayout(info_Container, getSupportFragmentManager());
        iv_boat_status = (ImageView)this.findViewById(R.id.iv_boat_status);
        tv_kph = (TextView)this.findViewById(R.id.tv_kph);
        tv_knot = (TextView)this.findViewById(R.id.tv_knot);
    }

    private void setDb_manager(){
        db_manager = new DB_Manager(start_Date.substring(0,10));
        db_manager.setOutput(this);
    }

    private void loadFragments(){
        if(!realTimeMode){
            return;
        }
        Bundle panelBundle = new Bundle();
        panelBundle.putString("startTime", start_Date.substring(11, 19));
        if(fishNumRecords!=null && fishNumRecords.size()>0){
            try {
                int[] tmp_fishNumArray = new int[fishNumRecords.size()];
                for(int i = 0 ; i<fishNumRecords.size() ; i++){
                    tmp_fishNumArray[i] = Integer.valueOf(fishNumRecords.get(i).getItemName());
                }
                panelBundle.putBoolean("drawChart", true);
                panelBundle.putString("fishNumsData", gson.toJson(tmp_fishNumArray));
                tmp_fishNumArray = null;
            }catch (IndexOutOfBoundsException e){
                Toast.makeText(this, "데이터 변환 오류", Toast.LENGTH_SHORT).show();
            }
        }else {
            panelBundle.putBoolean("drawChart", false);
        }
        this.fishingPanelFragment = new FishingPanelFragment();
        this.fishingPanelFragment.setArguments(panelBundle);
        time_engine.add_Time_Observer(this.fishingPanelFragment);

        this.weatherInfoFragment = new WeatherInfoFragment();
        Bundle weatherBundle = new Bundle();
        weatherBundle.putString("weatherData", gson.toJson(weather_upgrader.getWeatherData()));
        weatherInfoFragment.setArguments(weatherBundle);
        weatherBundle = null;



        Bundle courseBundle = new Bundle();
        courseBundle.putString("courseData", gson.toJson(courseLogs));
        this.courseLogFragment = new CourseLogFragment();
        courseLogFragment.setArguments(courseBundle);

        panelBundle = null;
        courseBundle = null;
        System.gc();
    }

    @Override
    public void complete_Upgrade(final Weather_Data weather_data) {
        Log.e("test", "upgrade completed");
        if(!notifyWeatherAtFirst) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bundle weatherBundle = new Bundle();
                    weatherBundle.putString("weatherData", gson.toJson(weather_data));
                    showDynamicDialog(0);
                    loadSingleInfo(0, weatherBundle);
                    notifyWeatherAtFirst = true;
                }
            });
        }
        db_manager.uploadWeatherInformation(weather_data);

    }

    @Override
    public void fail_Upgrade() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapActivity.this, "날씨 정보를 얻어오지 못했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFishingHistory(){
        try {
            db_manager.uploadMapPoints(pointList);
            pointList.clear();
            System.gc();
            db_manager.uploadFishingHistory(totalFishNum, start_Date, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), this.courseLogs);
        }catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(this, "정보를 저장하지 못했습니다. 죄송합니다", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(dynamicDialog!=null){
            dynamicDialog.dismiss();
        }
        Toast.makeText(getApplicationContext(), "정보를 성공적으로 저장 하였습니다.", Toast.LENGTH_SHORT).show();
        destroyMapElements();
        startActivity(new Intent(this, MainActivity.class));
        finish();


    }



    private void destroyMapElements(){
        toggleGps(false);
        try {
            mapView.removeAllPOIItems();
            mapView.removeAllPolylines();
            courseLogs.clear();
            courseLogs = null;
            boatMarker = null;
            destination_Marker = null;
            polyLine = null;
            destination_Guideline =null;
            destination_Marker = null;

            mapView.releaseUnusedMapTileImageResources();
            mapView.onSurfaceDestroyed();
            mapView.destroyDrawingCache();
            System.gc();
        }catch (NullPointerException e){

        }

    }



    private void showDynamicDialog(int type){
        dynamicDialog = new PopupWindow(this);

        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout mainLayout = new LinearLayout(this);
        View popupView = layoutInflater.inflate(R.layout.dynamic_dialog, null);
        dynamicDialog.setContentView(popupView);
        dynamicDialog.setBackgroundDrawable(null);
        dynamicDialog.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        dynamicDialog.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

        dynamicDialog.setOutsideTouchable(false);
        dynamicDialog.setTouchable(true);

        switch (type){
            case 0:
                dynamicDialog.setAnimationStyle(R.style.WeatherAnimation);
                Glide.with(this).load(R.drawable.dialog_weather_icon).into(((ImageView)popupView.findViewById(R.id.iv_dynamic_dialog_icon)));
                ((TextView)popupView.findViewById(R.id.tv_dynamic_dialog_msg)).setText("날씨 정보가 도착했습니다!\n확인하시겠습니까?");
                ((ImageButton)popupView.findViewById(R.id.btn_dynamic_dialog_ok)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(dynamicDialog!=null){
                            dynamicDialog.dismiss();
                        }
                        openInfoContainer();
                    }
                });

                ((ImageButton)popupView.findViewById(R.id.btn_dynamic_dialog_cancle)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(dynamicDialog!=null){
                            dynamicDialog.dismiss();
                        }
                        animationTheEnd();
                    }
                });

                break;
            case 1:
                dynamicDialog.setAnimationStyle(R.style.QuitAnimation);
                Glide.with(this).load(R.drawable.dialog_quit_icon).into(((ImageView)popupView.findViewById(R.id.iv_dynamic_dialog_icon)));
                ((TextView)popupView.findViewById(R.id.tv_dynamic_dialog_msg)).setText("낚시를 정말로 종료하시겠습니까?\n(정보 저장을 멈춥니다)");
                ((ImageButton)popupView.findViewById(R.id.btn_dynamic_dialog_ok)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveFishingHistory();
                    }
                });
                ((ImageButton)popupView.findViewById(R.id.btn_dynamic_dialog_cancle)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(dynamicDialog!=null){
                            dynamicDialog.dismiss();
                        }
                    }
                });
                break;
        }
        dynamicDialog.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
    }


    private void loadDefaultInfos(){


        if(uIelementSet!=null) {
            if(realTimeMode){
                loadFragments();
            }else {
                addFBInfoFragment(this.getIntent().getStringExtra("mapWeatherData"), this.getIntent().getStringExtra("mapHistoryData"));
            }
            loadFragments();
            if(fragmentsContainer!=null || pagetitles_Container!=null){
                fragmentsContainer.clear();
                pagetitles_Container.clear();
                fragmentsContainer = null;
                pagetitles_Container = null;
            }

            fragmentsContainer = new ArrayList<>();
            pagetitles_Container = new ArrayList<>();

            try {
                fragmentsContainer.add(weatherInfoFragment);
                pagetitles_Container.add("날씨");
                fragmentsContainer.add(fishingPanelFragment);
                pagetitles_Container.add("조황");
                fragmentsContainer.add(courseLogFragment);
                pagetitles_Container.add("경로");
                Log.e("numOfPageInLoading: ", ""+fragmentsContainer.size());
                uIelementSet.setFragments(fragmentsContainer, pagetitles_Container);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSingleInfo(int type, Bundle bundle){
        if(fragmentsContainer!=null || pagetitles_Container!=null){
            fragmentsContainer.clear();
            pagetitles_Container.clear();
            fragmentsContainer = null;
            pagetitles_Container = null;
        }

        fragmentsContainer = new ArrayList<>();
        pagetitles_Container = new ArrayList<>();
        switch (type){
            case 0:
                this.weatherInfoFragment = new WeatherInfoFragment();
                this.weatherInfoFragment.setArguments(bundle);
                pagetitles_Container.add("날씨");
                fragmentsContainer.add(this.weatherInfoFragment);
                break;

            case 1:
                this.fishingRecordFragment = new FishingRecordFragment();
                this.fishingRecordFragment.setArguments(bundle);
                pagetitles_Container.add("포인트");
                fragmentsContainer.add(this.fishingRecordFragment);
                break;
        }

        try {
            uIelementSet.setFragments(fragmentsContainer, pagetitles_Container);

        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    private void openInfoContainer(){
        uIelementSet.toogle_InfoFloating(info_Container);
        infoToggled = true;
    }
    @Override
    public void animationTheEnd() {

        if(infoToggled){
            return;
        }

        if(this.fishingPanelFragment!=null) {
            this.getSupportFragmentManager().beginTransaction().remove(this.fishingPanelFragment).commit();
            Log.e("AnimationEnd", "fishingPanel Deleted");
            //this.fishingPanelFragment = null;
        }

        if(this.weatherInfoFragment!=null) {
            this.getSupportFragmentManager().beginTransaction().remove(this.weatherInfoFragment).commit();
            Log.e("AnimationEnd", "weatherPanel Deleted");
            //this.weatherInfoFragment = null;
        }

        if(this.courseLogFragment!=null){
            this.getSupportFragmentManager().beginTransaction().remove(this.courseLogFragment).commit();
            Log.e("AnimationEnd", "coursePanel Deleted");
            //this.courseLogFragment = null;
        }

        if(this.fishingRecordFragment!=null){
            this.getSupportFragmentManager().beginTransaction().remove(this.fishingRecordFragment).commit();
            Log.e("AnimationEnd", "recordPanel Deleted");
            //this.fishingRecordFragment = null;
        }
        System.gc();
        Log.e("test", "check");
    }

    public void onMapClick(View v){
        switch (v.getId()){
            case R.id.toggle_InfoFloating:
                try {
                    if (!infoToggled) {
                        if(realTimeMode) {
                            weather_upgrader.invalidateWeatherInfo();
                        }
                        loadDefaultInfos();
                        openInfoContainer();

                    } else {
                        if(realTimeMode) {
                            time_engine.remove_Time_Observer(this.fishingPanelFragment);
                        }


                        uIelementSet.toogle_InfoFloating(info_Container);
                        infoToggled = false;
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;

            case R.id.fab_hook:
                toogleHook(true);
                break;

            case R.id.fab_hook_cancle:
                toogleHook(false);

                break;

            case R.id.fab_hook_complete:

                try {
                    int fishNum = Integer.valueOf((hook_marker.getItemName().split(" ")[0]));
                    this.totalFishNum += fishNum;
                    String[]courseDatas = new String[3];
                    courseDatas[0] = "Hook";
                    courseDatas[1] = String.valueOf(fishNum);
                    courseDatas[2] = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    courseLogs.add(courseDatas);
                    FishingRecord record;
                    if(weather_upgrader.isTaskComplete()) {
                        record = new FishingRecord(fishNum,
                                hook_marker.getMapPoint(),
                                weather_upgrader.getWeatherData().getTidalSpeed(),
                                weather_upgrader.getWeatherData().getTidalDeg(),
                                weather_upgrader.getWeatherData().getWaterTemp(),
                                weather_upgrader.get_OceanPost().getName(),
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    }else {
                        record = new FishingRecord(fishNum,
                                hook_marker.getMapPoint(),
                                99.0f,99.0f,99.0f,null,
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    }

                    createRecordMarker(hook_marker, fishNum, record);
                    db_manager.uploadFishingRecord(record);
                    record = null;
                }catch (NullPointerException e){
                    Log.e("Record is Not Created", "H"+e.toString());
                    e.printStackTrace();
                }

                toogleHook(false);
                break;

            case R.id.btn_dynamic_dialog_cancle:
                if(dynamicDialog!=null){
                    dynamicDialog.dismiss();

                }
                break;

        }
    }

    private void createRecordMarker(MapPOIItem hookMarker, int FishNum, FishingRecord record){
        MapPOIItem recordMarker = new MapPOIItem();
        recordMarker.setMapPoint(hookMarker.getMapPoint());
        recordMarker.setItemName(""+FishNum);
        recordMarker.setUserObject(record);
        recordMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        recordMarker.setCustomImageResourceId(R.drawable.default_marker);
        recordMarker.setShowDisclosureButtonOnCalloutBalloon(false);
        recordMarker.setLeftSideButtonResourceIdOnCalloutBalloon(R.drawable.bucket);
        recordMarker.setTag(TAG_RecordMarker);
        mapView.addPOIItem(recordMarker);
        //mapView.selectPOIItem(recordMarker, false);
        fishNumRecords.add(recordMarker);
    }

    private void createRecordMarker(MapPoint.GeoCoordinate geoCoordinate, int FishNum, FishingRecord record){
        Log.e("recordPollChecker", ""+geoCoordinate.latitude + "" +geoCoordinate.longitude +" " + FishNum + " " + record.toString());
        MapPOIItem recordMarker = new MapPOIItem();
        recordMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(geoCoordinate.latitude, geoCoordinate.longitude));
        recordMarker.setItemName(""+FishNum);
        recordMarker.setUserObject(record);
        recordMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        recordMarker.setCustomImageResourceId(R.drawable.default_marker);
        recordMarker.setShowDisclosureButtonOnCalloutBalloon(false);
        recordMarker.setLeftSideButtonResourceIdOnCalloutBalloon(R.drawable.bucket);
        recordMarker.setTag(TAG_RecordMarker);
        mapView.addPOIItem(recordMarker);
        mapView.selectPOIItem(recordMarker, false);
        fishNumRecords.add(recordMarker);
    }

    private void toogleHook(boolean h_switch){
        Log.e("Check", "Check");
        try {
            if(h_switch) {
                weather_upgrader.invalidateOceanInfo();
                mapView.removeAllPolylines();
                deleteMarker();
                mapView.setMapCenterPointAndZoomLevel(point, 1, true);
                hook_marker = new MapPOIItem();
                hook_marker.setMapPoint(point);
                hook_marker.setItemName("0");
                hook_marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                hook_marker.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
                ViewTreeObserver vto = reel.getViewTreeObserver();
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        // Remove after the first run so it doesn't fire forever
                        reel.getViewTreeObserver().removeOnPreDrawListener(this);
                        final int height = reel.getMeasuredHeight();
                        final int width = reel.getMeasuredWidth();
                        Log.e("Size Check", "reel x: " + width + " reel y" + height);
                        uIelementSet.buildFishingReel(width, height, BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.reel), reel, hook_marker);
                        return true;
                    }
                });

                mapView.addPOIItem(hook_marker);
                mapView.selectPOIItem(hook_marker, false);
                fab_hook.hide();
                FrameLayout reelContainer = (FrameLayout)this.findViewById(R.id.reel_container);
                mapView.setOnTouchListener(uIelementSet.touchSwitch(false));
                mapViewContainer.bringChildToFront(reelContainer);

                map_command = ChangeLifeCycle;
            }else {
                mapView.removePOIItem(hook_marker);
                hook_marker = null;
                reloadMarker();
                mapView.setMapCenterPointAndZoomLevel(point, 4, true);
                map_command = 0;
                mapView.setOnTouchListener(uIelementSet.touchSwitch(true));
                mapViewContainer.bringChildToFront(mapView);
                mapViewContainer.bringChildToFront(fab_hook);
                uIelementSet.cleanFishingReel();
                System.gc();
                fab_hook.show();
            }

        }catch (NullPointerException e){
            Toast.makeText(this, "현재 위치가 인식되지 않습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void setBoatMarker(MapPoint init_point){
        boatMarker = new MapPOIItem();
        boatMarker.setMapPoint(init_point);
        boatMarker.setItemName("boatMarker");
        boatMarker.setTag(1);
        boatMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        boatMarker.setCustomImageResourceId(R.drawable.boat);
        boatMarker.setCustomImageAnchorPointOffset(new MapPOIItem.ImageOffset(12, 44));
        mapView.addPOIItem(boatMarker);
    }

    private void setDestination(MapPoint d_point){

        if(destination_Marker!=null){
            mapView.removePOIItem(destination_Marker);
            destination_Marker = null;
        }
        destination_Marker = new MapPOIItem();
        destination_Marker.setMapPoint(d_point);
        destination_Marker.setItemName("도착지");
        destination_Marker.setItemName("destination_Marker");
        destination_Marker.setTag(TAG_DestinationMarker);
        destination_Marker.setRightSideButtonResourceIdOnCalloutBalloon(R.drawable.cancle_red);
        destination_Marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);

        destination_Guideline = new MapPolyline();
        mapView.addPOIItem(destination_Marker);
    }


    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {
        Log.e("check api auth", s);

    }


    //manage gps
    protected synchronized void buildGoogleApiClient() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("Permission", "show");


                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                Log.e("Permission", "pass");
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        111);
                return;


                // MY_PERMISSION_REQUEST_READ_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();

    }

    // Get permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 111: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
                    googleApiClient.connect();
                    Log.e("build check", "build check");

                } else {
                    // permission was denied
                }
                return;
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.e("connected check", "connected check");
        toggleGps(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "GPS 등록 실패!",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "GPS 등록 실패!",Toast.LENGTH_SHORT).show();
        Log.e("Gps Connect Error", connectionResult.getErrorMessage());
    }


    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            Log.e("toogle Gps", "Gps On");
        }else {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }

    }
    //end

    private double angleCalculator(double startlat, double startlong, double endlat, double endlong) {

        if(startlat==-99 && startlong ==-99){
            return 0;
        }
        double lat1 = Math.toRadians(startlat);
        double long1 = Math.toRadians(startlong);
        double lat2 = Math.toRadians(endlat);
        double long2 = Math.toRadians(endlong);

        double dLon = (long2 - long1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        //brng = 360 - brng; // count degrees counter-clockwise - remove to make clockwise

        return brng;
    }

    private float distance_Calculator(MapPoint startPoint, MapPoint endPoint) {
        double lat1 = startPoint.getMapPointGeoCoord().latitude;
        double lng1 = startPoint.getMapPointGeoCoord().longitude;
        double lat2 = endPoint.getMapPointGeoCoord().latitude;
        double lng2 = endPoint.getMapPointGeoCoord().longitude;

        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist/1000;
    }

    private void move_Boatmarker(MapPoint c_point, double start_lat, double start_long){
        if(boatMarker==null){
            return;
        }
        double headling = angleCalculator(start_lat, start_long, c_point.getMapPointGeoCoord().latitude, c_point.getMapPointGeoCoord().longitude);
        boatMarker.setMapPoint(c_point);
        boatMarker.setRotation((float)headling);
        pre_lat = c_point.getMapPointGeoCoord().latitude;
        pre_lot = c_point.getMapPointGeoCoord().longitude;
    }

    private boolean speedCalculator_WithDisplay(float c_speed){
        float kph = c_speed * 3.6f;
        float knot = (float) (kph * 0.5399568);
        int tmp_duration = 120; //maximum is 30 (0.5second)

        //display speed info
        try {
            tv_knot.setText(String.format("%.2f", knot) + " knot");
            tv_kph.setText("("+String.format("%.2f", kph) + " km/h)");
        }catch (NullPointerException e){
            Toast.makeText(this, "Speed UI has Error!", Toast.LENGTH_SHORT).show();
        }

        //return boolean value
        if (kph>2.5){
            return true;
        }
        //return false
        return true;
    }

    private float getAngle(MapPoint point) {

        float angle = (float) Math.toDegrees(Math.atan2(point.getMapPointScreenLocation().y - boatMarker.getMapPoint().getMapPointScreenLocation().x,  point.getMapPointScreenLocation().x - point.getMapPointScreenLocation().y));



        return angle;
    }

    private void change_MapViewArea(int command){
        switch (command){
            case 1:
                //mapView.setMapRotationAngle(getAngle(destination_Marker.getMapPoint()), true);
                break;

        }
    }

    private void change_DestinationMarker(MapPoint d_point){
        if(destination_Marker==null){
            return;
        }
        destination_Marker.setMapPoint(d_point);
        invalidate_DestinationGuideLine_And_MarkerTitle();
    }

    private void invalidate_DestinationGuideLine_And_MarkerTitle(){
        if(destination_Guideline==null){
            return;
        }
        String remain_distance = String.format("%.2f", distance_Calculator(boatMarker.getMapPoint(), destination_Marker.getMapPoint()));
        destination_Marker.setItemName(String.valueOf(remain_distance+" Km"));
        mapView.selectPOIItem(destination_Marker,false);
        mapView.removePolyline(destination_Guideline);
        destination_Guideline = null;
        destination_Guideline = new MapPolyline();
        destination_Guideline.setLineColor(Color.GREEN);
        destination_Guideline.addPoint(boatMarker.getMapPoint());
        destination_Guideline.addPoint(destination_Marker.getMapPoint());
        mapView.addPolyline(destination_Guideline);
        change_MapViewArea(1);
        remain_distance = null;
    }


    @Override
    public void onLocationChanged(Location location) {
        //Point Setting (Static)
        point = MapPoint.mapPointWithGeoCoord(location.getLatitude(), location.getLongitude());

        switch (map_command){
            case -1:
                weather_upgrader.excute_Upgrade(location.getLatitude(), location.getLongitude());
                setBoatMarker(point);
                pointList.add(point.getMapPointGeoCoord());
                map_command = 0;

                break;

            case 0:
                move_Boatmarker(point, pre_lat, pre_lot);
                add_position = speedCalculator_WithDisplay(location.getSpeed());
                invalidate_DestinationGuideLine_And_MarkerTitle();
                if(timeIsUP){
                    mapView.setMapCenterPointAndZoomLevel(point, 4, true);
                }
                polyLine.addPoint(point);
                mapView.addPolyline(polyLine);

                break;
        }

        if(add_position) {
            Log.w("pointList_Size: ", "" + pointList.size());
            if (pointCount % 25 >= 0) {
                pointList.add(point.getMapPointGeoCoord());
                pointCount=0;
                if (pointList.size() >= 400) {
                    try {
                        mapReverseGeoCoder = new MapReverseGeoCoder(getString(R.string.access_token), point, this, this);
                        mapReverseGeoCoder.startFindingAddress();
                        db_manager.uploadMapPoints(pointList);
                        pointList.clear();
                        System.gc();
                        isFirstTime = false;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                pointCount++;
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        //mapView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.map_command = ChangeLifeCycle;
        try {
            deleteMarker();
            mapView.removeAllPolylines();
            destination_Guideline = null;
            if(!isFirstTime){ // erase poly line
                polyLine = null;
            }

            mapView.releaseUnusedMapTileImageResources();

        }catch (NullPointerException e){
        }
        System.gc();
        Log.e("kim", "Real Time map is on pause");
    }

    private void reloadMarker(){
        try{
            toggleRecordMarkers(true);
            if(destination_Marker!=null){
                mapView.addPOIItem(destination_Marker);
                destination_Guideline = new MapPolyline();
            }
        }catch (NullPointerException e){

        }

    }

    private void toggleRecordMarkers(boolean toggle){
        MapPOIItem[] recordMarkers;
        if(fishNumRecords == null){
            return;
        }
        recordMarkers=new MapPOIItem[fishNumRecords.size()];
        if(toggle){
            mapView.addPOIItems(fishNumRecords.toArray(recordMarkers));
        }else {
            mapView.removePOIItems(fishNumRecords.toArray(recordMarkers));
        }
        recordMarkers = null;
    }

    private void deleteMarker(){

        toggleRecordMarkers(false);
        if(destination_Marker!=null) {
            mapView.removePOIItem(destination_Marker);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Map View", "Mapview is resume");

        if(this.map_command==ChangeLifeCycle){
            reloadMarker();
            if(!isFirstTime){ //draw polyline from fb
                try {
                    polyLine = new MapPolyline();
                    polyLine.setLineColor(Color.RED);
                    db_manager.queryData(0);
                    loadingDialog = ProgressDialog.show(this, "항로 로드중...", "잠시만 기다려 주세요");
                }catch (NullPointerException e){
                    Toast.makeText(this, "항로가 로드 되지 않습니다\n010-9957-1712로 문의 주세요", Toast.LENGTH_LONG).show();
                }
            }else {
                mapView.addPolyline(polyLine);
            }
            this.map_command = 0;
        }

    }

    private void drawFBPolyLine(String fbData){

        Type pointToken = new TypeToken<Collection<List<MapPoint.GeoCoordinate>>>(){}.getType();
        Collection<List<MapPoint.GeoCoordinate>> pointCollection = gson.fromJson(fbData, pointToken);

        List<List<MapPoint.GeoCoordinate>> listContainer = (ArrayList<List<MapPoint.GeoCoordinate>>)pointCollection;
        MapPoint fbPoint;
        int count = 0;
        int checkNum = 0;
        int lastCheck = 0;
        ArrayList<MapPoint>tmpList = new ArrayList<>();
        Log.e("TotalSlizeCheck: ", ""+listContainer.size());
        for(List<MapPoint.GeoCoordinate> pointList : listContainer){
            Log.e("indexCheck", ""+listContainer.indexOf(pointList));
            for(MapPoint.GeoCoordinate p : pointList){
                fbPoint = MapPoint.mapPointWithGeoCoord(p.latitude, p.longitude);
                polyLine.addPoint(fbPoint);

                /*if (count < 20) {
                    Log.e("Count:", "" + count);
                    count++;
                } else {
                    polyLine.addPoint(fbPoint);
                    //polyLine.addPoints(tmpList.toArray(new MapPoint[0]));
                    tmpList.clear();
                    count = 0;
                    Log.e("fbLaodCheck", "" + (++checkNum));
                }
                //polyLine.addPoint(fbPoint);
                */


            }
            /*

            */
            System.gc();
        }
        /*
        //Add Last Point
        List<MapPoint.GeoCoordinate> lastContainer = (listContainer.get(listContainer.size()-1));
        MapPoint.GeoCoordinate lastPoint = lastContainer.get(lastContainer.size()-1);
        polyLine.addPoint(MapPoint.mapPointWithGeoCoord(lastPoint.latitude, lastPoint.longitude));

        */

        mapView.addPolyline(polyLine);

        //lastContainer = null;
        //lastPoint = null;
        pointToken = null;
        pointCollection = null;
        listContainer = null;
        fbPoint = null;
        System.gc();
        if(loadingDialog!=null&&loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
        if(!realTimeMode){
            mapView.fitMapViewAreaToShowPolyline(polyLine);

        }
    }

    private void addFBPolItem(String fbData){
        Type polItemsToken = new TypeToken<Collection<FishingRecord>>(){}.getType();
        Collection<FishingRecord> recordCollection = gson.fromJson(fbData, polItemsToken);
        List<FishingRecord> records = (ArrayList<FishingRecord>)recordCollection;
        Log.e("recordsSize", ""+records.size());
        for(FishingRecord record : records){
            createRecordMarker(record.getHook_point(), record.getFishNum(), record);
            Log.e("why", "Why");
        }
        polItemsToken = null;
        System.gc();
    }

    private void addFBInfoFragment(String weatherData, String historyData){

        try {
            Bundle weatherBundle = new Bundle();
            weatherInfoFragment = new WeatherInfoFragment();
            weatherBundle.putString("weatherData", weatherData);
            weatherBundle.putString("weatherData", weatherData);
            weatherInfoFragment.setArguments(weatherBundle);

            Type fishingHistoryType = new TypeToken<HashMap<String, Object>>(){}.getType();
            HashMap<String, Object>fishingHistory = gson.fromJson(historyData, fishingHistoryType);

            Log.e("kim", historyData);
            Bundle panelBundle = new Bundle();
            panelBundle.putString("startTime", ((String)fishingHistory.get("startDate")).substring(11, 19));
            panelBundle.putString("endTime",((String)fishingHistory.get("endDate")).substring(11, 19));
            if(fishNumRecords!=null && fishNumRecords.size()>0){
                try {
                    int[] tmp_fishNumArray = new int[fishNumRecords.size()];
                    for(int i = 0 ; i<fishNumRecords.size() ; i++){
                        tmp_fishNumArray[i] = Integer.valueOf(fishNumRecords.get(i).getItemName());
                    }
                    panelBundle.putBoolean("drawChart", true);
                    panelBundle.putString("fishNumsData", gson.toJson(tmp_fishNumArray));
                    tmp_fishNumArray = null;
                }catch (IndexOutOfBoundsException e){
                    Toast.makeText(this, "데이터 변환 오류", Toast.LENGTH_SHORT).show();
                }
            }else {
                panelBundle.putBoolean("drawChart", false);
            }
            fishingPanelFragment = new FishingPanelFragment();
            fishingPanelFragment.setArguments(panelBundle);


            Bundle courseBundle = new Bundle();
            courseBundle.putString("courseData", (String)fishingHistory.get("courseData"));
            courseLogFragment = new CourseLogFragment();
            courseLogFragment.setArguments(courseBundle);
            weatherBundle = null;
            panelBundle = null;
            courseBundle = null;
            System.gc();

        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }



    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        finish();
        System.gc();
    }
    public void sendLogcatMail(){
        File outputFile = new File(Environment.getExternalStorageDirectory(), "logcat.txt");
        try{
            Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
        }catch (IOException e){
            e.printStackTrace();
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"rlathdwls2@ajou.ac.kr"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_STREAM, "Error is occured");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }



    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    private void time_count(int time){
        count_TimeNum = time;
        Log.e("time setUp", ""+count_TimeNum + ", " + count_TimeNum);
    }

    @Override
    public void time_update() {
        if(count_TimeNum>0){
            count_TimeNum--;
            timeIsUP = false;
            Log.e("time check", ""+count_TimeNum + ", " + timeIsUP);
        }else if(count_TimeNum==0){
            count_TimeNum = -1;
            timeIsUP = true;
            Log.e("time check", ""+count_TimeNum + ", " + timeIsUP);
        }
    }





    @Override
    public void onMapViewInitialized(MapView mapView) {
        this.mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(36.36131, 128.15552), 18, true);
        if(realTimeMode){
            buildGoogleApiClient();
        }else{
            drawFBPolyLine(this.getIntent().getStringExtra("mapPointList"));

            addFBPolItem(this.getIntent().getStringExtra("mapPolItems"));
            Log.e("forCheck", "Check");
            addFBInfoFragment(this.getIntent().getStringExtra("mapWeatherData"), this.getIntent().getStringExtra("mapHistoryData"));

        }

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        if(i!=4 && i!=11){
            time_count(10);
            Log.e("zoom level change", ""+i);
        }
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        time_count(10);

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        time_count(10);

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        Log.e("check", "check");
        if(destination_Guideline==null){
            setDestination(mapPoint);
            return;
        }

        change_DestinationMarker(mapPoint);

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        time_count(10);

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        time_count(10);

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void get_Data(String serialized_data, int command) {
        switch (command){
            case 0:
                try {
                    drawFBPolyLine(serialized_data);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
        }
    }


    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        Log.e("mp", ""+mapPOIItem.getTag() );
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        if(polItemTouched){
            polItemTouched = false;
            return;
        }
        Log.e("mp", ""+mapPOIItem.getTag() );

        switch (mapPOIItem.getTag()){
            case TAG_RecordMarker:
                if (fishingRecordFragment!=null){
                    fishingRecordFragment = null;
                }
                Bundle recordBundle = new Bundle();
                Log.e("kk", "Test");
                recordBundle.putString("record", gson.toJson(((FishingRecord)mapPOIItem.getUserObject())));
                loadSingleInfo(1, recordBundle);
                openInfoContainer();
                break;
            case TAG_DestinationMarker:
                if(destination_Marker!=null && destination_Guideline!=null){
                    mapView.removePOIItem(destination_Marker);
                    mapView.removePolyline(destination_Guideline);
                    destination_Marker = null;
                    destination_Guideline = null;
                }
        }
        polItemTouched = true;
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        if(courseLogs!=null){
            String[] courseDatas = new String[3];
            courseDatas[0] = "Location";
            courseDatas[1] = s;
            courseDatas[2] = new SimpleDateFormat("HH:mm:ss").format(new Date());
            courseLogs.add(courseDatas);
        }
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

    }
}
