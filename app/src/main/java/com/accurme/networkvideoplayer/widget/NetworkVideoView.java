package com.accurme.networkvideoplayer.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.accurme.networkvideoplayer.R;

import java.io.IOException;

import static android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;

/**
 * Created by LinJinFeng on 2017/2/8.
 */

public class NetworkVideoView extends FrameLayout implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnErrorListener,
        NetworkMediaController.NetworkMediaControl,
        SurfaceHolder.Callback{

    private MediaPlayer mMediaPlayer;
    private String url;
    private int mCurrentBufferProgress;
    private int mTotalDuration;
    private NetworkMediaController controller;
    private SurfaceView surfaceView;
    private RelativeLayout rightPanel;

    private ValueAnimator openAnim,closeAnim;

    private boolean hasPrepared;

    private int mVideoLayoutWidth,mVideoLayoutHeight;
    private int mVideoWidth,mVideoHeight;
    private int surfaceWidth,surfaceHeight;

    private int fullScreenSurfaceWidth,fullScreenSurfaceHeight;
    private int normalScreenSurfaceWidth,normalScreenSurfaceHeight;

    private AudioManager audioManager;

    private GestureDetectorCompat gestureDetector;

    private int volume=-1;

    private float brightness=-1.0f;

    private int screenPixelWidth;

    private int rightPanelWidth;

    private int pauseScreenPosition=-1;

    public NetworkVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setPauseScreenPosition(){
        if (mMediaPlayer!=null){
            this.pauseScreenPosition=getCurrentPosition();
        }
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.video_layout,this);
        surfaceView= (SurfaceView) findViewById(R.id.surfaceView);

        rightPanel=(RelativeLayout) findViewById(R.id.right_panel);
        rightPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rightPanel.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                rightPanelWidth=rightPanel.getWidth();
                openAnim=ValueAnimator.ofInt(-rightPanelWidth,0)
                        .setDuration(400);
                openAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rightPanel.getLayoutParams();
//                        Log.d("onAnimationUpdate","value="+rightMargin+"  rightMargin="+layoutParams.rightMargin+"  animation.getAnimatedValue()="+animation.getAnimatedValue());
                        layoutParams.rightMargin= (int) animation.getAnimatedValue();
                        rightPanel.setLayoutParams(layoutParams);
                    }
                });
                openAnim.setTarget(rightPanel);
            }
        });



        surfaceView.getHolder().setKeepScreenOn(true);
        /**
         * 为surfaceView配置surfaceHolder.Callback，以确保surfaceView准备好了
         */
        surfaceView.getHolder().addCallback(this);
        /**
         * 设置为SurfaceView不管理自己的缓存区，这个方法虽然提示为过时，
         * 但还是要设置，避免视频播放时，出现有声音没图像的问题
         */
        surfaceView.getHolder().setType(SURFACE_TYPE_PUSH_BUFFERS);
        if (isInEditMode())
            return;
        initMediaPlayer();
        ViewTreeObserver viewTreeObserver = surfaceView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                surfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                normalScreenSurfaceWidth=surfaceView.getWidth();
                normalScreenSurfaceHeight=surfaceView.getHeight();
            }
        });
        controller.showController();
        gestureDetector=new GestureDetectorCompat(getContext(),gestureListener);


        screenPixelWidth=getResources().getDisplayMetrics().widthPixels;
    }

    private void initMediaPlayer(){
        Log.d("xxxxxxx","-----initMediaPlayer-----");
        release(true);
        mMediaPlayer=new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        controller= (NetworkMediaController) findViewById(R.id.mediaController);
        controller.setNetworkMediaControl(this);
        audioManager= (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    private GestureDetector.SimpleOnGestureListener gestureListener=
            new GestureDetector.SimpleOnGestureListener(){

                @Override
                public boolean onDown(MotionEvent e) {
                    /**
                     * 必须返回为true，否则后续的回调函数无法被调用
                     */
                    if (controller.getIsShowing()){
                        controller.hideController();
                        controller.setIsShowing(false);
                    }else {
                        controller.showController();
                        controller.setIsShowing(true);
                    }
                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    Log.d("xxxxx","-----onSingleTapUp-----");

                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    float oldY=e1.getY();
                    float oldX=e1.getX();
                    float deltaY=oldY-e2.getY();
                    float percent= (deltaY/surfaceView.getHeight());
                    boolean soundControl=oldX<(0.5f*NetworkVideoView.this.getHeight());
                    Log.d("xxxxxx","-----onScroll-----percent="+percent);
                    if (soundControl){
                        slideSound(percent);
                        Log.d("xxxxxx","-----onScroll-----soundControl="+soundControl);
                    }else {
                        slideLight(percent);
                        Log.d("xxxxxx","-----onScroll-----soundControl="+soundControl);
                    }
                    return true;
                }

            };

    private void slideSound(float percent){
        int maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (volume==-1){
            volume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume<0){
                volume=3;
            }
        }
//        float currentPercent=(float) currentVolume/maxVolume;
//        float realPercent=percent+currentPercent;
//        Log.d("xxxxx","realPercent="+realPercent);
//        if (realPercent>=100){
//            realPercent=100;
//        }else if (realPercent<=0){
//            realPercent=0;
//        }
        int realVolume= (int) (volume+maxVolume*percent);
        if (realVolume>maxVolume){
            realVolume=maxVolume;
        }else if (realVolume<0){
            realVolume=0;
        }
        int realPercent= (int) (((float)realVolume/maxVolume)*100);
        Log.d("xxxxxx","currentVolume="+volume+"  maxVolume="+maxVolume);
        Log.d("xxxxxx","realPercent="+realPercent);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,realVolume,0);
        controller.hideLightController();
        controller.showSoundController(realPercent);

    }

    private void slideLight(float percent){
        Window window = ((Activity) getContext()).getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (brightness==-1.0f){
            brightness=attributes.buttonBrightness;
            if (brightness<=0.0f){
                brightness=0.5f;
            }else if (brightness<=0.01f){
                brightness=0.01f;
            }
        }

//        float currentBrightness=attributes.screenBrightness;
//        Log.d("xxxxxx","currentBrightness="+currentBrightness);
//        if (currentBrightness<=0.0f){
//            currentBrightness=0.5f;
//        }else if (currentBrightness<=0.01f){
//            currentBrightness=0.01f;
//        }
        float realBrightness=brightness+percent;
        if (realBrightness<=0.0f){
            realBrightness=0.0f;
        }else if (realBrightness>=1.0f){
            realBrightness=1.0f;
        }
        Log.d("xxxxxx","realBrightness="+realBrightness);
        attributes.screenBrightness=realBrightness;
        window.setAttributes(attributes);
        controller.hideSoundController();
        int realPercent= (int) (realBrightness*100);
        Log.d("xxxxxx","Brightness percent="+realPercent);
        controller.showLightController(realPercent);
    }

    public void setPlayUrl(String url) throws IOException {
        mMediaPlayer.reset();
        this.url=url;
        mMediaPlayer.setDataSource(getContext(),Uri.parse(url));
        hasPrepared=false;

        /**
         * 设置好数据源才能调用setDisplay
         */
        mMediaPlayer.prepareAsync();
    }

    public void setRightPanelView(View view){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height= LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width=LinearLayout.LayoutParams.MATCH_PARENT;
        rightPanel.addView(view,layoutParams);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d("xxxxx","buffer:"+percent);
        mCurrentBufferProgress=percent;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        boolean handled=false;
        switch (what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:{
                if (controller!=null){
                    controller.showLoading();
                }
                handled=true;
            }
            break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:{
                if (controller!=null){
                    controller.hideLoading();
                }
            }
        }
        return handled;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.setDisplay(surfaceView.getHolder());
        mTotalDuration=mp.getDuration();
        controller.hideLoading();
        controller.showProgress();
        mVideoWidth=mp.getVideoWidth();
        mVideoHeight=mp.getVideoHeight();
        hasPrepared=true;
        play();
//        if (fullScreenPosition!=0){
//            seekTo(fullScreenPosition);
//        }
//        controller.showController();
        controller.hideController();
        controller.setIsShowing(false);
//        if (mCurrentBufferProgress>0){
//            mMediaPlayer.seekTo(mCurrentBufferProgress);
//        }
        if (mVideoWidth!=0&&mVideoHeight!=0){
            surfaceView.getHolder().setFixedSize(mVideoWidth,mVideoHeight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)){
            return true;
        }
        switch (event.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:{
                touchUp();
            }
            break;
        }
        return false;
    }

    @Override
    public void seekTo(int position) {
        Log.d("xxxxxxxx","seekTo");
        mMediaPlayer.seekTo(position);
    }

    @Override
    public void play() {
        if (!hasPrepared){
            controller.showLoading();
        }else {
            mMediaPlayer.start();
            if (pauseScreenPosition!=-1){
                seekTo(pauseScreenPosition);
                pauseScreenPosition=-1;
            }
        }
    }


    @Override
    public boolean canPause() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if (canPause()){
            mMediaPlayer.pause();
        }
    }

    public boolean getIsFullScreen(){
        return isFullScreen;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    private boolean isFullScreen=false;

    @Override
    public void expandShrink(boolean fullScreen,int screenOrientation) {
        Activity activity= (Activity) getContext();
//        if (mMediaPlayer!=null){
//            fullScreenPosition=mMediaPlayer.getCurrentPosition();
//            Log.d("xxxxxxx","fullScreenPosition="+fullScreenPosition);
//        }
        isFullScreen=fullScreen;
        if (fullScreen){
            mVideoLayoutWidth=this.getWidth();
            mVideoLayoutHeight=this.getHeight();
            Log.d("xxxxxxx","mVideoLayoutWidth="+mVideoLayoutWidth+"  mVideoLayoutHeight="+mVideoLayoutHeight);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
            ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
            layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height= ViewGroup.LayoutParams.MATCH_PARENT;
            fullScreenSurfaceWidth=getResources().getDisplayMetrics().heightPixels;
            fullScreenSurfaceHeight=getResources().getDisplayMetrics().widthPixels;

            this.setLayoutParams(layoutParams);
        }else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height=mVideoLayoutHeight;
            layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT;
            setLayoutParams(layoutParams);
        }
        if (onFullScreenListener!=null){
            onFullScreenListener.onFullScreen(fullScreen,this);
        }
        keepVideoAspectRatio();
    }

    @Override
    public void expandShrink(boolean fullScreen) {
        int screenOrientation=fullScreen? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        expandShrink(fullScreen,screenOrientation);
    }

    @Override
    public int getBufferingPosition() {
        return mCurrentBufferProgress;
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer!=null){
            mCurrentBufferProgress=mMediaPlayer.getCurrentPosition();
            return mCurrentBufferProgress;
        }
        return mCurrentBufferProgress;
    }

    @Override
    public int getTotalDuration() {
        return mTotalDuration;
    }

    private boolean rightPanelToggle=false;

    @Override
    public void rightBtn() {
        if (rightPanelToggle){
            closeRightPanel();
            rightPanelToggle=false;
        }else {
            openRightPanel();
            rightPanelToggle=true;
        }
//        if (drawerLayout.isDrawerOpen(Gravity.RIGHT)){
//            drawerLayout.closeDrawer(Gravity.RIGHT);
//        }else {
//            drawerLayout.openDrawer(Gravity.RIGHT);
//        }

    }

    private void openRightPanel(){
//        rightPanel.animate()
//                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//
//                    }
//                })
//                .setInterpolator(new AccelerateDecelerateInterpolator())
//                .setDuration(400)
//                .start();
        if (openAnim!=null){
            openAnim.start();
        }
    }

    private void closeRightPanel(){
//        rightPanel.animate()
//                .xBy(rightPanel.getWidth())
//                .setInterpolator(new AccelerateDecelerateInterpolator())
//                .setDuration(400)
//                .start();
        if (openAnim!=null){
            openAnim.reverse();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("xxxxxxx","-----surfaceCreated-----");
        if (mMediaPlayer==null){
            initMediaPlayer();
            try {
                setPlayUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
//            if (fullScreenPosition!=0){
//                seekTo(fullScreenPosition);
//            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        surfaceWidth=width;
//        surfaceHeight=height;
        Log.d("xxxxxxx","-----surfaceChanged-----");
//        if (mMediaPlayer!=null){
////            keepVideoAspectRatio();
////            if (fullScreenPosition!=0){
////                seekTo(fullScreenPosition);
////            }
//            play();
//        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("xxxxxxx","-----surfaceDestroyed-----");
        if (mMediaPlayer!=null){
            release(true);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        controller.showController();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.d("xxxxxxx","-----onVideoSizeChanged-----");
        mVideoWidth=width;
        mVideoHeight=height;
        if (mVideoWidth!=0&&mVideoHeight!=0){
            surfaceView.getHolder().setFixedSize(mVideoWidth,mVideoHeight);
            keepVideoAspectRatio();
        }
    }

    /**
     * 保持组件和视频的纵横比
     */
    private void keepVideoAspectRatio(){
        Log.d("xxxxxxx","keepVideoAspectRatio-----surfaceView.getHeight()="+surfaceView.getHeight()+"   surfaceView.getWidth()="+surfaceView.getWidth());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        if (getIsFullScreen()){
            if (mVideoWidth*fullScreenSurfaceHeight<mVideoHeight*fullScreenSurfaceWidth){
                fullScreenSurfaceWidth=mVideoWidth*fullScreenSurfaceHeight/mVideoHeight;
            }else if (mVideoHeight*fullScreenSurfaceWidth<mVideoWidth*fullScreenSurfaceHeight){
                fullScreenSurfaceHeight=mVideoHeight*fullScreenSurfaceWidth/mVideoWidth;
            }
            layoutParams.width=fullScreenSurfaceWidth;
            layoutParams.height=fullScreenSurfaceHeight;
        }else {
            if (mVideoWidth*normalScreenSurfaceHeight<mVideoHeight*normalScreenSurfaceWidth){
                normalScreenSurfaceWidth=mVideoWidth*normalScreenSurfaceHeight/mVideoHeight;
            }else if (mVideoHeight*fullScreenSurfaceWidth<mVideoWidth*normalScreenSurfaceHeight){
                normalScreenSurfaceHeight=mVideoHeight*normalScreenSurfaceWidth/mVideoWidth;
            }
            layoutParams.width=normalScreenSurfaceWidth;
            layoutParams.height=normalScreenSurfaceHeight;
        }



        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        Log.d("xxxxxxx","keepVideoAspectRatio-----surfaceView:"+surfaceWidth+"  "+surfaceHeight+"    video:"+mVideoWidth+"  "+mVideoHeight);
    }


    public void touchUp(){
        if (onTouchUpListener!=null){
            onTouchUpListener.onTouchUp();
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                controller.hideLightController();
                controller.hideSoundController();
            }
        },1500);
    }

    public void release(boolean clean){
        if (mMediaPlayer!=null){
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
    }

    private onTouchUpListener onTouchUpListener;

    public void setOnTouchUpListener(onTouchUpListener listener){
        this.onTouchUpListener=listener;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("onError","what="+what+"  extra="+extra+"  isPlaying="+mMediaPlayer.isPlaying());
        return false;
    }

    public interface onTouchUpListener{
        public void onTouchUp();
    }

    public interface onFullScreenListener{
        public void onFullScreen(boolean isFullScreen,View videoView);
    }

    private onFullScreenListener onFullScreenListener;

    public void setOnFullScreenListener(NetworkVideoView.onFullScreenListener onFullScreenListener) {
        this.onFullScreenListener = onFullScreenListener;
    }
}
