package com.example.kimseongjin.realtime_fishing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kimseongjin on 2017. 5. 27..
 */

public class CourseLogFragment extends Fragment {
    View view;
    private RecyclerView courseLogRecyclerView;
    private Gson gson;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.course_log_fragment, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        courseLogRecyclerView = (RecyclerView)view.findViewById(R.id.course_recycler_view);
        courseLogRecyclerView.setLayoutManager(linearLayoutManager);
        gson = new Gson();
        Type courseDataType = new TypeToken<Collection<String[]>>(){}.getType();

        if(getArguments()!=null){
            Collection<String[]> Datas = gson.fromJson(getArguments().getString("courseData"), courseDataType);
            ArrayList<String[]> courseDatas = (ArrayList<String[]>)Datas;
            Log.e("courseDatas Num: ", ""+courseDatas.size());
            CourseLogsAdapter adapter = new CourseLogsAdapter(courseDatas);
            courseLogRecyclerView.setAdapter(adapter);
        }
        return view;
    }


}
class CourseLogsAdapter extends RecyclerView.Adapter<CourseLogsAdapter.ViewHolder>{
    private ArrayList<String[]> dataSet;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_icon;
        public TextView tv_title, tv_descriptiion;
        public ViewHolder(View itemView) {
            super(itemView);
            iv_icon = (ImageView)itemView.findViewById(R.id.iv_course_icon);
            tv_title = (TextView)itemView.findViewById(R.id.tv_course_log_title);
            tv_descriptiion = (TextView)itemView.findViewById(R.id.tv_course_log_description);
        }
    }

    public CourseLogsAdapter(ArrayList<String[]> set){
        this.dataSet = set;
    }
    @Override
    public CourseLogsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_log_card, parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CourseLogsAdapter.ViewHolder holder, int position) {
        String[] courseData = dataSet.get(position);
        if(courseData[0].equals("Location")){
            holder.iv_icon.setImageResource(R.drawable.course_marker);
            holder.tv_title.setText(courseData[1]+" 로 이동");
            holder.tv_descriptiion.setText(courseData[2]);
        }else{
            holder.iv_icon.setImageResource(R.drawable.course_hook);
            holder.tv_title.setText(courseData[1]+" 마리 잡음 ");
            holder.tv_descriptiion.setText(courseData[2]);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
