package blueberrycheese.myolifehacker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

/**
 * Created by pjw12 on 2018-05-07.
 */

public class SplashActivity extends Activity{
    private Handler handler;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        handler = new Handler();
        setContentView(R.layout.activity_splash);
        context=this;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }
        },2000);
//        try {
//            Thread.sleep(2000);
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//        }



    }
}
