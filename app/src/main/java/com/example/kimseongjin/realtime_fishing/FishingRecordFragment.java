package com.example.kimseongjin.realtime_fishing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * Created by kimseongjin on 2017. 5. 27..
 */

public class FishingRecordFragment extends Fragment {

    View view;
    Gson gson;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fishing_record_fragment, container, false);
        gson = new Gson();
        if(getArguments()!=null){
            try {
                FishingRecord record = gson.fromJson(getArguments().getString("record"), FishingRecord.class);
                ((TextView)view.findViewById(R.id.tv_fishnum_record)).setText(""+record.getFishNum() + " 마리");
                ((TextView)view.findViewById(R.id.tv_fishing_date_record)).setText("시간: "+record.getHookTime());
                ((TextView)view.findViewById(R.id.tv_tide_speed_record)).setText(""+record.getTidal_speed()+" m/s");
                ((TextView)view.findViewById(R.id.tv_water_temp_record)).setText(""+record.getWater_temp());
                ((TextView)view.findViewById(R.id.tv_ocean_post_record)).setText(""+record.getPost_name());

                ((ImageView)view.findViewById(R.id.iv_tide_compass_record)).setRotation(record.getTidal_deg());

            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        return view;
    }
}
