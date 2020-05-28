package com.example.kimseongjin.realtime_fishing;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kimseongjin on 2017. 5. 26..
 */

public class FishingPanelFragment extends Fragment implements Interface_Collection.time_Observer{
    View view;
    LineChart fishNumsChart;
    Gson gson;
    TextView tv_startTime,tv_fishingTimer, tv_total_fish_num;
    java.text.DateFormat df;
    String start_time;
    String last_time;
    String diffTimeText;
    int totalFishNum;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle data;
        gson = new Gson();
        this.totalFishNum = 0;
        Log.e("kim", "Fragment is onCreate");
        if(view==null){
            Log.e("kim", "fragment is created");
            view = inflater.inflate(R.layout.fishing_panel_fragment,container, false);
            fishNumsChart = (LineChart) view.findViewById(R.id.fishNums_chart);
            df = new SimpleDateFormat("hh:mm:ss");
            tv_startTime = (TextView)view.findViewById(R.id.tv_start_time);
            tv_fishingTimer = (TextView)view.findViewById(R.id.tv_fishing_timer);
            tv_total_fish_num = (TextView)view.findViewById(R.id.tv_total_fish_num);

        }
        if(getArguments()!=null){
            data = getArguments();
            start_time = data.getString("startTime");
            tv_startTime.setText(start_time);
            if((this.last_time = data.getString("endTime"))!=null){
                time_update();
            }

            if(data.getBoolean("drawChart")){
                int[]fishNumsData = gson.fromJson(data.getString("fishNumsData"), int[].class);
                drawChart(fishNumsData);
            }


        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void drawChart(int[] fishNums){
        if(fishNumsChart==null){
            Log.e("Panel Fragment", "LineChartis NULL");
            return;
        }
        List<Entry> entries = new ArrayList<Entry>();

        try {
            for(int i = 0 ; i<fishNums.length ; i++){
                entries.add(new Entry(i, fishNums[i]));
                this.totalFishNum += fishNums[i];
            }
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return;
        }catch (IndexOutOfBoundsException i){
            i.printStackTrace();
            return;
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "조황 그래프");
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);

        fishNumsChart.setBackgroundColor(Color.BLACK);
        fishNumsChart.setGridBackgroundColor(Color.argb(10, 255, 255, 255));
        fishNumsChart.setBorderColor(Color.argb(10, 255, 255, 255));
        fishNumsChart.setTouchEnabled(false);
        LineData lineData = new LineData(dataSet);
        fishNumsChart.setData(lineData);
        fishNumsChart.invalidate();
        this.tv_total_fish_num.setText(""+this.totalFishNum);


    }

    @Nullable
    @Override
    public View getView() {
        return view;
    }

    @Override
    public void time_update() {
        int timeInSeconds, hours, minutes, seconds;
        try {

            Date startTime = df.parse(this.start_time);
            if(this.getArguments().getString("endTime")==null) {
                last_time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            }

            Date currentTime = df.parse(last_time);
            long diffTime = currentTime.getTime() - startTime.getTime();

            timeInSeconds = (int)diffTime/1000;
            hours = timeInSeconds/3600;
            timeInSeconds = timeInSeconds - (hours * 3600);
            minutes = timeInSeconds / 60;
            timeInSeconds = timeInSeconds - (minutes * 60);
            seconds = timeInSeconds;

            diffTimeText = (hours<10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_fishingTimer.setText(diffTimeText);
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }catch (NullPointerException n){
            n.printStackTrace();
        }catch (ClassCastException c){
            c.printStackTrace();
        }
    }
}
