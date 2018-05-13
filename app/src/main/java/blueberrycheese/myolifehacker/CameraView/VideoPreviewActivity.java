package blueberrycheese.myolifehacker.CameraView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.events.ServiceEvent;


public class VideoPreviewActivity extends AppCompatActivity {
    private static final String TAG = "VideoPreviewActivity";
    private static final int ADDITIONAL_DELAY = 0;
    private static final int VIBRATION_A = 1;
    private static final int VIBRATION_B = 2;
    private static final int VIBRATION_C = 3;

    private int gestureNum = -1;
    int[] smoothcount = new int[6];

    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        setTitle("Video Preview");
        videoView = findViewById(R.id.video);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();
            }
        });
        final MessageView actualResolution = findViewById(R.id.actualResolution);

        Uri videoUri = getIntent().getParcelableExtra("video");
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                actualResolution.setTitle("Actual resolution");
                actualResolution.setMessage(mp.getVideoWidth() + " x " + mp.getVideoHeight());
                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                float videoWidth = mp.getVideoWidth();
                float videoHeight = mp.getVideoHeight();
                float viewWidth = videoView.getWidth();
                lp.height = (int) (viewWidth * (videoHeight / videoWidth));
                videoView.setLayoutParams(lp);
                playVideo();
            }
        });

        //Restart lock Timer so user can use gesture continuously
        EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));
    }

    void playVideo() {
        if (videoView.isPlaying()) return;
        videoView.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGestureEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d(TAG,"Gesture num : "+event.gestureNumber);

        switch(gestureNum){
            case 0 :
                if(smoothcount[gestureNum]>1) {
                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_A));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));
                    finish();
                    smoothcount[gestureNum]=-1;
                    resetSmoothCount();
                }
                smoothcount[gestureNum]++;

                break;
            default :
                break;

        }
    }

    public void resetSmoothCount(){
        for(int i=0;i<smoothcount.length;i++){
            smoothcount[i]=0;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop(){
        EventBus.getDefault().unregister(this);
        super.onStop();

    }
}
