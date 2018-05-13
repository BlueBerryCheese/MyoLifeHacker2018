package blueberrycheese.myolifehacker.ImageViewer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

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

import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.events.ServiceEvent;

public class GalleryActivity extends AppCompatActivity {
    public static final String CLICK_INDEX = "CLICK_INDEX";
    public static final String COMMENT_LIST = "COMMENT_LIST";
    public static final String LIST_SIZE = "LIST_SIZE";
    private static final String SAMPLE_COMMENT = "";
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
    private int post_postionNum=-1;
    private int paddingDp;
    private float density;
    private int paddingPixel;
    private int pageTot;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
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
    }
    @Override
    public void onStop(){
        EventBus.getDefault().unregister(this);
        super.onStop();
//        this.closeBLEGatt();
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
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent());
                    Intent it = new Intent();
                    it.putExtra(CLICK_INDEX, positionNum);
                    it.putExtra(COMMENT_LIST, commentList);
                    it.putExtra(LIST_SIZE,urls.size());
                    it.setClass(GalleryActivity.this, CommentGalleryActivity.class);
                    startActivity(it);

                }


                break;

            case 1 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {

                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent());
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
                    resetSmoothCount();
                }


                break;

            case 2 :
                smoothcount[gestureNum]++;
                if(smoothcount[gestureNum]>1) {
                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent());
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
                    resetSmoothCount();
                }

                break;

            case 3 :

                break;

            default :
                break;

        }

    }

    public void resetSmoothCount(){
        for(int i : smoothcount){
            i = -1;
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

}
