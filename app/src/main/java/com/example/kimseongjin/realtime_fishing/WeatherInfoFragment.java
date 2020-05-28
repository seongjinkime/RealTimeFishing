package com.example.kimseongjin.realtime_fishing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by kimseongjin on 2017. 5. 25..
 */

public class WeatherInfoFragment extends Fragment{
    View view;
    TextView tv_temp,tv_postInfo, tv_lunarDate, tv_tideTime, tv_Humidity, tv_windSpeed, tv_tideSpeed, tv_waterTemp;
    ImageView iv_weatherIcon, iv_windCompass, iv_tideCompass;
    ArrayList<TextView[]> tideInfoContainer;
    TextView[]tvContainer;
    Weather_Data weatherData;
    Gson gson;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.weather_fragment, container, false);
        tvContainer = new TextView[2];
        tideInfoContainer = new ArrayList<TextView[]>();

        gson = new Gson();

        tv_temp = (TextView)view.findViewById(R.id.tv_temp);
        tv_postInfo = (TextView)view.findViewById(R.id.tv_post_info);
        tv_lunarDate =  (TextView)view.findViewById(R.id.tv_lunar_date);
        tv_tideTime =  (TextView)view.findViewById(R.id.tv_tide_time);
        tv_Humidity =  (TextView)view.findViewById(R.id.tv_humidity);
        tv_windSpeed =  (TextView)view.findViewById(R.id.tv_wind_speed);
        tv_tideSpeed =  (TextView)view.findViewById(R.id.tv_tide_speed);
        tv_waterTemp =  (TextView)view.findViewById(R.id.tv_water_temp);
        tvContainer[0] =  (TextView)view.findViewById(R.id.tv_first_low_tide_time);
        tvContainer[1] =  (TextView)view.findViewById(R.id.tv_first_low_tide_level);
        tideInfoContainer.add(tvContainer);

        tvContainer = new TextView[2];
        tvContainer[0] =  (TextView)view.findViewById(R.id.tv_first_high_tide_time);
        tvContainer[1] =  (TextView)view.findViewById(R.id.tv_first_high_tide_level);
        tideInfoContainer.add(tvContainer);

        tvContainer = new TextView[2];
        tvContainer[0] =  (TextView)view.findViewById(R.id.tv_second_low_tide_time);
        tvContainer[1] =  (TextView)view.findViewById(R.id.tv_second_low_tide_level);
        tideInfoContainer.add(tvContainer);

        tvContainer = new TextView[2];
        tvContainer[0] = (TextView)view.findViewById(R.id.tv_second_high_tide_time);
        tvContainer[1] = (TextView)view.findViewById(R.id.tv_second_high_tide_level);
        tideInfoContainer.add(tvContainer);



        iv_weatherIcon = (ImageView)view.findViewById(R.id.iv_weather_icon);
        iv_windCompass = (ImageView)view.findViewById(R.id.iv_wind_compass);
        iv_tideCompass = (ImageView)view.findViewById(R.id.iv_tide_compass);
        if(getArguments()!=null){
            weatherData = gson.fromJson(getArguments().getString("weatherData"), Weather_Data.class);
            invalidateUI();
        }
        return view;
    }


    private void invalidateUI(){
        try {
            String temp = String.format("%.1f", (weatherData.getTemp()-273.15f));
            tv_temp.setText(temp+"°");
            tv_postInfo.setText(weatherData.getOceanPostName()+ ", " + weatherData.getPreTidePostName() +"\n에서 업데이트 됨");
            tv_lunarDate.setText(weatherData.getLunar_Date());
            tv_tideTime.setText(weatherData.getTide_time());
            tv_Humidity.setText(weatherData.getHumidity() +"%");
            tv_windSpeed.setText(weatherData.getWindSpeed() +"m/s");
            tv_tideSpeed.setText(weatherData.getTidalSpeed() + "m/s");
            tv_waterTemp.setText(""+weatherData.getWaterTemp());
            for(int i = 0 ; i<weatherData.getPre_TideInfo().size() ; i++){
                (tideInfoContainer.get(i)[0]).setText(weatherData.getPre_TideInfo().get(i)[1]);
                (tideInfoContainer.get(i)[1]).setText(weatherData.getPre_TideInfo().get(i)[2]);
            }

            Glide.with(getContext()).load(getResources().getIdentifier(weatherData.getIcon(), "drawable", getActivity().getPackageName())).into(iv_weatherIcon);
            iv_windCompass.setRotation(weatherData.getWindDeg());
            iv_tideCompass.setRotation(weatherData.getTidalDeg());
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public View getView() {
        return view;
    }


}
