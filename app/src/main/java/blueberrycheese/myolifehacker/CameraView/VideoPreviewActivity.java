package blueberrycheese.myolifehacker.CameraView;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    private static final int CURRENT_ACTIVITY = 2;

    private int gestureNum = -1;
    int[] smoothcount = new int[6];

    private VideoView videoView;
    private int lock_vibrate_state;
    private int recog_vibrate_state;
    private int conn_vibrate_state;
    private SharedPreferences sharedPreferences;  //sharePreference호출 후 적용
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        setTitle("Video Preview");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());  //sharePreference호출 후 적용
        setting_vibrate();
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
        //Post event to notify that user's watching the activity.
        EventBus.getDefault().postSticky(new ServiceEvent.currentActivity_Event(CURRENT_ACTIVITY));
    }

    @Override
    public void onStop(){
        //Post event to notify that user's leaving the activity.
        EventBus.getDefault().postSticky(new ServiceEvent.currentActivity_Event(-1));
        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    public void setting_vibrate(){
        String lock_vp = sharedPreferences.getString("lock_vibrate_power","강하게");
        String recog_vp = sharedPreferences.getString("recog_vibrate_power","강하게");
        String conn_vp = sharedPreferences.getString("conn_vibrate_power","강하게");
        int lock_vpp,recog_vpp,conn_vpp;

        boolean iv = sharedPreferences.getBoolean("vibrate",true);
        if(iv){
            if(lock_vp.equals("강하게"))
                lock_vpp=3;
            else if(lock_vp.equals("보통"))
                lock_vpp=2;
            else if(lock_vp.equals("약하게"))
                lock_vpp=1;
            else
                lock_vpp=3;

            if(recog_vp.equals("강하게"))
                recog_vpp=3;
            else if(recog_vp.equals("보통"))
                recog_vpp=2;
            else if(recog_vp.equals("약하게"))
                recog_vpp=1;
            else
                recog_vpp=3;

            if(conn_vp.equals("강하게"))
                conn_vpp=3;
            else if(conn_vp.equals("보통"))
                conn_vpp=2;
            else if(conn_vp.equals("약하게"))
                conn_vpp=1;
            else
                conn_vpp=3;
        }else{
            lock_vpp=0;
            recog_vpp=0;
            conn_vpp=0;
        }
        lock_vibrate_state = lock_vpp;
        recog_vibrate_state = recog_vpp;
        conn_vibrate_state = conn_vpp;
    }
}
