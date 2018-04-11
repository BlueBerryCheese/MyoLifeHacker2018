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

import java.util.HashMap;

import blueberrycheese.myolifehacker.R;

import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import blueberrycheese.myolifehacker.myo_manage.NopModel;

public class SystemControlActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    public static final int MENU_LIST = 0;
    public static final int MENU_BYE = 1;

    /** Device Scanning Time (ms) */
    private static final long SCAN_PERIOD = 5000;

    /** Intent code for requesting Bluetooth enable */
    private static final int REQUEST_ENABLE_BT = 1;

    private static final String TAG = "BLE_Myo";
    private BluetoothDevice bluetoothDevice;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private TextView emgDataText;
    private TextView gestureText;
    private TextView maxDataTextView;
    private TextView algorithm1;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();

    private String deviceName;
    String[] gestureString = {"WiFi On, Off", "Sound Mode Chnage ", "Volume Up", "Volume Down", "Brightness Up", "Brightness Down"};

    private GestureSaveModel saveModel;
    private GestureSaveMethod saveMethod;
    private GestureDetectModel_System detectModel;
    private GestureDetectMethod_System detectMethod;

    private Button Option1;
    private Button Option2;
    private Button Option3;
    private Button Option4;
    private Button Option5;
    private Button Option6;
    private SystemFeature systemFeature;
    NotificationManager notificationManager;
    boolean retVal = true;

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
        algorithm1 = (TextView)findViewById(R.id.algorithm1);
        maxDataTextView=(TextView)findViewById(R.id.maxData);

        startNopModel();

        Intent intent = getIntent();
        if(intent!=null){

            bluetoothDevice = intent.getExtras().getParcelable("bluetoothDevice");
            deviceName = bluetoothDevice.getName();
            HashMap<String,View> views = new HashMap<String,View>();
            mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views,maxDataTextView,-1);
            mBluetoothGatt = bluetoothDevice.connectGatt(this, false, mMyoCallback);
            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
            Log.d(TAG,"bluetoothDevice is "+deviceName);
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();


        }

    }
    @Override
    public void onStop(){
        super.onStop();
        this.closeBLEGatt();
    }

    /** Define of BLE Callback */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(TAG,"Hello onLeSacn");
        device = bluetoothDevice;
        if (deviceName.equals(device.getName())) {
            mBluetoothAdapter.stopLeScan(this);
            // Trying to connect GATT
            HashMap<String,View> views = new HashMap<String,View>();

            mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views,maxDataTextView,-1);
            mBluetoothGatt = device.connectGatt(this, false, mMyoCallback);
            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
        }
    }
    public void onClickEMG(View v) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
            Log.d(TAG,"False EMG");
        } else {
            saveMethod  = new GestureSaveMethod(-1, this);
            if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                gestureText.setText("DETECT Ready");
            } else {
                gestureText.setText("Teach me \'Gesture\'");
            }
        }
    }

    public void onClickNoEMG(View v) {
        if (mBluetoothGatt == null
                || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
                || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())) {
            Log.d(TAG,"False Data Stop");
        }

    }

    public void onClickSave(View v) {
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Ready ||
                saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
            saveModel   = new GestureSaveModel(saveMethod,-1);
            startSaveModel();
        } else if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Not_Saved) {
            startSaveModel();
        }
        saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);

        gestureText.setText("Saving ; " + gestureString[saveMethod.getGestureCounter()] + " Gesture");
    }

    public void onClickDetect(View v) {
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
            gestureText.setText("Let's Go !!");
            /*detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList());*/
            detectMethod = new GestureDetectMethod_System(mHandler,saveMethod.getCompareDataList(),algorithm1,systemFeature);    //아예 새롭게 각각의 detectMethod를 구현하는것이 빠를것으로 예상된다.
            //detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList(),algorithm1);
            detectModel = new GestureDetectModel_System(detectMethod);
            startDetectModel();
        }
    }

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
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(SystemControlActivity.this);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(this);
        }
    }

}
