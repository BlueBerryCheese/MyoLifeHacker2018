package blueberrycheese.myolifehacker.myo_manage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.EventLog;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.events.ServiceEvent;

public class MyoService extends Service {
    private static final String TAG = "Myo_Service";
    //Previous SCAN_PERIOD was 5000.
    private static final long SCAN_PERIOD = 3000;


    NotificationManager manager;
    Notification myNotication;

    BluetoothDevice bluetoothDevice;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    private BluetoothDevice myoDevice;
    private BluetoothAdapter mBluetoothAdapter;

    private GestureSaveMethod saveMethod;
    private GestureDetectModel detectModel;
    private GestureDetectMethod detectMethod;
    private IGestureDetectModel model;

    private int gestureNum = -1;
    private int postCount = 0;

    public MyoService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e("ServiceEvent", "onStartCommand");
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }


        mHandler = new Handler();
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        mBluetoothAdapter = mBluetoothManager.getAdapter();



        //Notification
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent nintent = new Intent("com.rj.notitfications.SECACTIVITY");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, nintent, 0);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setAutoCancel(false);
        builder.setTicker("this is ticker text");
        builder.setContentTitle("Life Hacker Activated");
        builder.setContentText("......");
        builder.setSmallIcon(R.drawable.ic_menu_myo);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
//        builder.setSubText("This is subtext...");   //API level 16
        builder.setNumber(100);
        builder.build();

        myNotication = builder.getNotification();
        manager.notify(11, myNotication);
        //

        return START_STICKY;
    }


  @Override
  public void onDestroy(){
      EventBus.getDefault().unregister(this);
      manager.cancel(11);
      super.onDestroy();

  }

  @Subscribe(sticky = true)
  public void getMyoDevice(ServiceEvent.MyoDeviceEvent event){
      Log.e(TAG, "@Subcribe getMyoDevice got an event");
      if(event.MyoDevice!=null){
          Log.e(TAG, event.MyoDevice.getName() + " arrived at service !!");
        myoDevice = event.MyoDevice;
        mMyoCallback = new MyoGattCallback(mHandler);
        mBluetoothGatt = myoDevice.connectGatt(getApplicationContext(), false, mMyoCallback);
        mMyoCallback.setBluetoothGatt(mBluetoothGatt);

//  Made separated runnable()... Look down below
//
//        new Handler().postDelayed(new Runnable() {
//              @Override
//              public void run() {
//                  if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
//                      Log.d(TAG,"False EMG");
//                      Log.d("EMGFALSETest","mBluetoothGatt : " + mBluetoothGatt);//
////call stopSelf() for killing service
////                      stopSelf();
//                  } else {
//                      saveMethod  = new GestureSaveMethod(-1, getApplicationContext(),1);
//                      Log.d(TAG,"True EMG");
//                      if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//                          detectMethod = new GestureDetectMethod(mHandler, saveMethod.getCompareDataList());    //아예 새롭게 각각의 detectMethod를 구현하는것이 빠를것으로 예상된다.
//                          detectModel = new GestureDetectModel(detectMethod);
//                          startDetectModel();
//                      }
//
//                      if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//                          //gestureText.setText("DETECT Ready");
//                      } else {
//                          //gestureText.setText("Teach me \'Gesture\'");
//                      }
//                  }
//              }
//          },SCAN_PERIOD);
          new Handler().postDelayed(runMethodModel, SCAN_PERIOD);

      }
  }

    private final Runnable runMethodModel = new Runnable(){
        public void run(){
            postCount++;
            if(postCount > 2){
                Log.d(TAG,"postDelayed already executed enough! Something's wrong now.");
                stopSelf();
                return;
            }
//            Log.d("EMGFALSETest","mMyoCallbaack.setMyoControlCommand-before if clause: " + mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly()));
            if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
                Log.d(TAG,"False EMG");
                Log.d(TAG,"mBluetoothGatt : " + mBluetoothGatt);
//                Log.d("EMGFALSETest","mMyoCallbaack.setMyoControlCommand-sendEmgOnlyFirst: " + mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly()));
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        Log.d(TAG,"mMyoCallbaack.setMyoControlCommand - PostDelayed running again");
                        new Handler().post(runMethodModel);
                    }
                },1000);
//                Log.d("EMGFALSETest","mMyoCallbaack.setMyoControlCommand-sendEmgOnlyLast: " + mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly()));
//call stopSelf() for killing service
//                      stopSelf();
            } else {
                saveMethod  = new GestureSaveMethod(-1, getApplicationContext(),1);
                Log.d(TAG,"True EMG");
                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                    detectMethod = new GestureDetectMethod(mHandler, saveMethod.getCompareDataList());    //아예 새롭게 각각의 detectMethod를 구현하는것이 빠를것으로 예상된다.
                    detectModel = new GestureDetectModel(detectMethod);
                    startDetectModel();
                }

                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                    //gestureText.setText("DETECT Ready");
                } else {
                    //gestureText.setText("Teach me \'Gesture\'");
                }
            }
        }
    };

    //아래에서 NumberSmoother에서 post한 gestureNumber(i_element)를 받음.
    @Subscribe
    public void getGestureNumber(ServiceEvent.GestureEvent event){
        gestureNum = event.gestureNumber;
        Log.d(TAG,"Gesture num : "+event.gestureNumber);
    }


    @Subscribe(sticky = true)
    public void getMyoDevice(ServiceEvent.testEvent event){
        Log.e(TAG, event.text + " arrived at service !!");
    }

    public void startDetectModel() {
//        IGestureDetectModel model = detectModel;
        model = detectModel;
//        model.setAction(new GestureDetectSendResultAction(this));    //변경
        Log.e(TAG,"In MyoService - model : " + model);
        GestureDetectModelManager.setCurrentModel(model);
        EventBus.getDefault().post(new ServiceEvent.setDetectModel_Event(1));
    }

    @Subscribe
    public void setDetectModel(ServiceEvent.setDetectModel_Event event){
        if(event.set == 1){
            GestureDetectModelManager.setCurrentModel(model);
            Log.e(TAG,"setDetectModel called");
        }
    }

//    @Subscribe(sticky = true)
//    public void getMyoDevice_String(ServiceEvent.MyoDevice_StringEvent event){
//        Log.e("serviceevent", event.MyoDevice_String + " arrived at service !!");
//        bluetoothDevice = event.MyoDevice_String;
//    }



}



/*
-How should I build-

Get Bluetooth device. Connect.
Make detect method, model. Start detecting gestures.
When doing adaptive(Fragment3), need to stop detecting and change to save mode.
Make save method, model. Maybe These can be done inside TabFragement3.

Most important thing is that service should hold bluetooth connection and also give EMG characteristic data.
So service will keep calling "event" method so detect/save model can get the emg data.

 */