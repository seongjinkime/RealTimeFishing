package com.example.kimseongjin.realtime_fishing;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import net.daum.mf.map.api.MapPOIItem;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kimseongjin on 2017. 5. 23..
 */

public class UIelementSet implements Interface_Collection.animation_Engine{
    private Interface_Collection.animation_Observer animation_observer;
    private boolean viewFloated = false;
    private WheelTouchListener wheelTouchListener;
    private Bitmap reelScaled, reelImg;
    private LinearLayout fragmentsContainer;
    private LinearLayout infoPagerTitleContainer;
    private ViewPager infoPager;
    private ArrayList<String> pageTitles;
    private ArrayList<TextView>tvTitlceContainer;

    private InfoPagerAdapter infoPagerAdapter;

    private final View.OnTouchListener disableTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    return true;
            }
            return true;
        }
    };

    private final View.OnTouchListener enableTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    private Animation fadeInAnimation = new AlphaAnimation(0,1);
    private Animation fadeOutAnimation = new AlphaAnimation(1, 0);



    public void buildFishingReel(int width, int height, Bitmap reel_drawable, ImageView iv_reel, MapPOIItem marker){


        Matrix matrix = new Matrix();
        Matrix resize = new Matrix();
        resize.postScale(1.0f, 1.0f);

        reelImg = Bitmap.createBitmap(reel_drawable, 0, 0, reel_drawable.getWidth(),
                reel_drawable.getHeight(), resize, false);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        reelImg.compress(Bitmap.CompressFormat.PNG, 2, os);
        byte[] array = os.toByteArray();

        reelScaled = BitmapFactory.decodeByteArray(array, 0, array.length);

        float translateX = (width / 2) - (reelScaled.getWidth() / 2);
        float translateY = (height /2) - (reelScaled.getHeight() / 2);
        matrix.postTranslate(translateX, translateY);

        iv_reel.setImageBitmap(reelScaled);
        iv_reel.setImageMatrix(matrix);
        wheelTouchListener = new WheelTouchListener(width, height, matrix, iv_reel, marker);
        iv_reel.setOnTouchListener(wheelTouchListener);

        matrix = null;
        resize = null;
        os = null;
        array = null;
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();

    }

    public void cleanFishingReel(){
        if(wheelTouchListener!=null) {
            wheelTouchListener = null;
        }
        if(reelScaled!=null && reelImg!=null){
            reelImg.recycle();
            reelScaled.recycle();
            reelImg = null;
            reelScaled = null;
        }
        System.gc();
    }

    @Override
    public void addAnimObserver(Interface_Collection.animation_Observer observer) {
        this.animation_observer = observer;
    }

    @Override
    public void notifyAnimationTheEnd() {
        animation_observer.animationTheEnd();
    }


    private class WheelTouchListener implements View.OnTouchListener {
        private double startAngle;
        private double totalRotation;
        private int top;
        private int divAngle;
        private int divCount;
        private boolean snapToCenterFlag = true;
        private int reel_width, reel_height;
        private Matrix matrix;                         //Matrix used to perform rotations
        private ImageView reel;
        private MapPOIItem marker;
        private int count;

        public WheelTouchListener(int width, int height, Matrix reel_matrix, ImageView iv_reel, MapPOIItem hook_marker){
            this.reel_width = width;
            this.reel_height = height;
            this.matrix = reel_matrix;
            this.reel = iv_reel;
            this.marker = hook_marker;
            count = 0;
            setDivCount();
        }

        public void setDivCount() {
            this.divCount = 2;

            divAngle = 360 / divCount;
            totalRotation = -1 * (divAngle / 2);


        }

        private int getQuadrant(double x, double y) {
            if (x >= 0) {
                return y >= 0 ? 1 : 4;
            } else {
                return y >= 0 ? 2 : 3;
            }
        }

        private double getAngle(double x, double y) {
            x = x - (reel_width / 2d);
            y = reel_height - y - (reel_height / 2d);

            switch (getQuadrant(x, y)) {
                case 1:
                    return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
                case 2:
                    return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
                case 3:
                    return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
                case 4:
                    return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
                default:
                    return 0;
            }
        }

        private void rotateWheel(float degrees) {
            matrix.postRotate(degrees, reel_width / 2, reel_height / 2);
            reel.setImageMatrix(matrix);

            //add the rotation to the total rotation
            totalRotation = totalRotation + degrees;

        }

        private void change_FishCount(double num){
            this.count+=num;
            if(count<=0){
                count=0;
            }
            marker.setItemName(""+count+" 마리");
            Log.e("reel Change:", ""+count);

        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    //get the start angle for the current move event
                    startAngle = getAngle(event.getX(), event.getY());
                    break;


                case MotionEvent.ACTION_MOVE:
                    //get the current angle for the current move event
                    double currentAngle = getAngle(event.getX(), event.getY());

                    //rotate the wheel by the difference
                    rotateWheel((float) (startAngle - currentAngle));

                    float angle = (float) (startAngle - currentAngle);
                    Log.e("angle:", ""+angle);

                    if(angle<=-200 && angle>-360){
                        angle = 1;
                    }
                    if(angle>=200 && angle<360){
                        angle = -1;
                    }




                    change_FishCount(angle/35.0f); //UX_Check

                    //current angle becomes start angle for the next motion
                    startAngle = currentAngle;
                    break;


                case MotionEvent.ACTION_UP:
                    //get the total angle rotated in 360 degrees
                    totalRotation = totalRotation % 360;

                    //represent total rotation in positive value
                    if (totalRotation < 0) {
                        totalRotation = 360 + totalRotation;
                    }

                    //calculate the no of divs the rotation has crossed
                    int no_of_divs_crossed = (int) ((totalRotation) / divAngle);

                    //calculate current top
                    top = (divCount + top - no_of_divs_crossed) % divCount;

                    //for next rotation, the initial total rotation will be the no of degrees
                    // inside the current top
                    totalRotation = totalRotation % divAngle;

                    //snapping to the top's center
                    if (snapToCenterFlag) {

                        //calculate the angle to be rotated to reach the top's center.
                        double leftover = divAngle / 2 - totalRotation;

                        rotateWheel((float) (leftover));

                        //re-initialize total rotation
                        totalRotation = divAngle / 2;
                    }


                    break;
            }

            return true;
        }
    }


    public View.OnTouchListener touchSwitch(boolean button){
        if(button){
            return enableTouch;
        }else {
            return disableTouch;
        }
    }

    private Transition.TransitionListener floatListener = new Transition.TransitionListener() {
        @Override
        public void onTransitionStart(Transition transition) {
            if(viewFloated){
                //delete UI
                infoPager.setVisibility(View.INVISIBLE);
                infoPagerTitleContainer.setVisibility(View.INVISIBLE);
                infoPager.startAnimation(fadeOutAnimation);
                infoPagerTitleContainer.startAnimation(fadeOutAnimation);
            }
        }

        @Override
        public void onTransitionEnd(Transition transition) {
            if(infoPager!=null && infoPagerTitleContainer != null){
                if(viewFloated){
                    notifyAnimationTheEnd(); //delete fragment object
                }else {
                    //add View
                    infoPager.setVisibility(View.VISIBLE);
                    infoPagerTitleContainer.setVisibility(View.VISIBLE);
                    infoPager.startAnimation(fadeInAnimation);
                    infoPagerTitleContainer.startAnimation(fadeInAnimation);
                }
                viewFloated = !viewFloated;
            }
        }

        @Override
        public void onTransitionCancel(Transition transition) {

        }

        @Override
        public void onTransitionPause(Transition transition) {

        }

        @Override
        public void onTransitionResume(Transition transition) {

        }
    };

    private Animation.AnimationListener floatingAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            if(viewFloated){
                //delete UI
                infoPager.setVisibility(View.INVISIBLE);
                infoPagerTitleContainer.setVisibility(View.INVISIBLE);
                infoPager.startAnimation(fadeOutAnimation);
                infoPagerTitleContainer.startAnimation(fadeOutAnimation);
            }

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(infoPager!=null && infoPagerTitleContainer != null){
                if(viewFloated){
                    notifyAnimationTheEnd(); //delete fragment object
                }else {
                    //add View
                    infoPager.setVisibility(View.VISIBLE);
                    infoPagerTitleContainer.setVisibility(View.VISIBLE);
                    infoPager.startAnimation(fadeInAnimation);
                    infoPagerTitleContainer.startAnimation(fadeInAnimation);
                }
                viewFloated = !viewFloated;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private class FloatingAnimation extends Animation{
        private final float mStartWeight;
        private final float mDeltaWeight;
        private ViewGroup targetLayout;

        public FloatingAnimation(float start_weight, float end_weight, ViewGroup target){
            this.mStartWeight = start_weight;
            this.mDeltaWeight = end_weight - start_weight;
            this.targetLayout = target;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) targetLayout.getLayoutParams();
            lp.weight = (mStartWeight + (mDeltaWeight * interpolatedTime));
            targetLayout.setLayoutParams(lp);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public void setNestedInfoLayout(View infoView, FragmentManager fragmentManager){
        fragmentsContainer = (LinearLayout) infoView.findViewById(R.id.fragment_container);
        infoPager = (ViewPager)infoView.findViewById(R.id.info_pager);
        infoPagerTitleContainer = (LinearLayout)infoView.findViewById(R.id.info_pager_title_container);
        infoPagerAdapter = new InfoPagerAdapter(fragmentManager);
        infoPager.setAdapter(infoPagerAdapter);

    }

    public void setFragments(ArrayList<Fragment>fragments, ArrayList<String> page_titles){
        if(infoPager==null && infoPagerAdapter == null){
            return;
        }
        if(pageTitles!=null){
            this.pageTitles.clear();
            this.pageTitles.addAll(page_titles);
        }else {
            this.pageTitles = page_titles;
        }
        infoPagerAdapter.clearInfoFragments();
        Log.e("numOfPage", ": "+fragments.size());
        infoPagerAdapter.setInfoFragments(fragments);
    }

    public void toogle_InfoFloating(ViewGroup target){
        if(fragmentsContainer==null){
            Log.e("UI Error", "fragments elements is null");
            return;
        }
        Animation mainAnim, nestedAnim;
        LinearLayout.LayoutParams mainContainer, nestedContainer;
        LinearLayout titleContainer;
        LinearLayout.LayoutParams title_ViewParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvTitlceContainer = new ArrayList<TextView>();
        titleContainer = (LinearLayout)infoPagerTitleContainer;
        Log.e("title Test", ""+tvTitlceContainer.size()+", " + pageTitles.size());
        if(viewFloated){
            mainContainer = (LinearLayout.LayoutParams) target.getLayoutParams();
            mainContainer.weight = 10;
            nestedContainer = (LinearLayout.LayoutParams) fragmentsContainer.getLayoutParams();
            nestedContainer.weight = 300;
            titleContainer.removeAllViews();
        }else {
            for(String title : pageTitles){
                TextView titleView = new TextView(target.getContext());
                titleView.setText(title);
                titleView.setLayoutParams(title_ViewParam);
                titleView.setTextSize(12.0f);
                titleView.setTextColor(Color.WHITE);
                titleView.setPadding(0, 0, 10, 10);
                titleContainer.addView(titleView);
                tvTitlceContainer.add(titleView);
                Log.e("title Test", "check");
            }


            mainContainer = (LinearLayout.LayoutParams) target.getLayoutParams();
            mainContainer.weight = 0.5f;


            nestedContainer = (LinearLayout.LayoutParams) fragmentsContainer.getLayoutParams();
            nestedContainer.weight = 1;

        }



        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTarget(target);
        transitionSet.addTarget(fragmentsContainer);
        transitionSet.setStartDelay(0);
        transitionSet.setDuration(200);
        transitionSet.addTransition(new Slide(Gravity.TOP));
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transitionSet.addListener(floatListener);
        TransitionManager.beginDelayedTransition((ViewGroup)target.getParent(), transitionSet);
        target.setLayoutParams(mainContainer);
        fragmentsContainer.setLayoutParams(nestedContainer);
        fadeInAnimation.setDuration(300);
        fadeOutAnimation.setDuration(300);

    }

    private class InfoPagerAdapter extends FragmentPagerAdapter{

        private String pageTitle[] = new String[]{"날씨"};
        private ArrayList<Fragment> infoFragments;

        public InfoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setInfoFragments(ArrayList<Fragment> fragments){
            this.infoFragments = fragments;
            Log.e("numOfPageInAdapter", ": "+fragments.size());
            this.notifyDataSetChanged();
        }

        public void clearInfoFragments(){
            if(infoFragments!=null){
                infoFragments.clear();
                infoFragments = null;
            }
        }


        @Override
        public Fragment getItem(int position) {
            try {
                return infoFragments.get(position);

            }catch (NullPointerException e){
                Log.e("infoPagerAdapter", "fragment is NULL");
            }
            return null;
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if(infoFragments==null){
                return 0;
            }
            return infoFragments.size();
        }
    }

}

