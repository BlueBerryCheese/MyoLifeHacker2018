package blueberrycheese.myolifehacker.SystemControl;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import blueberrycheese.myolifehacker.R;
import blueberrycheese.myolifehacker.Toasty;

/**
 * Created by User on 2017-12-01.
 */

public class SystemFeature {
    Context mContext;
    private WifiManager wifiManager;
    private AudioManager audioManager;
    private ContentResolver cResolver;
    private Drawable icon_1,icon_2,icon_3,icon_4,icon_5,icon_6;
    int currentBrightness;
    int BrightnessDiff = 5;
    int old_pos = -1;

    public SystemFeature(Context mContext){
        this.mContext = mContext;
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        audioManager =(AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        cResolver =  mContext.getContentResolver();
    }

    int[] smoothcount = new int[6];
/*    private WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    private AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);*/
    public void WifiEnable(){
        wifiManager.setWifiEnabled(true);
    }

    public void WifiDisable(){
        wifiManager.setWifiEnabled(false);
    }

    public void VolumeUp(){
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    public void VolumeDown(){
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }
    public void resetSmoothCount(){
        for(int i=0;i<smoothcount.length;i++){
            smoothcount[i]=0;
        }
    }
    public void function(int poseNum){
// 테스트용으로 기능 수행 안되게 주석처리함
        icon_1 = mContext.getDrawable(R.drawable.gesture_1_w);
        icon_2 = mContext.getDrawable(R.drawable.gesture_2_w);
        icon_3 = mContext.getDrawable(R.drawable.gesture_3_w);
        icon_4 = mContext.getDrawable(R.drawable.gesture_4_w);
        icon_5 = mContext.getDrawable(R.drawable.gesture_5_w);
        icon_6 = mContext.getDrawable(R.drawable.gesture_6_w);

        switch(poseNum){
            case 0:
                /*
                if(smoothcount[poseNum]>3) {
                    if(!wifiManager.isWifiEnabled()){
                        WifiEnable();
                        Toasty.normal(mContext,"Wifi ON", Toast.LENGTH_SHORT, icon_1).show();
                        //Toast.makeText(mContext,"WiFi On",Toast.LENGTH_LONG).show();
                    } else{
                        WifiDisable();
                        Toasty.normal(mContext,"Wifi OFF", Toast.LENGTH_SHORT, icon_1).show();
                        //Toast.makeText(mContext,"WiFi OFF",Toast.LENGTH_LONG).show();
                    }
                    //Wifi On off
                    smoothcount[poseNum]=-1;
                    resetSmoothCount();
                }
                smoothcount[poseNum]++;
                */



                //Brightness down
                if(smoothcount[poseNum]>1){
                    currentBrightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS,0
                    );
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, ((currentBrightness - BrightnessDiff)<20)?currentBrightness:currentBrightness - BrightnessDiff);
                    Toasty.normal(mContext,"Bright down", Toast.LENGTH_SHORT, icon_1).show();
                    resetSmoothCount();
                    smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;

                /*
                //Sound on off vibration
                if(smoothcount[poseNum]>3) {
                    switch(audioManager.getRingerMode()){
                        case AudioManager.RINGER_MODE_NORMAL:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            Toasty.normal(mContext,"Silent Mode", Toast.LENGTH_SHORT, icon_1).show();
                            //Toast.makeText(mContext,"RingerModeSilent",Toast.LENGTH_LONG).show();
                            break;
                        case AudioManager.RINGER_MODE_SILENT:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            Toasty.normal(mContext,"Vibrate Mode", Toast.LENGTH_SHORT, icon_1).show();
                            //Toast.makeText(mContext,"RingerModeVibrate",Toast.LENGTH_LONG).show();
                            break;
                        case AudioManager.RINGER_MODE_VIBRATE:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            Toasty.normal(mContext,"Normal Mode", Toast.LENGTH_SHORT, icon_1).show();
                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
                            break;
                        default:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            Toasty.normal(mContext,"Normal Mode", Toast.LENGTH_SHORT, icon_1).show();
                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
                            break;
                    }
                    resetSmoothCount();
                    smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;
                */
            break;
            case 1:
                //Volume Down
                if(smoothcount[poseNum]>1){
                    VolumeDown();
                    Toasty.normal(mContext,"Volume Down", Toast.LENGTH_SHORT, icon_2).show();
                    resetSmoothCount();
                    smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;
                break;
            case 2:
                //Volume up
                if(smoothcount[poseNum]>1){
                    VolumeUp();
                    Toasty.normal(mContext,"Volume Up", Toast.LENGTH_SHORT, icon_3).show();
                    resetSmoothCount();
                    smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;
                break;
            case 3:
                /*
                //Sound on off vibration
                if(smoothcount[poseNum]>3) {
                    switch(audioManager.getRingerMode()){
                        case AudioManager.RINGER_MODE_NORMAL:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            Toasty.normal(mContext,"Silent Mode", Toast.LENGTH_SHORT, icon_4).show();
                            //Toast.makeText(mContext,"RingerModeSilent",Toast.LENGTH_LONG).show();
                            break;
                        case AudioManager.RINGER_MODE_SILENT:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            Toasty.normal(mContext,"Vibrate Mode", Toast.LENGTH_SHORT, icon_4).show();
                            //Toast.makeText(mContext,"RingerModeVibrate",Toast.LENGTH_LONG).show();
                            break;
                        case AudioManager.RINGER_MODE_VIBRATE:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            Toasty.normal(mContext,"Normal Mode", Toast.LENGTH_SHORT, icon_4).show();
                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
                            break;
                        default:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            Toasty.normal(mContext,"Normal Mode", Toast.LENGTH_SHORT, icon_4).show();
                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
                            break;
                    }
                    resetSmoothCount();
                    smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;
                break;
                */
                //Brightness up
                if(smoothcount[poseNum]>1){
                    currentBrightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS,0
                    );
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, ((currentBrightness + BrightnessDiff)>95)?currentBrightness:currentBrightness + BrightnessDiff);
                    resetSmoothCount();
                    Toasty.normal(mContext,"Bright up", Toast.LENGTH_SHORT, icon_4).show();
                }
                smoothcount[poseNum]++;
                break;
            case 4:

                break;
            case 5:
                if(smoothcount[poseNum]>3){
                    Toasty.normal(mContext,"Go Back", Toast.LENGTH_SHORT, icon_6).show();
                    resetSmoothCount();
                    smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;
                break;
            default :
                break;
        }

    }

}