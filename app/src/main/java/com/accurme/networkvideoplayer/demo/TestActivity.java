package com.accurme.networkvideoplayer.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.accurme.networkvideoplayer.R;
import com.accurme.networkvideoplayer.widget.NetworkVideoView;

import java.io.IOException;

public class TestActivity extends AppCompatActivity{

//    private final static String url="http://220.170.49.102/13/a/y/e/t/ayetmsranwgpaknlrbvctlckmnnnma/hc.yinyuetai.com/562D0159F986126A0EB4A4ADC8E6952A.mp4?sc=4c95876a10ab6264&br=779&vid=2784721&aid=349&area=JP&vst=0&ptp=mv&rd=yinyuetai.com";
    private final static String url="http://7xln7a.media1.z0.glb.clouddn.com/lihg3ecb8bvb5IimEIrAMgYjkCrc";
    private NetworkVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        gestureDetector=new GestureDetectorCompat(this,this);
//        gestureDetector.setOnDoubleTapListener(this);
        videoView= (NetworkVideoView) findViewById(R.id.videoView);
        try {
            videoView.setPlayUrl(url);
//            videoView.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
        videoView.setOnFullScreenListener(new NetworkVideoView.onFullScreenListener() {
            @Override
            public void onFullScreen(boolean isFullScreen, View videoView) {
//                if (isFullScreen){
//                    ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
//                    layoutParams.width=ViewGroup.LayoutParams.MATCH_PARENT;
//                    layoutParams.height=ViewGroup.LayoutParams.MATCH_PARENT;
//                    videoView.setLayoutParams(layoutParams);
////                    ((LinearLayout)findViewById(R.id.normal_container)).removeView(videoView);
////                    ((RelativeLayout)findViewById(R.id.full_container)).addView(videoView,layoutParams);
//                }else {
//                    ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
//                    Log.d("xxxxxxx","layoutParams.height="+layoutParams.height+"  layoutParams.width="+layoutParams.width);
////                    ((RelativeLayout)findViewById(R.id.full_container)).removeView(videoView);
//                    layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT;
//                    layoutParams.height= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,250,getResources().getDisplayMetrics());
//                    videoView.setLayoutParams(layoutParams);
////                    ((LinearLayout)findViewById(R.id.normal_container)).addView(videoView,layoutParams);
//                }
            }
        });
    }

//    public void openDrawer(View view){
//        ((DrawerLayout)findViewById(R.id.drawerLayout)).openDrawer(Gravity.LEFT);
//    }

    @Override
    protected void onPause() {
        Log.d("xxxxxxx","-----onPause-----");
        super.onPause();
        videoView.setPauseScreenPosition();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            getSupportActionBar().hide();
        }else if (newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            getSupportActionBar().show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (videoView.getIsFullScreen()){
            videoView.expandShrink(false);
        }else {
            finish();
        }
    }
}
