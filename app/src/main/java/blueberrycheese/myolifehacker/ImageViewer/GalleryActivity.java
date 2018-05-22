package blueberrycheese.myolifehacker.ImageViewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bosong.commentgallerylib.CommentGalleryContainer;
import com.bosong.commentgallerylib.CommentImageGrid;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blueberrycheese.myolifehacker.FontConfig;
import blueberrycheese.myolifehacker.MyoApp;
import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.Toasty;
import blueberrycheese.myolifehacker.events.ServiceEvent;

public class GalleryActivity extends AppCompatActivity {
    public static final String CLICK_INDEX = "CLICK_INDEX";
    public static final String COMMENT_LIST = "COMMENT_LIST";
    public static final String LIST_SIZE = "LIST_SIZE";
    private static final String SAMPLE_COMMENT = "";
    private LottieAnimationView animationView_gallery_lock;
    private LottieAnimationView animationView_gallery_unlock;
    private static final int VIBRATION_A = 1;
    private static final int VIBRATION_B = 2;
    private static final int VIBRATION_C = 3;
    private static final int ADDITIONAL_DELAY = 0;

    private boolean first=true;   ///////
    private MyoApp myoApp = null;
    private boolean myoConnection;
    private CommentImageGrid mCommentGrid;
    private TextView img_pager;
    CommentGalleryContainer commentList;
    private static String basePath;
    private String[] imgs;
    private static final String dir_path = "/MHL_Camera";
    List<String> urls;
    int[] smoothcount = new int[6];
    private int gestureNum = -1;
    private int positionNum = 0;
    private Drawable icon_1,icon_2,icon_3,icon_4,icon_5,icon_6;
    private int post_postionNum=-1;
    private int paddingDp;
    private float density;
    private int paddingPixel;
    private int pageTot;
    private static final int CURRENT_ACTIVITY = 0;
    private int lock_vibrate_state;
    private int recog_vibrate_state;
    private int conn_vibrate_state;
    private SharedPreferences sharedPreferences;  //sharePreference호출 후 적용
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //화면 꺼짐/잠금 상태에서 가능하도록
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);


        setContentView(R.layout.activity_gallery);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 화면 안꺼지게

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());  //sharePreference호출 후 적용
        setting_vibrate();

        mCommentGrid = (CommentImageGrid) findViewById(R.id.comment_grid);
        img_pager = (TextView)findViewById(R.id.img_pager);
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString(),dir_path);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
//                return null;
            }
        }
        basePath = mediaStorageDir.getPath();
        File file = new File(basePath);

        imgs = file.list();
        pageTot = imgs.length;
        urls = Arrays.asList(imgs);
        commentList = new CommentGalleryContainer(urls, SAMPLE_COMMENT);

        for(int i=0;i<urls.size();i++){
            urls.set(i,"file:///"+Environment.getExternalStorageDirectory().toString()+dir_path+"/"+urls.get(i));
            Log.d("array",urls.get(i));
        }
        paddingDp = 5;
        density = this.getResources().getDisplayMetrics().density;
        paddingPixel = (int)(paddingDp * density);

        mCommentGrid.setData(urls);
        for(int i=0;i<urls.size();i++){
            UnActiveImage(i);
            //mCommentGrid.getChildAt(i).setBackground(getResources().getDrawable(R.drawable.transparent_button));
        }
//        mCommentGrid.getChildAt(positionNum).setPadding(paddingPixel,paddingPixel,paddingPixel,paddingPixel);
//        mCommentGrid.getChildAt(positionNum).setBackground(getResources().getDrawable(R.color.color_accent));
        ActiveImage(positionNum);

        icon_1 = getResources().getDrawable(R.drawable.gesture_1_w);
        icon_2 = getResources().getDrawable(R.drawable.gesture_2_w);
        icon_3 = getResources().getDrawable(R.drawable.gesture_3_w);
        icon_4 = getResources().getDrawable(R.drawable.gesture_4_w);
        icon_5 = getResources().getDrawable(R.drawable.gesture_5_w);
        icon_6 = getResources().getDrawable(R.drawable.gesture_6_w);

        FontConfig.setGlobalFont(this,getWindow().getDecorView());
        animationView_gallery_lock = (LottieAnimationView) findViewById(R.id.lottie_gallery_lock);
        animationView_gallery_unlock = (LottieAnimationView) findViewById(R.id.lottie_gallery_unlock);

        animationView_gallery_lock.setVisibility(View.INVISIBLE);
       animationView_gallery_unlock.setVisibility(View.INVISIBLE);

        post_postionNum=positionNum;
        mCommentGrid.setOnItemClickLisener(new CommentImageGrid.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Intent it = new Intent();
                it.putExtra(CLICK_INDEX, position);
                it.putExtra(COMMENT_LIST, commentList);
                it.putExtra(LIST_SIZE,urls.size());
                it.setClass(GalleryActivity.this, CommentGalleryActivity.class);
                startActivity(it);
                ActiveImage(position);
                UnActiveImage(post_postionNum);
//                mCommentGrid.getChildAt(position).setBackground(getResources().getDrawable(R.color.color_accent));
//                mCommentGrid.getChildAt(positionNum).setBackground(getResources().getDrawable(R.drawable.transparent_button));
                post_postionNum = position;

            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        //Post event to notify that user's watching the activity.
        EventBus.getDefault().postSticky(new ServiceEvent.currentActivity_Event(CURRENT_ACTIVITY));
    }
    @Override
    public void onStop(){
//        //Post event to notify that user's leaving the activity.
//        EventBus.getDefault().postSticky(new ServiceEvent.currentActivity_Event(-1));

        EventBus.getDefault().unregister(this);
        super.onStop();
//        this.closeBLEGatt();
    }

    @Override
    public void onPause(){
        //Post event to notify that user's leaving the activity.
        EventBus.getDefault().postSticky(new ServiceEvent.currentActivity_Event(-1));
        super.onPause();
    }

    // 마요 잠기면 애니메이션 재생
    @Subscribe
    public void getMyoDevice(ServiceEvent.myoLock_Event event) {
        myoConnection = event.lock;
        if(myoConnection) {
            //  animationView_main.cancelAnimation();
            //  animationView_main.clearAnimation();
            //  animationView_main.setAnimation("lock.json");
            animationView_gallery_lock.playAnimation();
            animationView_gallery_lock.loop(true);
            animationView_gallery_lock.setVisibility(View.VISIBLE);
            animationView_gallery_unlock.setVisibility(View.INVISIBLE);
        }
        else {
            //  animationView_main.cancelAnimation();
            // animationView_main.clearAnimation();
            //animationView_main_unlock.setAnimation("material_wave_loading.json");
            animationView_gallery_unlock.playAnimation();
            animationView_gallery_unlock.loop(true);
            animationView_gallery_unlock.setVisibility(View.VISIBLE);
            animationView_gallery_lock.setVisibility(View.INVISIBLE);
        }
    }

    // 마요 연결되어 있으면 애니메이션 재생
    @Subscribe(sticky = true)
    public void getMyoDevice(ServiceEvent.myoConnected_Event event) {
        myoConnection = event.connection;
        myoApp = (MyoApp) getApplication().getApplicationContext();
        if(myoConnection) {
            if(first && !myoApp.isUnlocked()) {
                animationView_gallery_lock.playAnimation();
                animationView_gallery_lock.loop(true);
                animationView_gallery_lock.setVisibility(View.VISIBLE);
                first=false;
            }else if(first && myoApp.isUnlocked()) {
                animationView_gallery_unlock.playAnimation();
                animationView_gallery_unlock.loop(true);
                animationView_gallery_unlock.setVisibility(View.VISIBLE);
                first=false;
            }
        }
        else {
            animationView_gallery_lock.cancelAnimation();
            animationView_gallery_unlock.cancelAnimation();
            animationView_gallery_lock.setVisibility(View.INVISIBLE);
            animationView_gallery_unlock.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d("MenuEvent","MenuEvent Gesture num : "+event.gestureNumber);

        switch(gestureNum){
            case 0 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    //Send Vibration Event

                    resetSmoothCount();
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(recog_vibrate_state));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    Intent it = new Intent();
                    it.putExtra(CLICK_INDEX, positionNum);
                    it.putExtra(COMMENT_LIST, commentList);
                    it.putExtra(LIST_SIZE,urls.size());
                    it.setClass(GalleryActivity.this, CommentGalleryActivity.class);
                    startActivity(it);
                    Toasty.normal(getBaseContext(),"Open picture", Toast.LENGTH_SHORT, icon_1).show();
                }


                break;

            case 1 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {

                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(recog_vibrate_state));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    positionNum --;
                    if (positionNum>=urls.size()){
                        positionNum=0;
                    }
                    if(positionNum<0){
                        positionNum = urls.size()-1;
                    }
                    UnActiveImage(post_postionNum);
                    ActiveImage(positionNum);
//                    mCommentGrid.getChildAt(post_postionNum).setBackground(getResources().getDrawable(R.drawable.transparent_button));
//                    mCommentGrid.getChildAt(positionNum).setBackground(getResources().getDrawable(R.color.color_accent));
                    post_postionNum=positionNum;
                    Toasty.normal(getBaseContext(),"Previous picture", Toast.LENGTH_SHORT, icon_2).show();
                    resetSmoothCount();
                }


                break;

            case 2 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(recog_vibrate_state));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    positionNum ++;
                    if (positionNum>=urls.size()){
                        positionNum=0;
                    }
                    if(positionNum<0){
                        positionNum = urls.size()-1;
                    }
                    UnActiveImage(post_postionNum);
                    ActiveImage(positionNum);
//                    mCommentGrid.getChildAt(post_postionNum).setBackground(getResources().getDrawable(R.color.Transparent));
//                    mCommentGrid.getChildAt(positionNum).setBackground(getResources().getDrawable(R.color.color_accent));
                    post_postionNum=positionNum;
                    Toasty.normal(getBaseContext(),"Next Picture", Toast.LENGTH_SHORT, icon_3).show();
                    resetSmoothCount();
                }

                break;

            case 3 :

                break;

            case 5:
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(recog_vibrate_state));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    finish();
//                    Toasty.normal(getBaseContext(),"Open picture", Toast.LENGTH_SHORT, icon_1).show();

                    resetSmoothCount();
                }
            default :
                break;

        }

    }

    public void resetSmoothCount(){
        for(int i=0;i<smoothcount.length;i++){
            smoothcount[i]=0;
        }
    }

    public void ActiveImage(int position){
        img_pager.setText(""+(position+1)+"/"+pageTot);
        mCommentGrid.getChildAt(position).setPadding(paddingPixel,paddingPixel,paddingPixel,paddingPixel);
        mCommentGrid.getChildAt(position).setBackground(getResources().getDrawable(R.color.color_accent));
    }
    public void UnActiveImage(int position){
        mCommentGrid.getChildAt(position).setPadding(0,0,0,0);
        mCommentGrid.getChildAt(position).setBackground(getResources().getDrawable(R.color.Transparent));
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
