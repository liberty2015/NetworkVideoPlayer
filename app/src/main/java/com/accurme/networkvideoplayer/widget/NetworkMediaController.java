package com.accurme.networkvideoplayer.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.accurme.networkvideoplayer.R;

/**
 * Created by LinJinFeng on 2017/2/8.
 */

public class NetworkMediaController extends FrameLayout implements
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener{

    private View rootView,loading,soundController,lightController,titleBar,mediaContainer;
    private ImageView playPause,expandShrink,back;
    private TextView title,currentProgress,totalDuration,chapter;
    private AppCompatSeekBar seekBar;


    private NetworkMediaControl control;

    private int currentPosition;
    private int newProgress;

    private final int MAX_PROGRESS=100;

    public final int SHOW_PROGRESS=0x11;

    public final int HIDE_CONTROLLER=0x14;

    public final int SHOW_PLAY_PAUSE=0x12;

    public final int SHOW_EXPAND_SHRINK=0x13;

    private boolean isFullScreen;

    private boolean isShowing=true;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_PROGRESS:{
                    int pos=setProgress();
                    msg=obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg,MAX_PROGRESS-(pos%MAX_PROGRESS));
                }
                break;
                case HIDE_CONTROLLER:{
                    hideController();
                    isShowing=false;
                }
            }
        }
    };

    public boolean getIsFullScreen(){
        return isFullScreen;
    }

    public NetworkMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void initView(){
        rootView=LayoutInflater.from(getContext()).inflate(R.layout.media_controller_layout,this);
        playPause= (ImageView) rootView.findViewById(R.id.play_pause);
        expandShrink= (ImageView) rootView.findViewById(R.id.expand_shrink);
        title= (TextView) rootView.findViewById(R.id.title);
        currentProgress= (TextView) rootView.findViewById(R.id.currentProgress);
        totalDuration= (TextView) rootView.findViewById(R.id.totalDuration);
        back= (ImageView) rootView.findViewById(R.id.back);
        seekBar= (AppCompatSeekBar) rootView.findViewById(R.id.progress);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(MAX_PROGRESS);
        loading=rootView.findViewById(R.id.loadingController);
        soundController=rootView.findViewById(R.id.soundController);
        lightController=rootView.findViewById(R.id.lightController);
        titleBar=rootView.findViewById(R.id.video_titleBar);
        mediaContainer=rootView.findViewById(R.id.media_controller);
        chapter= (TextView) rootView.findViewById(R.id.chapter);
        chapter.setOnClickListener(this);
        playPause.setOnClickListener(this);
        expandShrink.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void init(){
        initView();
//        gestureDetector=new GestureDetectorCompat(getContext(),gestureListener);

    }

    public boolean getIsShowing(){
        return isShowing;
    }

    public void setIsShowing(boolean isShowing){
        this.isShowing=isShowing;
    }

    public void setNetworkMediaControl(NetworkMediaControl control){
        this.control=control;
    }

    public void setTitle(String titleTxt){
        if (title!=null){
            title.setText(titleTxt);
        }
    }

    public int setProgress(){
        if (control==null){
            return 0;
        }
        int position=control.getCurrentPosition();
        int duration=control.getTotalDuration();
        String timeStr=calculateProgress(position);
        String totalTimeStr=calculateProgress(duration);
        currentProgress.setText(timeStr);
        totalDuration.setText(totalTimeStr);
        if (seekBar!=null){
            if (duration>0){
                long pos=MAX_PROGRESS*position/duration;
                seekBar.setProgress((int) pos);
            }
            int bufferingPosition=control.getBufferingPosition();
            seekBar.setSecondaryProgress(bufferingPosition*MAX_PROGRESS/100);
        }
        return position;
    }


    public void showLoading(){
        loading.setVisibility(VISIBLE);
    }

    public void hideLoading(){
        loading.setVisibility(GONE);
    }

    public void showSoundController(int percent){
        Log.d("xxxxxx","showSoundController");
        soundController.setVisibility(VISIBLE);
        if (percent<=0){
            ((TextView)findViewById(R.id.soundNum)).setText(0+"%");
            ((ImageView)findViewById(R.id.sound_icon)).setImageResource(R.mipmap.sound_off);
        }else {
            ((TextView)findViewById(R.id.soundNum)).setText(percent+"%");
            ((ImageView)findViewById(R.id.sound_icon)).setImageResource(R.mipmap.sound_on);
        }
    }

    public void hideSoundController(){
        soundController.setVisibility(GONE);
    }

    public void showLightController(int percent){
        lightController.setVisibility(VISIBLE);
        if (percent<=0){
            ((TextView)findViewById(R.id.lightNum)).setText(0+"%");
            ((ImageView)findViewById(R.id.light_icon)).setImageResource(R.mipmap.light_off);
        }else {
            ((TextView)findViewById(R.id.lightNum)).setText(percent+"%");
            ((ImageView)findViewById(R.id.light_icon)).setImageResource(R.mipmap.light_on);
        }
    }

    public void hideLightController(){
        lightController.setVisibility(GONE);
    }

    /**
     * 计算时间进度
     * @param position
     * @return
     */
    private String calculateProgress(int position){
        float dprogress=position/1000;
        int minute= (int) (dprogress/60);
        int second= (int) (dprogress%60);
        String progressStr=Integer.toString(minute);
        if (second<10){
            progressStr=progressStr+":0"+second;
        }else {
            progressStr=progressStr+":"+second;
        }
        Log.d("xxxxx","progressStr="+progressStr);
        return progressStr;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.play_pause:{
                if (control!=null){
                    if (control.isPlaying()){
                        currentPosition=control.getCurrentPosition();
                        control.pause();
                    }else {
                        if(currentPosition!=0){
                            control.seekTo(currentPosition);
                            currentPosition=0;
                        }
                        control.play();
                    }
                    showController();
                }
            }
            break;
            case R.id.expand_shrink:{
                if (isFullScreen){
                    control.expandShrink(false);
                    isFullScreen=false;
                }else {
                    control.expandShrink(true);
                    isFullScreen=true;
                }
            }
            break;
            case R.id.back:{
                control.expandShrink(false);
                isFullScreen=false;
            }
            break;
            case R.id.chapter:{
                control.rightBtn();
            }
            break;
        }
    }

    public void seekTo(int position){
        seekBar.setProgress(position*MAX_PROGRESS/control.getTotalDuration());
    }



    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        Log.d("xxxxxxx","-----onProgressChanged-----");
        newProgress=progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
//        Log.d("xxxxxxx","-----onStartTrackingTouch-----");

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        Log.d("xxxxxxx","-----onStopTrackingTouch-----");
        int newPosition=newProgress*control.getTotalDuration()/MAX_PROGRESS;
        control.seekTo(newPosition);
        currentPosition=newPosition;
    }

    public interface NetworkMediaControl{
        void seekTo(int position);
        void play();
        boolean canPause();
        void pause();
        boolean isPlaying();
        void expandShrink(boolean fullScreen);
        void expandShrink(boolean fullScreen,int screenOrientation);
        int getBufferingPosition();
        int getCurrentPosition();
        int getTotalDuration();
        void rightBtn();
    }

    public void showProgress(){
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }



    public void hideController(){
        if (isShowing){
            titleBar.setVisibility(GONE);
            mediaContainer.setVisibility(GONE);
        }

    }

    public void showCenterView(@IdRes int resId){
        switch (resId){
            case R.id.loadingController:{
                loading.setVisibility(VISIBLE);
            }
            break;
            case R.id.soundController:{
                soundController.setVisibility(VISIBLE);
            }
            break;
            case R.id.lightController:{
                lightController.setVisibility(VISIBLE);
            }
            break;
        }
    }

    public void showController(){
        playPause.setImageResource(control.isPlaying()?R.mipmap.pause:R.mipmap.play);
        expandShrink.setImageResource(isFullScreen?R.mipmap.shrink:R.mipmap.expand);
        titleBar.setVisibility(isFullScreen?VISIBLE:GONE);
        mediaContainer.setVisibility(VISIBLE);
        Message msg=new Message();
        msg.what=HIDE_CONTROLLER;
        mHandler.sendMessageDelayed(msg,3*1000);
    }



    public void hideView(@IdRes int resId){

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }
}
