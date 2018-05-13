package blueberrycheese.myolifehacker.ImageViewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.bosong.commentgallerylib.CommentGallery;
import com.bosong.commentgallerylib.CommentGalleryContainer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.events.ServiceEvent;

public class CommentGalleryActivity extends AppCompatActivity {
    private int numCounter = 0;
    private CommentGallery mGallery;
    int[] smoothcount = new int[6];
    private int gestureNum = -1;
    private int max_size=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거

        mGallery = (CommentGallery) findViewById(R.id.comment_gallery);
        mGallery.setData((CommentGalleryContainer) getIntent().getSerializableExtra(GalleryActivity.COMMENT_LIST),
                getIntent().getExtras().getInt(GalleryActivity.CLICK_INDEX));
        numCounter = getIntent().getExtras().getInt(GalleryActivity.CLICK_INDEX);
        max_size = getIntent().getExtras().getInt(GalleryActivity.LIST_SIZE);
        Log.d("commentGalleryActivity",numCounter+""+max_size);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d("MenuEvent","MenuEvent Gesture num : "+event.gestureNumber);

        switch(gestureNum){
            case 0 :
//                if(smoothcount[gestureNum]>1) {
//                    mGallery.setData((CommentGalleryContainer) getIntent().getSerializableExtra(GalleryActivity.COMMENT_LIST),numCounter)
//                }
//                smoothcount[gestureNum]++;

                break;

            case 1 :

                smoothcount[gestureNum]++;
                if(numCounter<0){
                    numCounter=max_size-1;
                }
                if(smoothcount[gestureNum]>1) {
                    numCounter--;
                    mGallery.setData((CommentGalleryContainer) getIntent().getSerializableExtra(GalleryActivity.COMMENT_LIST),numCounter);
                    resetSmoothCount();
                }


                break;

            case 2 :

                smoothcount[gestureNum]++;
                if(numCounter>=max_size){
                    numCounter=0;
                }
                if(smoothcount[gestureNum]>1) {
                    numCounter++;
                    mGallery.setData((CommentGalleryContainer) getIntent().getSerializableExtra(GalleryActivity.COMMENT_LIST),numCounter);
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
}
