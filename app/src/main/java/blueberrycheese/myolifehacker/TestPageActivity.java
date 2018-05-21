package blueberrycheese.myolifehacker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import blueberrycheese.myolifehacker.events.ServiceEvent;


public class TestPageActivity extends AppCompatActivity {
    private static final String TAG = "TestPageActivity";
    int[] smoothcount = new int[6];
    private int gestureNum = -1;
    private Context mContest;

    private TextView LogTextView;
    private TextView gestureTextView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"TestPageActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testpage);

        FontConfig.setGlobalFont(this,getWindow().getDecorView());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        mContest = this;

        LogTextView = findViewById(R.id.LogTextView);
        gestureTextView2 = findViewById(R.id.gestureTextView2);
    }

    @Override
    protected void onResume(){
        super.onResume();


    }

    //
    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }





    @Override
    public void onDestroy(){
        Log.d(TAG,"TestPageActivity onDestroy!");

        super.onDestroy();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceEvent.GestureEvent event) {
        String log_smoothcount;
        gestureNum = event.gestureNumber;
        Log.d(TAG,"TESTPAGE_RAW_DETECT Gesture num : "+event.gestureNumber);

        switch(gestureNum){
            case 0 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    resetSmoothCount();
                    gestureTextView2.setText("FIST Detect!");
                }


                break;

            case 1 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    resetSmoothCount();
                    gestureTextView2.setText("WAVE IN Detect!");
                }


                break;

            case 2 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    resetSmoothCount();
                    gestureTextView2.setText("WAVE OUT Detect!");
                }

                break;

            case 3 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    resetSmoothCount();
                    gestureTextView2.setText("SPREAD Detect!");
                }
                break;
            case 4 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    resetSmoothCount();
                    gestureTextView2.setText("LITTLE FINGER Detect!");
                }
                break;
            case 5:
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    resetSmoothCount();
                    gestureTextView2.setText("SCISSOR Detect!");
                }
            default :
                break;

        }
        log_smoothcount = "SMOOTH_COUNT : "+"[ " + smoothcount[0] + " ],"+"[ " + smoothcount[1] + " ],"+"[ " + smoothcount[2] + " ],"+"[ " + smoothcount[3] + " ],"+"[ " + smoothcount[4] + " ],"+"[ " + smoothcount[5] + " ]";
        LogTextView.setText(log_smoothcount);
    }

    public void resetSmoothCount(){
        for(int i=0;i<smoothcount.length;i++){
            smoothcount[i]=0;
        }
    }


}
