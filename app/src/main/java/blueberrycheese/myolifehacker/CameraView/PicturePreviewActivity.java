package blueberrycheese.myolifehacker.CameraView;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.otaliastudios.cameraview.AspectRatio;
import com.otaliastudios.cameraview.CameraUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.events.ServiceEvent;


public class PicturePreviewActivity extends AppCompatActivity {
    private static final String TAG = "PicturePreviewActivity";
    private int gestureNum = -1;
    int[] smoothcount = new int[6];

    private static WeakReference<byte[]> image;

    public static void setImage(@Nullable byte[] im) {
        image = im != null ? new WeakReference<>(im) : null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        setTitle("Picture Preview");

        final ImageView imageView = findViewById(R.id.image);
        final MessageView nativeCaptureResolution = findViewById(R.id.nativeCaptureResolution);
        // final MessageView actualResolution = findViewById(R.id.actualResolution);
        // final MessageView approxUncompressedSize = findViewById(R.id.approxUncompressedSize);
        final MessageView captureLatency = findViewById(R.id.captureLatency);

        final long delay = getIntent().getLongExtra("delay", 0);
        final int nativeWidth = getIntent().getIntExtra("nativeWidth", 0);
        final int nativeHeight = getIntent().getIntExtra("nativeHeight", 0);
        byte[] b = image == null ? null : image.get();
        if (b == null) {
            finish();
            return;
        }

        CameraUtils.decodeBitmap(b, 1000, 1000, new CameraUtils.BitmapCallback() {
            @Override
            public void onBitmapReady(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
                saveImage(bitmap, "" + System.currentTimeMillis());

                // approxUncompressedSize.setTitle("Approx. uncompressed size");
                // approxUncompressedSize.setMessage(getApproximateFileMegabytes(bitmap) + "MB");

                captureLatency.setTitle("Approx. capture latency");
                captureLatency.setMessage(delay + " milliseconds");

                // ncr and ar might be different when cropOutput is true.
                AspectRatio nativeRatio = AspectRatio.of(nativeWidth, nativeHeight);
                nativeCaptureResolution.setTitle("Native capture resolution");
                nativeCaptureResolution.setMessage(nativeWidth + "x" + nativeHeight + " (" + nativeRatio + ")");

                // AspectRatio finalRatio = AspectRatio.of(bitmap.getWidth(), bitmap.getHeight());
                // actualResolution.setTitle("Actual resolution");
                // actualResolution.setMessage(bitmap.getWidth() + "x" + bitmap.getHeight() + " (" + finalRatio + ")");
            }
        });

    }

    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024 / 1024;
    }

    private void saveImage(Bitmap finalBitmap, String image_name) {
        String root = Environment.getExternalStorageDirectory().toString();
//        String time = "" + System.currentTimeMillis();
        File myDir = new File(root+"/MHL_Camera");
        myDir.mkdirs();
        String fname = "MHL_Image_" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()){
            file.delete();
        }
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGestureEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d(TAG,"Gesture num : "+event.gestureNumber);

        switch(gestureNum){
            case 0 :
                if(smoothcount[gestureNum]>1) {
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
        for(int i : smoothcount){
            i = -1;
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
