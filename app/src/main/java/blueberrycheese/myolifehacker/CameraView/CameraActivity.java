package blueberrycheese.myolifehacker.CameraView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
//import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Grid;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.Size;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.events.ServiceEvent;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
import blueberrycheese.myolifehacker.myo_manage.NopModel;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, ControlView.Callback {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camera);
//    }

    private CameraView camera;
    private ViewGroup controlPanel;

    private boolean mCapturingPicture;
    private boolean mCapturingVideo;

    // To show stuff in the callback
    private Size mCaptureNativeSize;
    private long mCaptureTime;

    boolean videoRecording = false;
//    Button dttButton;

    //Detect용 가져옴
    /** Device Scanning Time (ms) */
    private static final long SCAN_PERIOD = 5000;

    private static final String TAG = "CameraActivity";
//    private BluetoothDevice bluetoothDevice;
//    private Handler mHandler;
//    private BluetoothAdapter mBluetoothAdapter;
//    private BluetoothGatt mBluetoothGatt;
    private TextView gestureText;
//    private MyoGattCallback mMyoCallback;
//    private MyoCommandList commandList = new MyoCommandList();
//    private String deviceName;
    String[] gestureString = {"WiFi On, Off", "Sound Mode Chnage ", "Volume Up", "Volume Down", "Brightness Up", "Brightness Down"};

//    private GestureSaveModel saveModel;
//    private GestureSaveMethod saveMethod;
//    private GestureDetectModel_Camera detectModel;
//    private GestureDetectMethod_Camera detectMethod;

    private int gestureNum = -1;
    private Flash currentCameraFlash = Flash.OFF;
    private Grid currentGrid = Grid.OFF;
    int[] smoothcount = new int[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_camera);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);

        setTitle("Camera");

        camera = findViewById(R.id.camera);
        camera.addCameraListener(new CameraListener() {
            public void onCameraOpened(CameraOptions options) { onOpened(); }
            public void onPictureTaken(byte[] jpeg) { onPicture(jpeg); }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                onVideo(video);
            }
        });

        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.capturePhoto).setOnClickListener(this);
        findViewById(R.id.captureVideo).setOnClickListener(this);
        findViewById(R.id.toggleCamera).setOnClickListener(this);

        controlPanel = findViewById(R.id.controls);
        ViewGroup group = (ViewGroup) controlPanel.getChildAt(0);
        Control[] controls = Control.values();
        for (Control control : controls) {
            ControlView view = new ControlView(this, control, this);
            group.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        controlPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
                b.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        //Detect용
//        gestureText = (TextView)findViewById(R.id.cameraActivityGesture);

        //startNopModel() will setCurrentModel to another model so Service's gesture detect model won't work! - So I commented out
//        startNopModel();

        //Comment out for Service
//        Intent intent = getIntent();
//        if(intent!=null){
//            bluetoothDevice = intent.getExtras().getParcelable("bluetoothDevice");
//            if(bluetoothDevice != null){
//                deviceName = bluetoothDevice.getName();
//                HashMap<String,View> views = new HashMap<String,View>();
//                mMyoCallback = new MyoGattCallback(mHandler);
//                mBluetoothGatt = bluetoothDevice.connectGatt(this, false, mMyoCallback);
//                mMyoCallback.setBluetoothGatt(mBluetoothGatt);
//                Log.d(TAG,"bluetoothDevice is "+deviceName);
//                BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//                mBluetoothAdapter = mBluetoothManager.getAdapter();
//            }
//
//        }
//        dttButton = (Button) findViewById(R.id.dttButton);
    }

    private void message(String content, boolean important) {
        int length = important ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this, content, length).show();
    }

    private void onOpened() {
        ViewGroup group = (ViewGroup) controlPanel.getChildAt(0);
        for (int i = 0; i < group.getChildCount(); i++) {
            ControlView view = (ControlView) group.getChildAt(i);
            view.onCameraOpened(camera);
        }
    }

    private void onPicture(byte[] jpeg) {
        mCapturingPicture = false;
        long callbackTime = System.currentTimeMillis();
        if (mCapturingVideo) {
            message("Captured while taking video. Size="+mCaptureNativeSize, false);
            return;
        }

        // This can happen if picture was taken with a gesture.
        if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
        if (mCaptureNativeSize == null) mCaptureNativeSize = camera.getPictureSize();

        PicturePreviewActivity.setImage(jpeg);
        Intent intent = new Intent(CameraActivity.this, PicturePreviewActivity.class);
        intent.putExtra("delay", callbackTime - mCaptureTime);
        intent.putExtra("nativeWidth", mCaptureNativeSize.getWidth());
        intent.putExtra("nativeHeight", mCaptureNativeSize.getHeight());
        startActivity(intent);

        mCaptureTime = 0;
        mCaptureNativeSize = null;
    }

    private void onVideo(File video) {
        mCapturingVideo = false;
        Log.d("VideoUri","Video Uri"+Uri.fromFile(video));

        Intent intent = new Intent(CameraActivity.this, VideoPreviewActivity.class);
        intent.putExtra("video", Uri.fromFile(video));
        startActivity(intent);
        saveVideo(Uri.fromFile(video));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit: edit(); break;
            case R.id.capturePhoto: capturePhoto(); break;
            case R.id.captureVideo:
                if(videoRecording == false){
                    videoRecording = true;
                    captureVideo();
                } else if(videoRecording == true){
                    videoRecording = false;
                    camera.stopCapturingVideo();
                }
                break;
            case R.id.toggleCamera: toggleCamera(); break;
        }
    }

    @Override
    public void onBackPressed() {
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        if (b.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            b.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        super.onBackPressed();
    }

    private void edit() {
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        b.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void capturePhoto() {
        if (mCapturingPicture) return;
        mCapturingPicture = true;
        mCaptureTime = System.currentTimeMillis();
        mCaptureNativeSize = camera.getPictureSize();
        message("Capturing picture...", false);
        camera.capturePicture();
    }

    private void captureVideo() {
        if (camera.getSessionType() != SessionType.VIDEO) {
            message("Can't record video while session type is 'picture'.", false);
            return;
        }
        if (mCapturingPicture || mCapturingVideo) return;
        mCapturingVideo = true;
        message("Recording...", true);
//        camera.startCapturingVideo(null, 8000);
        camera.startCapturingVideo(null);
    }

    private void toggleCamera() {
        if (mCapturingPicture) return;
        switch (camera.toggleFacing()) {
            case BACK:
                message("Switched to back camera!", false);
                break;

            case FRONT:
                message("Switched to front camera!", false);
                break;
        }
    }

    @Override
    public boolean onValueChanged(Control control, Object value, String name) {
        if (!camera.isHardwareAccelerated() && (control == Control.WIDTH || control == Control.HEIGHT)) {
            if ((Integer) value > 0) {
                message("This device does not support hardware acceleration. " +
                        "In this case you can not change width or height. " +
                        "The view will act as WRAP_CONTENT by default.", true);
                return false;
            }
        }
        control.applyValue(camera, value);
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        b.setState(BottomSheetBehavior.STATE_HIDDEN);
        message("Changed " + control.getName() + " to " + name, false);
        return true;
    }

    //region Boilerplate

    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        camera.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stop();
//        emgOff();
//        detectOn = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
//        emgOff();
//        detectOn = false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !camera.isStarted()) {
            camera.start();
        }
    }

    //endregion

    //Detect용 추가

//    /** Define of BLE Callback */
//    @Override
//    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//        Log.d(TAG,"Hello onLeSacn");
//        device = bluetoothDevice;
//        if (deviceName.equals(device.getName())) {
//            mBluetoothAdapter.stopLeScan(this);
//            // Trying to connect GATT
//            HashMap<String,View> views = new HashMap<String,View>();
//
//            mMyoCallback = new MyoGattCallback(mHandler);
//            mBluetoothGatt = device.connectGatt(this, false, mMyoCallback);
//            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
//        }
//    }


    @Override
    public void onStop(){
        EventBus.getDefault().unregister(this);
        super.onStop();
//        this.closeBLEGatt();

    }

//    public void closeBLEGatt() {
//        if (mBluetoothGatt == null) {
//            return;
//        }
//        mMyoCallback.stopCallback();
//        mBluetoothGatt.close();
//        mBluetoothGatt = null;
//    }

//    public void startSaveModel() {
//        IGestureDetectModel model = saveModel;
//        model.setAction(new GestureDetectSendResultAction_Camera(this)); //변경
//        GestureDetectModelManager.setCurrentModel(model);
//    }
//
//    public void startDetectModel() {
//        IGestureDetectModel model = detectModel;
//        model.setAction(new GestureDetectSendResultAction_Camera(this));    //변경
//        GestureDetectModelManager.setCurrentModel(model);
//    }
//
//    public void startNopModel() {
//        GestureDetectModelManager.setCurrentModel(new NopModel());
//    }
//
//    public void setGestureText(final String message) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                gestureText.setText(message);
//            }
//        });
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.stopLeScan(CameraActivity.this);
//                }
//            }, SCAN_PERIOD);
//            mBluetoothAdapter.startLeScan(this);
//        }
//
//    }

//    boolean detectOn = false;
//
//
//    public void onClickdtt(View v) {
//        if(detectOn == false){
//            if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
//                Log.d(TAG,"False EMG");
//            } else {
//                saveMethod  = new GestureSaveMethod(-1, this,1);
//                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//                    gestureText.setText("DETECT Ready");
//                } else {
//                    gestureText.setText("Teach me \'Gesture\'");
//                }
//                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//                    gestureText.setText("Let's Go !!");
//                    /*detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList());*/
//                    detectMethod = new GestureDetectMethod_Camera(mHandler,saveMethod.getCompareDataList());    //아예 새롭게 각각의 detectMethod를 구현하는것이 빠를것으로 예상된다.
//                    //detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList(),algorithm1);
//                    detectModel = new GestureDetectModel_Camera(detectMethod);
//                    startDetectModel();
//                }
//                detectOn = true;
//                dttButton.setText("On");
//
//            }
//        } else if(detectOn == true){
//            if (mBluetoothGatt == null
//                    || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
//                    || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())) {
//                Log.d(TAG,"False Data Stop");
//            }
//            detectOn = false;
//            dttButton.setText("Off");
//        }
//    }

//    public void emgOff(){
//        if (mBluetoothGatt == null
//                || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
//                || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())){
//            Log.d(TAG,"Data Stop");
//
//        }
//    }

    public void saveVideo(Uri videoUri){
        String root = Environment.getExternalStorageDirectory().toString();
        String video_name = "" + System.currentTimeMillis();

        File myDir = new File(root+"/MHL_Camera");
        myDir.mkdirs();
        String fname = "MHL_Video_" + video_name+ ".mp4";
        File file = new File(myDir, fname);
        if (file.exists()){
            file.delete();
        }
//        Log.i("LOAD", root + fname);
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try{
            FileOutputStream newVideo = new FileOutputStream(file);
            FileInputStream tempVideo = new FileInputStream(videoUri.getPath());

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = tempVideo.read(buf)) > 0) {
                newVideo.write(buf, 0, len);
            }
            newVideo.flush();
            newVideo.close();
            tempVideo.close();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGestureEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d(TAG,"CameraEvent Gesture num : "+event.gestureNumber);

        switch(gestureNum){
            case 0 :
                if(smoothcount[gestureNum]>1) {
                    capturePhoto();
                    smoothcount[gestureNum]=-1;
                    resetSmoothCount();
                }
                smoothcount[gestureNum]++;

                break;

            case 1 :
                if(smoothcount[gestureNum]>1) {
                    switch(currentCameraFlash){
                        case OFF:
                            camera.setFlash(Flash.OFF);
                            break;
                        case ON:
                            camera.setFlash(Flash.AUTO);
                            break;
                        case AUTO:
                            camera.setFlash(Flash.TORCH);
                            break;
                        case TORCH:
                            camera.setFlash(Flash.OFF);
                            break;
                        default:
                            break;
                    }

                    smoothcount[gestureNum]=-1;
                    resetSmoothCount();
                }
                smoothcount[gestureNum]++;
                break;

            case 2 :
                if(smoothcount[gestureNum]>1) {
                    switch(currentGrid){
                        case OFF:
                            camera.setGrid(Grid.DRAW_3X3);
                            break;
                        case DRAW_3X3:
                            camera.setGrid(Grid.OFF);
                            break;
                        default:
                            break;
                    }

                    smoothcount[gestureNum]=-1;
                    resetSmoothCount();
                }
                smoothcount[gestureNum]++;

                break;

            case 3 :
                if(smoothcount[gestureNum]>1) {
                    camera.setSessionType(SessionType.VIDEO);
                    if(videoRecording == false){
                        videoRecording = true;
                        captureVideo();
                    } else if(videoRecording == true){
                        videoRecording = false;
                        camera.stopCapturingVideo();
                    }

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
}
