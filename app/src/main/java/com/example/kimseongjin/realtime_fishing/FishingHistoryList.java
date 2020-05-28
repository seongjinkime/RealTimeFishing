package com.example.kimseongjin.realtime_fishing;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by kimseongjin on 2017. 5. 28..
 */

public class FishingHistoryList extends AppCompatActivity implements Interface_Collection.FB_Cursor_Observer{
    private RecyclerView historyList;
    private String historyTable = "fishingHistory";

    FirebaseAuth m_auth;
    FirebaseUser m_user;
    FB_Cursor fb_cursor;

    DatabaseReference dataReference;

    private FishingHistorysAdapter historysAdapter;
    private View.OnClickListener changeRangeListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fishing_history_list);

        m_auth = FirebaseAuth.getInstance();
        m_user = m_auth.getCurrentUser();

        fb_cursor = new FB_Cursor();
        fb_cursor.add_cursor_observer(this);


        this.historyList =  (RecyclerView)findViewById(R.id.fishing_history_list_view);
        this.historyList.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<DataSnapshot>emptySnapshot = new ArrayList<DataSnapshot>();
        historysAdapter = new FishingHistorysAdapter(emptySnapshot);
        historyList.setAdapter(historysAdapter);
        String[] tmpDate = new SimpleDateFormat("yyyy-MM").format(new Date()).split("-");

        convertDateAndQueryList(Integer.valueOf(tmpDate[0]), Integer.valueOf(tmpDate[1]));
        //convertDateAndQueryList(year, monthOfYear);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.select_date_range:
                String Dates[] = ((String)new SimpleDateFormat("yyyy-MM-dd").format(new Date())).split("-");
                monthPickerDialoge dialoge = new monthPickerDialoge(FishingHistoryList.this, this);
                dialoge.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(FishingHistoryList.this, MainActivity.class));
        finish();
    }

    public void convertDateAndQueryList(int year, int month){
        String strYear = String.valueOf(year);
        String strMonth = String.valueOf(month);

        if (strMonth.length()<2){
            strMonth = "0"+strMonth;
        }
        String startDate = strYear +"-" + strMonth + "-" + "00";
        String endDate = strYear + "-" + strMonth + "-" + "31";
        Log.e("convertDateCheck:", startDate +", " + endDate);
        queryList(startDate, endDate);
    }

    private void queryList(String startDate, String endDate){
        String queryKey = historyTable + "/" + m_user.getUid();
        dataReference = FirebaseDatabase.getInstance().getReference().child(queryKey);
        Query query = dataReference.orderByKey().startAt(startDate).endAt(endDate);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<DataSnapshot> dataContainer = new ArrayList<DataSnapshot>();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    dataContainer.add(data);
                    Log.e("numOfDataCheck", "DataAdded");
                }
                Log.e("dataContainerNum", ""+dataContainer.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        historysAdapter.swapData(dataContainer);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void get_queriedData(ArrayList<String> datas) {
        if(datas.size()==4) {
            Log.e("dataQueryIs Success!", "" + datas.size());
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("isRealTimeMode", false);
            intent.putExtra("mapPointList", datas.get(0));
            intent.putExtra("mapPolItems", datas.get(1));
            intent.putExtra("mapWeatherData", datas.get(2));
            intent.putExtra("mapHistoryData", datas.get(3));
            startActivity(intent);
        }else {
            Toast.makeText(this, "죄송합니다. 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }



    class FishingHistorysAdapter extends RecyclerView.Adapter<FishingHistorysAdapter.ViewHolder>{

        private ArrayList<DataSnapshot> dataSet;
        private ArrayList<String> dateList;
        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView tv_fish_total_num, tv_start_date, tv_start_time, tv_end_time;
            public ViewHolder(View itemView) {
                super(itemView);
                tv_fish_total_num = (TextView)itemView.findViewById(R.id.tv_fish_total_num_history);
                tv_start_date = (TextView)itemView.findViewById(R.id.tv_fish_start_date_history);
                tv_start_time = (TextView)itemView.findViewById(R.id.tv_fish_start_time_history);
                tv_end_time = (TextView)itemView.findViewById(R.id.tv_fish_end_time_history);
            }
        }

        public FishingHistorysAdapter(ArrayList<DataSnapshot> set){
            this.dataSet = set;
            this.dateList = new ArrayList<String>();
        }
        @Override
        public FishingHistorysAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fishing_history_card, parent,false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        public void swapData(ArrayList<DataSnapshot> set){
            dataSet.clear();
            dataSet.addAll(set);
            this.dateList.clear();
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(FishingHistorysAdapter.ViewHolder holder, final int position) {
            HashMap<String, Object> historyData = (HashMap<String, Object>)(dataSet.get(position).getValue());
            String key = historyData.keySet().iterator().next();
            Log.e("test is Null?", key);
            HashMap<String, Object>  data =  (HashMap<String, Object>) historyData.get(key);
            historyData.get(historyData.keySet());
            this.dateList.add((String)data.get("startDate"));


            holder.tv_fish_total_num.setText(String.valueOf(data.get("totalFishNum")));
            holder.tv_start_date.setText(((String)(data.get("startDate"))).substring(0,10));
            holder.tv_start_time.setText(((String)(data.get("startDate"))).substring(11,19));
            holder.tv_end_time.setText(((String)(data.get("endDate"))).substring(11,19));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("ListDateCheck", dateList.get(position));
                    fb_cursor.selectAllDataFromFB_At( dateList.get(position));

                }
            });
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }


    }
}

class monthPickerDialoge extends Dialog implements View.OnClickListener {


    TextView tv_year, tv_month;
    FishingHistoryList parent;
    public monthPickerDialoge(@NonNull Context context, FishingHistoryList parent) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.parent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.month_picker);

        ImageButton leftYear, rightYear;
        ImageButton leftMonth, rightMonth;

        leftYear = (ImageButton)findViewById(R.id.left_year);
        leftYear.setOnClickListener(this);
        rightYear = (ImageButton)findViewById(R.id.right_year);
        rightYear.setOnClickListener(this);
        leftMonth = (ImageButton)findViewById(R.id.left_month);
        leftMonth.setOnClickListener(this);
        rightMonth = (ImageButton)findViewById(R.id.right_month);
        rightMonth.setOnClickListener(this);

        Button monthOk, monthCancle;

        monthOk = (Button)findViewById(R.id.btn_monthPicker_OK);
        monthCancle = (Button)findViewById(R.id.btn_monthPicker_cancle);
        monthOk.setOnClickListener(this);
        monthCancle.setOnClickListener(this);

        tv_month = (TextView)findViewById(R.id.tv_month);
        tv_year = (TextView)findViewById(R.id.tv_year);

        String[] currentDates = new SimpleDateFormat("yyyy-MM").format(new Date()).split("-");
        tv_year.setText(currentDates[0]);
        tv_month.setText(currentDates[1]);

    }

    private void changeMonth(boolean increment){
        int month = Integer.valueOf((String)tv_month.getText());
        if(increment){
            month++;
            if(month>12){
                month=1;
            }
        }else {
            month--;
            if(month<=0){
                month=12;
            }
        }

        String resultText = String.valueOf(month);
        if(resultText.length()<2){
            resultText = 0+resultText;
        }
        tv_month.setText(resultText);
    }

    private void changeYear(boolean increment){
        int year = Integer.valueOf((String)tv_year.getText());
        if(increment){
            year++;
        }else {
            year--;
        }
        tv_year.setText(String.valueOf(year));
    }

    private void changeParentDate(){
        parent.convertDateAndQueryList(Integer.valueOf((String)tv_year.getText()), Integer.valueOf((String)tv_month.getText()));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.left_year:
                changeYear(false);
                break;
            case R.id.right_year:
                changeYear(true);
                break;
            case R.id.left_month:
                changeMonth(false);
                break;
            case R.id.right_month:
                changeMonth(true);
                break;
            case R.id.btn_monthPicker_OK:
                changeParentDate();
                this.dismiss();
                break;
            case R.id.btn_monthPicker_cancle:
                this.dismiss();
                break;
        }
    }


}





