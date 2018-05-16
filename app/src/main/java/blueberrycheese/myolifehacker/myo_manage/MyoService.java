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
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import blueberrycheese.myolifehacker.MainActivity;
import blueberrycheese.myolifehacker.MyoApp;
import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.TabFragment1;
import blueberrycheese.myolifehacker.Toasty;
import blueberrycheese.myolifehacker.events.ServiceEvent;

public class MyoService extends Service {
    private static final String TAG = "Myo_Service";
    //Previous SCAN_PERIOD was 5000.
    private static final long SCAN_PERIOD = 5000;
    private static final int TIMETOLOCK = 8000;

    private static final int VIBRATION_A = 1;
    private static final int VIBRATION_B = 2;
    private static final int VIBRATION_C = 3;
    private int vibrate_state=3;
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
    private Drawable locked, unlocked;
    private int gestureNum = -1;
    private int falseCount = 0;

    private MyoApp myoApp = null;
    public int[] smoothcount = new int[6];
    private final int FIST = 0;
    private final int LITTLEFINGER = 4;
    private final int SCISSORS = 5;
    private SharedPreferences sharedPreferences;
    private int currentActivity;

    private final int DEFAULT_ACTIVITY = 0;
    private final int MUSIC_ACTIVITY = 1;
    private final int CAMERA_ACTIVITY = 2;


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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());  //sharePreference호출 후 적용
        Log.d(TAG,"recognizing_count : " + sharedPreferences.getString("recognizing_count","30"));
        locked = getResources().getDrawable(R.drawable.locked);
        unlocked = getResources().getDrawable(R.drawable.unlocked);
        String vp = sharedPreferences.getString("vibrate_power","강하게");
        int vpp;

        boolean iv = sharedPreferences.getBoolean("vibrate",true);
        if(iv){
            if(vp.equals("강하게"))
                vpp=3;
            else if(vp.equals("보통"))
                vpp=2;
            else if(vp.equals("약하게"))
                vpp=1;
            else
                vpp=3;
        }else{
            vpp=0;
        }
        vibrate_state = vpp;
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
        myoApp = (MyoApp) getApplicationContext();

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

//            Log.d("EMGFALSETest","mMyoCallbaack.setMyoControlCommand-before if clause: " + mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly()));
            if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
                falseCount++;
                if(falseCount > 3){
                    Log.d(TAG,"postDelayed already executed enough! Something's wrong now.");
                    stopSelf();
                    return;
                }
                Log.d(TAG,"False EMG");
                Log.d(TAG,"mBluetoothGatt : " + mBluetoothGatt);
//                Log.d("EMGFALSETest","mMyoCallbaack.setMyoControlCommand-sendEmgOnlyFirst: " + mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly()));
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        Log.d(TAG,"mMyoCallbaack.setMyoControlCommand - PostDelayed running again");
                        new Handler().post(runMethodModel);
                    }
                },1800);
//                Log.d("EMGFALSETest","mMyoCallbaack.setMyoControlCommand-sendEmgOnlyLast: " + mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly()));
//call stopSelf() for killing service
//                      stopSelf();
            } else {
                saveMethod  = new GestureSaveMethod(-1, getApplicationContext(),1);
                Log.d(TAG,"True EMG");
                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {

                    int recog_cnt = Integer.parseInt(sharedPreferences.getString("recognizing_count","30"));
                    detectMethod = new GestureDetectMethod(mHandler, saveMethod.getCompareDataList(),recog_cnt);    //아예 새롭게 각각의 detectMethod를 구현하는것이 빠를것으로 예상된다.
                    detectModel = new GestureDetectModel(detectMethod);
                    startDetectModel();
                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(vibrate_state));
                    EventBus.getDefault().postSticky(new ServiceEvent.myoConnected_Event(true));
                    //EventBus.getDefault().post(new ServiceEvent.myoLock_Event(true));

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
    public void getGestureNumber(ServiceEvent.GestureEvent_forService event){

        gestureNum = event.gestureNumber;
        Log.d(TAG,"Gesture num : "+ gestureNum);

        if(myoApp.isUnlocked()){
            if(myoApp.getUnlockedGesture()==LITTLEFINGER){
                EventBus.getDefault().post(new ServiceEvent.GestureEvent(gestureNum));
            }else if(myoApp.getUnlockedGesture()==SCISSORS){
                EventBus.getDefault().post(new ServiceEvent.GestureEvent_forMusic(gestureNum));
            }

//            Log.d(TAG,"Send EventBus post - Gesture num : "+ gestureNum);
        }else{
            switch(gestureNum) {
                case LITTLEFINGER:
                    if(currentActivity == MUSIC_ACTIVITY){
                        EventBus.getDefault().post(new ServiceEvent.GestureEvent(gestureNum));
                    }else{
                        smoothcount[gestureNum]++;

                        if (smoothcount[gestureNum] > 2) {
                            if(!myoApp.isUnlocked()){
                                myoApp.unlockGesture(0);

                                Handler mHandler = new Handler(Looper.getMainLooper());
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 내용
                                        Toasty.normal(getBaseContext(),"Gesture recognition Unlocked", Toast.LENGTH_SHORT,unlocked).show();
                                        EventBus.getDefault().post(new ServiceEvent.myoLock_Event(!myoApp.isUnlocked()));
                                        // EventBus.getDefault().post(new ServiceEvent.myoLock_Event(true));
                                    }
                                }, 0);
                                Log.d(TAG,"Unlock "+ LITTLEFINGER);
                            }

                            //create runnable for lock.
                            mHandler.postDelayed(lockRunnable, TIMETOLOCK);

                            //Send Vibration Event
                            EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_B));

                            resetSmoothCount();
//                    smoothcount[gestureNum] = -1;
                            mHandler.removeCallbacks(resetCountRunnable);
                        }


                        //create runnable to reset smoothcount

                        mHandler.removeCallbacks(resetCountRunnable);
                        mHandler.postDelayed(resetCountRunnable, TIMETOLOCK);
                    }
                    break;

                case SCISSORS:
                    if(currentActivity == DEFAULT_ACTIVITY){
                        EventBus.getDefault().post(new ServiceEvent.GestureEvent(gestureNum));
                    } else{
                        smoothcount[gestureNum]++;

                        if (smoothcount[gestureNum] > 2) {
                            if(!myoApp.isUnlocked()){
                                myoApp.unlockGesture(1);
                                Handler mHandler = new Handler(Looper.getMainLooper());
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 내용
                                        Toasty.normal(getBaseContext(),"Gesture recognition Unlocked", Toast.LENGTH_SHORT,unlocked).show();
                                        EventBus.getDefault().post(new ServiceEvent.myoLock_Event(!myoApp.isUnlocked()));
                                        //EventBus.getDefault().post(new ServiceEvent.myoLock_Event(true));
                                    }
                                }, 0);
                                Log.d(TAG,"Unlock "+ SCISSORS);
                            }

                            //create runnable for lock.
                            mHandler.postDelayed(lockRunnable, TIMETOLOCK);

                            //Send Vibration Event
                            EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_B));

                            resetSmoothCount();
//                    smoothcount[gestureNum] = -1;

                            mHandler.removeCallbacks(resetCountRunnable);
                        }


                        //create runnable to reset smoothcount

                        mHandler.removeCallbacks(resetCountRunnable);
                        mHandler.postDelayed(resetCountRunnable, TIMETOLOCK);
                    }

                    break;
                case FIST:
                    if(currentActivity == CAMERA_ACTIVITY){
                        EventBus.getDefault().post(new ServiceEvent.GestureEvent(gestureNum));
                    }
                    break;
                default:
                    break;

            }

        }

    }

    public void resetSmoothCount(){
        for(int i=0;i<smoothcount.length;i++){
            smoothcount[i]=0;
        }
        Log.e(TAG,"resetSmoothCount - reset");
    }

    private Runnable resetCountRunnable = new Runnable(){
        @Override
        public void run(){
            resetSmoothCount();
            Log.e(TAG,"Reset Count Runnable : smoothcount reset");
        }
    };

    private Runnable lockRunnable = new Runnable(){
        @Override
        public void run(){
            //Lock gesture
            myoApp.lockGesture();
            Toasty.normal(getBaseContext(),"Time over myo Locked", Toast.LENGTH_SHORT,locked).show();
            EventBus.getDefault().post(new ServiceEvent.myoLock_Event(!myoApp.isUnlocked()));
            Log.e(TAG,"Lock_Runnable : Gesture locked");
        }
    };

    @Subscribe
    public void restartLockTimer(ServiceEvent.restartLockTimerEvent event){
        mHandler.removeCallbacks(lockRunnable);
        mHandler.postDelayed(lockRunnable, TIMETOLOCK);
      //  Toasty.normal(getBaseContext(),"Lock_Runnable : restart Lock Timer!", Toast.LENGTH_SHORT).show();
        Log.e(TAG,"Lock_Runnable : restart Lock Timer!");
    }

    @Subscribe(sticky = true)
    public void getCurrentActivity(ServiceEvent.currentActivity_Event event){
        currentActivity = event.currentActivity;
        Log.e(TAG,"Got current activity : " + currentActivity);
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

    @Subscribe
    public void vibrate(ServiceEvent.VibrateEvent event){
        mMyoCallback.setMyoControlCommand(commandList.sendVibration(event.vibrateNum));
        Log.d(TAG,"Vibrate Myo - Got VibrateEvent");
    }

    @Subscribe(sticky = true)
    public void setting_event(ServiceEvent.SettingEvent event){
        Log.d(TAG, "setting_event" + event.vibrate_p + "," + event.recognizing_Num + "," +event.is_vibrate);
        vibrate_state=3;
        if(event.is_vibrate){
            //mMyoCallback.setMyoControlCommand(commandList.sendVibration(event.vibrate_p));
            vibrate_state=event.vibrate_p;
            Log.d(TAG, "setting_event" + event.vibrate_p + " on ");
        }else{
            //mMyoCallback.setMyoControlCommand(commandList.sendVibration(0));
            vibrate_state=0;
            Log.d(TAG, "setting_event" +" No Vibration ");
        }
        EventBus.getDefault().post(new ServiceEvent.VibrateEvent(vibrate_state));

    }



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