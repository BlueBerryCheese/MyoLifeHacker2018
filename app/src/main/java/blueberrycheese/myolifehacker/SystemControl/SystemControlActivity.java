package blueberrycheese.myolifehacker.SystemControl;

/*
DetectMethod에 연동시켜서 하는게 중요한데 이럴때 필요한게
DetectMethod.java
DetectModel.java
DetectSendResultAction.java
이 파일의 변형이 필요하다
다른 activity에 적용할때 이 폴더를 보고 비슷하게 만들도록 하면 될거같습니다.
 */

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import blueberrycheese.myolifehacker.FontConfig;
import blueberrycheese.myolifehacker.ImageViewer.CommentGalleryActivity;
import blueberrycheese.myolifehacker.ImageViewer.GalleryActivity;
import blueberrycheese.myolifehacker.R;

import blueberrycheese.myolifehacker.events.ServiceEvent;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import blueberrycheese.myolifehacker.myo_manage.NopModel;

public class SystemControlActivity extends AppCompatActivity {
    public static final int MENU_LIST = 0;
    public static final int MENU_BYE = 1;

    /** Device Scanning Time (ms) */
    private static final long SCAN_PERIOD = 5000;

    /** Intent code for requesting Bluetooth enable */
    private static final int REQUEST_ENABLE_BT = 1;

    private static final String TAG = "BLE_Myo";
    private static final int VIBRATION_A = 1;
    private static final int VIBRATION_B = 2;
    private static final int VIBRATION_C = 3;
    private static final int ADDITIONAL_DELAY = 0;

    private BluetoothDevice bluetoothDevice;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private TextView emgDataText;
    private TextView gestureText;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    private String deviceName;
    String[] gestureString = {"WiFi On, Off", "Sound Mode Chnage ", "Volume Up", "Volume Down", "Brightness Up", "Brightness Down"};
    private LottieAnimationView animationView_system;
    private GestureSaveModel saveModel;
    private GestureSaveMethod saveMethod;
    private GestureDetectModel_System detectModel;
    private GestureDetectMethod_System detectMethod;

    private boolean myoConnection;
    private Button Option1;
    private Button Option2;
    private Button Option3;
    private Button Option4;
    private Button Option5;
    private Button Option6;
    private SystemFeature systemFeature;
    NotificationManager notificationManager;
    boolean retVal = true;
    private Context mContext;
    int[] smoothcount = new int[6];
    int gestureNum = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_systemcontrol);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if((Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)){
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);
            }
            if(!notificationManager.isNotificationPolicyAccessGranted()){
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
        systemFeature=new SystemFeature(this);
        emgDataText = (TextView)findViewById(R.id.emgDataTextView);
        gestureText = (TextView)findViewById(R.id.gestureTextView);
        mHandler = new Handler();
        mContext = this;


        FontConfig.setGlobalFont(this,getWindow().getDecorView());
        animationView_system = (LottieAnimationView) findViewById(R.id.lottie_system);
        animationView_system.setVisibility(View.INVISIBLE);
      //  final LottieAnimationView animationView_system = (LottieAnimationView) findViewById(R.id.lottie);
      //  animationView_system
        //startNopModel() will setCurrentModel to another model so Service's gesture detect model won't work! - So I commented out
//        startNopModel();
//20180430 For service
//        Intent intent = getIntent();
//        if(intent!=null){
//
//            bluetoothDevice = intent.getExtras().getParcelable("bluetoothDevice");
//            deviceName = bluetoothDevice.getName();
//            HashMap<String,View> views = new HashMap<String,View>();
//            mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views,maxDataTextView,-1);
//            mBluetoothGatt = bluetoothDevice.connectGatt(this, true, mMyoCallback);
//            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
//            Log.d(TAG,"bluetoothDevice is "+deviceName);
//
//            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//            mBluetoothAdapter = mBluetoothManager.getAdapter();
//            //onClickEMG(this);
//            //onClickDetect();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    onClickEMG(mContext);
//                    onClickDetect();
//                }
//            },SCAN_PERIOD);
//        }



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

    /** Define of BLE Callback */
//    @Override
//    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//        Log.d(TAG,"Hello onLeSacn");
//        device = bluetoothDevice;
//        if (deviceName.equals(device.getName())) {
//            mBluetoothAdapter.stopLeScan(this);
//            // Trying to connect GATT
//            HashMap<String,View> views = new HashMap<String,View>();
//
//            mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views,maxDataTextView,-1);
//            mBluetoothGatt = device.connectGatt(this, true, mMyoCallback);
//            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
//        }
//    }
    public void onClickEMG(Context context) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
            Log.d(TAG,"False EMG");
        } else {
            saveMethod  = new GestureSaveMethod(-1, context,1);
            if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                gestureText.setText("DETECT Ready");
            } else {
                gestureText.setText("Teach me \'Gesture\'");
            }
        }
    }

//    public void onClickDetect() {
//        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//            gestureText.setText("Let's Go !!");
//            /*detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList());*/
//            detectMethod = new GestureDetectMethod_System(mHandler,saveMethod.getCompareDataList(),algorithm1,systemFeature);    //아예 새롭게 각각의 detectMethod를 구현하는것이 빠를것으로 예상된다.
//            //detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList(),algorithm1);
//            detectModel = new GestureDetectModel_System(detectMethod);
//            startDetectModel();
//        }
//    }

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void startSaveModel() {
        IGestureDetectModel model = saveModel;
        model.setAction(new GestureDetectSendResultAction_System(this)); //변경
        GestureDetectModelManager.setCurrentModel(model);
    }

    public void startDetectModel() {
        IGestureDetectModel model = detectModel;
        model.setAction(new GestureDetectSendResultAction_System(this));    //변경
        GestureDetectModelManager.setCurrentModel(model);
    }

    public void startNopModel() {
        GestureDetectModelManager.setCurrentModel(new NopModel());
    }

    public void setGestureText(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                gestureText.setText(message);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.stopLeScan(SystemControlActivity.this);
//                }
//            }, SCAN_PERIOD);
//            mBluetoothAdapter.startLeScan(this);
//        }
    }

    // 마요 연결되어 있으면 애니메이션 재생
    @Subscribe(sticky = true)
    public void getMyoDevice(ServiceEvent.myoConnected_Event event) {
        myoConnection = event.connection;
        if(myoConnection) {
            animationView_system.playAnimation();
            animationView_system.loop(true);
            animationView_system.setVisibility(View.VISIBLE);
        }
        else {
            animationView_system.cancelAnimation();
            animationView_system.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d("Event","SystemEvent Gesture num : "+event.gestureNumber);
        //Send Vibration Event
        EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_A));
        //Restart lock Timer so user can use gesture continuously
        EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

        systemFeature.function(gestureNum);
        animationView_system.playAnimation();
        animationView_system.loop(true);
        animationView_system.setVisibility(View.VISIBLE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (gestureNum){
                    case 0:
                        emgDataText.setText("Wifi ON/OFF");
                        break;
                    case 1:
                        emgDataText.setText("Volume UP");
                        break;
                    case 2:
                        emgDataText.setText("Volume Down");
                        break;
                    case 3:
                        emgDataText.setText("Sound-Vibrate");
                        break;
                    case 4:
                        emgDataText.setText("Screen Brighter");
                        break;
                    case 5:
                        emgDataText.setText("Screen darker");
                        break;
                    default:
                        break;
                }
                gestureText.setText(gestureNum+" detect!!");
            }
        });
    }
    /*
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d("Event","SystemEvent Gesture num : "+event.gestureNumber);
        systemFeature.function(gestureNum);
        */
//        switch(gestureNum){
//            case 0 :
//                if(smoothcount[gestureNum]>1) {
//
//                }
//                smoothcount[gestureNum]++;
//
//                break;
//
//            case 1 :
//                if(smoothcount[gestureNum]>1) {
//
//                }
//                smoothcount[gestureNum]++;
//
//                break;
//
//            case 2 :
//                if(smoothcount[gestureNum]>1) {
//
//                }
//                smoothcount[gestureNum]++;
//                break;
//
//            case 3 :
//
//                break;
//            case 4 :
//
//                break;
//            case 5 :
//
//                break;
//            default :
//                break;
//
//        }
//    }

    public void resetSmoothCount(){
        for(int i : smoothcount){
            i = -1;
        }
    }
}
