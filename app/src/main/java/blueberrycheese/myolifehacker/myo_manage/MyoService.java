package blueberrycheese.myolifehacker.myo_manage;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import blueberrycheese.myolifehacker.MainActivity;
import blueberrycheese.myolifehacker.MyoApp;
import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.Toasty;
import blueberrycheese.myolifehacker.events.ServiceEvent;

public class MyoService extends Service {
    private static final String TAG = "Myo_Service";
    //Previous SCAN_PERIOD was 5000.
    private static final long SCAN_PERIOD = 4500;
    private static int TIMETOLOCK = 8000;

    private static final int VIBRATION_A = 1;
    private static final int VIBRATION_B = 2;
    private static final int VIBRATION_C = 3;
    private int lock_vibrate_state=3;
    private int recog_vibrate_state=3;
    private int conn_vibrate_state=3;
    private Toast toast;
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
    private final int CAMERA_PREVIEW_ACTIVITY = 2;
    private final int TEST_ACTIVITY = 3;
    private final int MAIN_ACTIVITY = 4;
    private static IGestureDetectModel nopModel = new NopModel();

    private WindowManager mWindowManager;
    private final int notifyID = 11;

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
        setting_vibrate();
        TIMETOLOCK = Integer.parseInt(sharedPreferences.getString("recognizing_lock_count","8"))*1000;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null && intent.getAction()!=null && intent.getAction().equals("STOP")){
            stopSelf();
            return START_NOT_STICKY;
        }

        Log.e("ServiceEvent", "onStartCommand");
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        mHandler = new Handler();
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //Notification
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent stopSelf = new Intent(this, MyoService.class);
        stopSelf.setAction("STOP");
        PendingIntent stopIntent = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT) ;

        String CHANNEL_ID = "MLH_Channel";
        CharSequence name = "MyoLifeHackerCHNL";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        //For Oreo (NotificationChannel)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            manager.createNotificationChannel(mChannel);
        }

        Notification notification =  new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_m)
                .setContentTitle("Life Hacker On")
                .setChannelId(CHANNEL_ID)
                .addAction(R.drawable.ic_menu_myo,"STOP", stopIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();

//        builder.setAutoCancel(false);
//        builder.setTicker("this is ticker text");
//        builder.setContentTitle("Life Hacker On");
////        builder.setContentText("");
//        builder.setSmallIcon(R.drawable.ic_stat_m);
//        builder.setContentIntent(pendingIntent);
//        builder.setOngoing(true);
////        builder.setSubText("This is subtext...");   //API level 16
//        builder.setNumber(100);
//        builder.addAction(R.drawable.ic_menu_myo,"STOP", stopIntent);
//        builder.build();

//        myNotication = builder.getNotification();
//        manager.notify(notifyID, myNotication);
        //
        manager.notify(notifyID, notification);

        return START_STICKY;
    }


  @Override
  public void onDestroy(){
        Log.d(TAG,"Service onDestory");

      EventBus.getDefault().unregister(this);
      if(manager != null){
          manager.cancel(notifyID);
      }
      closeBLEGatt();
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

          new Handler().postDelayed(runMethodModel, SCAN_PERIOD);

      }
  }

    private final Runnable runMethodModel = new Runnable(){
        public void run(){

//            Log.d("EMGFALSETest","mMyoCallbaack.setMyoControlCommand-before if clause: " + mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly()));
            if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
                falseCount++;
                if(falseCount > 4){
                    Log.d(TAG,"postDelayed Runnable already executed enough! Something's wrong.");
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
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(conn_vibrate_state));
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
        if (currentActivity == TEST_ACTIVITY){
            EventBus.getDefault().post(new ServiceEvent.GestureEvent(gestureNum));
            return;
        }


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

                        //When screen is off or no activity is on. It will start main activity.
                        if(currentActivity == -1 && smoothcount[gestureNum] > 1){
                            Log.d(TAG,"Start main activity... - by little finger");
                            EventBus.getDefault().post(new ServiceEvent.startActivity_Event());
                            resetSmoothCount();
                            return;
                        }

                        if (smoothcount[gestureNum] > 2) {
                            if(!myoApp.isUnlocked()){
                                myoApp.unlockGesture(0);

                                Handler mHandler = new Handler(Looper.getMainLooper());
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 내용
                                        if (toast!=null)
                                            toast.cancel();
                                        toast =Toasty.normal(getBaseContext(),"Gesture recognition Unlocked", Toast.LENGTH_SHORT,unlocked);
                                        toast.show();
                                        EventBus.getDefault().post(new ServiceEvent.myoLock_Event(!myoApp.isUnlocked()));
                                        // EventBus.getDefault().post(new ServiceEvent.myoLock_Event(true));
                                    }
                                }, 0);
                                Log.d(TAG,"Unlock "+ LITTLEFINGER);
                            }

                            //create runnable for lock.
                            mHandler.postDelayed(lockRunnable, TIMETOLOCK);

                            //Send Vibration Event
                            EventBus.getDefault().post(new ServiceEvent.VibrateEvent(recog_vibrate_state));

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
                    if(currentActivity == DEFAULT_ACTIVITY || currentActivity == CAMERA_PREVIEW_ACTIVITY){
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
                                        if (toast!=null)
                                            toast.cancel();
                                        toast =Toasty.normal(getBaseContext(),"Gesture recognition Unlocked", Toast.LENGTH_SHORT,unlocked);
                                        toast.show();
                                        EventBus.getDefault().post(new ServiceEvent.myoLock_Event(!myoApp.isUnlocked()));
                                        //EventBus.getDefault().post(new ServiceEvent.myoLock_Event(true));
                                    }
                                }, 0);
                                Log.d(TAG,"Unlock "+ SCISSORS);
                            }

                            //create runnable for lock.
                            mHandler.postDelayed(lockRunnable, TIMETOLOCK);

                            //Send Vibration Event
                            EventBus.getDefault().post(new ServiceEvent.VibrateEvent(lock_vibrate_state));

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
                    if(currentActivity == CAMERA_PREVIEW_ACTIVITY){
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
            if (toast!=null)
                toast.cancel();
            toast =Toasty.normal(getBaseContext(),"Time over myo Locked", Toast.LENGTH_SHORT,locked);
            toast.show();
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
    public void setCurrentActivity(ServiceEvent.currentActivity_Event event){
        Log.e(TAG,"Set current activity from: " + this.currentActivity + "  to : " + event.currentActivity);
        currentActivity = event.currentActivity;

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
            Log.e(TAG,"setCurrentModel -> DetectModel");
        } else if(event.set == 2){
            GestureDetectModelManager.setCurrentModel(nopModel);
            Log.e(TAG,"setCurrentModel -> NopModel");
        }
    }

    @Subscribe
    public void vibrate(ServiceEvent.VibrateEvent event){
        mMyoCallback.setMyoControlCommand(commandList.sendVibration(event.vibrateNum));
        Log.d(TAG,"Vibrate Myo - Got VibrateEvent");
    }

    @Subscribe(sticky = true)
    public void setting_event(ServiceEvent.SettingEvent event){
        Log.d(TAG, "setting_event" + event.lock_vibrate_p + ","+ event.recog_vibrate_p + ","+ event.conn_vibrate_p + "," + event.recognizing_Num + "," +event.is_vibrate);

        lock_vibrate_state=3;
        recog_vibrate_state=3;
        conn_vibrate_state=3;

        if(event.is_vibrate){
            //mMyoCallback.setMyoControlCommand(commandList.sendVibration(event.vibrate_p));
            lock_vibrate_state=event.lock_vibrate_p;
            recog_vibrate_state=event.recog_vibrate_p;
            conn_vibrate_state=event.conn_vibrate_p;
            Log.d(TAG, "setting_event on ");
        }else{
            //mMyoCallback.setMyoControlCommand(commandList.sendVibration(0));
            lock_vibrate_state=0;
            recog_vibrate_state=0;
            conn_vibrate_state=0;
            Log.d(TAG, "setting_event" +" No Vibration ");
        }
        EventBus.getDefault().post(new ServiceEvent.VibrateEvent(recog_vibrate_state));
    }


    //새로 detectMethod/Model 생성. -> K-means 모델 바꼈을때(Adapt 버튼 눌렀을 때) or 인식 주기 설정 바꼈을때 App 재시작 하지않고 적용 가능하게 하기 위함
    @Subscribe
    public void reCreateDetect(ServiceEvent.reCreateDetectM_Event event){
        Log.d(TAG,"Recreating Detect for using new model");
        saveMethod  = new GestureSaveMethod(-1, getApplicationContext(),1);
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
            int recog_cnt = Integer.parseInt(sharedPreferences.getString("recognizing_count","50"));
            TIMETOLOCK = Integer.parseInt(sharedPreferences.getString("recognizing_lock_count","8"))*1000;
            detectMethod = new GestureDetectMethod(mHandler, saveMethod.getCompareDataList(),recog_cnt);
            detectModel = new GestureDetectModel(detectMethod);
            model = detectModel;
//            GestureDetectModelManager.setCurrentModel(model);
            Log.d(TAG,"Recreating Detect done!");

//            startDetectModel();
        }
    }

    @Subscribe
    public void startActivity(ServiceEvent.startActivity_Event event) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
        wl.acquire(3000);
        Intent actIntent = new Intent(this, MainActivity.class);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
        wl.release();
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

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
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