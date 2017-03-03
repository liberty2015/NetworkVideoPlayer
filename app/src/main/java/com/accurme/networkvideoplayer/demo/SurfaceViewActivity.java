package com.accurme.networkvideoplayer.demo;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.accurme.networkvideoplayer.R;

import java.io.IOException;

public class SurfaceViewActivity extends AppCompatActivity implements View.OnClickListener{

    private SurfaceView surfaceView;
    private MediaPlayer player;
    private AppCompatSeekBar progressBar;
    private Button play,pause,stop;
    private int position;
    private int totalDuration;
    private int mCurrentBufferPercentage;
    private final int MAXPROGRESS=1000;
    private int newPosition;

    private final int SHOW_PROGRESS=0x11;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_PROGRESS:{
                    int pos=setProgress();
                    msg=obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg,1000-(pos%1000));
                }
                break;
            }
        }
    };

    private String url="http://flv.bn.netease.com/videolib3/1605/22/auDfZ8781/HD/auDfZ8781-mobile.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);
        surfaceView= (SurfaceView) findViewById(R.id.surfaceView);
        progressBar= (AppCompatSeekBar) findViewById(R.id.progress);
        progressBar.setMax(MAXPROGRESS);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * 拖动进度条改变时调用
             * @param seekBar
             * @param progress
             * @param fromUser
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newPosition=totalDuration*progress/MAXPROGRESS;
            }

            /**
             * 开始拖动进度条时调用
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             * 停止拖动进度条时调用
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(newPosition);
            }
        });
        play= (Button) findViewById(R.id.play);
        pause= (Button) findViewById(R.id.pause);
        stop= (Button) findViewById(R.id.stop);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);

        /**
         * 设置播放时打开屏幕
         */
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new SurfaceListener());

        player=new MediaPlayer();
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                totalDuration=mp.getDuration();
                player.start();
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        });
        player.setOnBufferingUpdateListener(bufferingUpdateListener);
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();

        switch (id){
            case R.id.play:{
                position=0;
                try {
                    play();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case R.id.pause:{
                if (player.isPlaying()){
                    position=player.getCurrentPosition();
                    player.pause();
                }else {
                    replay();
                }
            }
            break;
            case R.id.stop:{
                if (player.isPlaying()){
                    player.stop();
                }
            }
            break;
        }
    }

    private void play() throws IOException {
        player.reset();

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        /**
         * 设置需要播放的视频的路径
         */
        player.setDataSource(this, Uri.parse(url));
        player.setDisplay(surfaceView.getHolder());
        player.prepareAsync();
    }

    private void replay(){
        player.seekTo(position);
        player.start();
    }

    private int setProgress(){
        Log.d("xxxxx","-----setProgress-----");
        if (player==null){
            return 0;
        }
        int position=player.getCurrentPosition();
        int duration=player.getDuration();
        if (progressBar!=null){
            if (duration>0){
                long pos=1000L*position/duration;
                progressBar.setProgress((int) pos);
            }
            progressBar.setSecondaryProgress(mCurrentBufferPercentage*10);
        }
        return position;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player.isPlaying()){
            position=player.getCurrentPosition();
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()){
            player.stop();
        }
        player.release();
    }

    private MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener=new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.d("xxxxx","buffer percent="+percent);

            mCurrentBufferPercentage=percent;
        }
    };

    class SurfaceListener implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d("xxxxx","surfaceCreated");
            if (position>0){
                replay();
            }
//            if (position>0){
//                try {
//                    play();
////                    player.seekTo(position);
//                    position=0;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d("xxxxx","surfaceCreated");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d("xxxxx","surfaceCreated");
        }
    }

}
