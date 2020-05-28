package com.example.kimseongjin.realtime_fishing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String userNmae;

    ImageView mainSymbol_imageView;
    TextView tv_welcom;

    Button start_fishing;
    Button btn_login;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainSymbol_imageView = (ImageView)findViewById(R.id.main_symbol_view);
        Glide.with(this).load(R.drawable.surffishing).into(mainSymbol_imageView);
        start_fishing = (Button)findViewById(R.id.btn_start_fishing);
        tv_welcom = (TextView)findViewById(R.id.tv_welcome_text);
        btn_login = (Button)findViewById(R.id.btn_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser==null){
            startActivity(new Intent(this, SignActivity.class));
            finish();
            return;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        userNmae = mFirebaseUser.getDisplayName();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = reference.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            HashMap<String, Object> users;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    users = (HashMap<String, Object>)snapshot.getValue();
                    if (mFirebaseAuth.getCurrentUser().getUid().equals(users.get("Uid"))){
                        tv_welcom.setText(users.get("Name") + " 조사님\n환영합니다!");
                        btn_login.setText("로그아웃 하기");
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onMainClick(View v){
        Intent intent;
        switch (v.getId()){
            case R.id.btn_start_fishing:
                intent = new Intent(this, MapActivity.class);
                intent.putExtra("isRealTimeMode", true);
                startActivity(intent);

                intent = null;
                finish();
                break;
            case R.id.btn_view_history:
                intent = new Intent(this, FishingHistoryList.class);
                startActivity(intent);
                intent = null;
                finish();
                break;
            case R.id.btn_login:
                mFirebaseAuth.getInstance().signOut();
                mFirebaseUser = null;
                startActivity(new Intent(this, SignActivity.class));
                finish();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        mainSymbol_imageView.setImageBitmap(null);
        Glide.clear(mainSymbol_imageView);
        super.onDestroy();
    }
}
