package blueberrycheese.myolifehacker.SystemControl;


import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;

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
        for(int i : smoothcount){
            i = 0;
        }
    }
    public void function(int poseNum){
// 테스트용으로 기능 수행 안되게 주석처리함

        switch(poseNum){
            case 0:
                if(smoothcount[poseNum]>3) {
                    if(!wifiManager.isWifiEnabled()){
                        WifiEnable();
                        //Toast.makeText(mContext,"WiFi On",Toast.LENGTH_LONG).show();
                    } else{
                        WifiDisable();
                        //Toast.makeText(mContext,"WiFi OFF",Toast.LENGTH_LONG).show();
                    }
                    //Wifi On off
                    resetSmoothCount();
                    //smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;
            break;
            case 1:
                //Volume Down
                if(smoothcount[poseNum]>0){
                    VolumeDown();
                    //smoothcount[poseNum]=-1;
                    resetSmoothCount();
                }
                smoothcount[poseNum]++;
                break;
            case 2:
                //Volume up
                if(smoothcount[poseNum]>0){
                    VolumeUp();
                    //smoothcount[poseNum]=-1;
                    resetSmoothCount();
                }
                smoothcount[poseNum]++;
                break;
            case 3:
                //Sound on off vibration
                if(smoothcount[poseNum]>3) {
                    switch(audioManager.getRingerMode()){
                        case AudioManager.RINGER_MODE_NORMAL:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            //Toast.makeText(mContext,"RingerModeSilent",Toast.LENGTH_LONG).show();
                            break;
                        case AudioManager.RINGER_MODE_SILENT:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            //Toast.makeText(mContext,"RingerModeVibrate",Toast.LENGTH_LONG).show();
                            break;
                        case AudioManager.RINGER_MODE_VIBRATE:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
                            break;
                        default:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
                            break;
                    }
                    resetSmoothCount();
                }
                smoothcount[poseNum]++;
                break;
            case 4:
                //Brightness up
                if(smoothcount[poseNum]>0){
                    currentBrightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS,0
                    );
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, ((currentBrightness + BrightnessDiff)>95)?currentBrightness:currentBrightness + BrightnessDiff);
                    resetSmoothCount();
                    //smoothcount[poseNum]=-1;
                }
                smoothcount[poseNum]++;
                break;
            case 5:
                //Brightness down
                if(smoothcount[poseNum]>0){
                    currentBrightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS,0
                    );
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, ((currentBrightness - BrightnessDiff)<20)?currentBrightness:currentBrightness - BrightnessDiff);
                    resetSmoothCount();
                }
                smoothcount[poseNum]++;
                break;
            default :
                break;
        }

    }


//이 아래는 필요없는듯 위에 function 주석 풀고 사용하면 될듯
//public void function(int poseNum){
//    if(old_pos!=poseNum){
//        old_pos=poseNum;
//        switch(poseNum){
//            case 0:
//
//                    if(!wifiManager.isWifiEnabled()){
//                        WifiEnable();
//                        //Toast.makeText(mContext,"WiFi On",Toast.LENGTH_LONG).show();
//                    } else{
//                        WifiDisable();
//                        //Toast.makeText(mContext,"WiFi OFF",Toast.LENGTH_LONG).show();
//                    }
//                    //Wifi On off
//                    resetSmoothCount();
//                    //smoothcount[poseNum]=-1;
//
//                break;
//            case 1:
//                //Sound on off vibration
//
//                    switch(audioManager.getRingerMode()){
//                        case AudioManager.RINGER_MODE_NORMAL:
//                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//                            //Toast.makeText(mContext,"RingerModeSilent",Toast.LENGTH_LONG).show();
//                            break;
//                        case AudioManager.RINGER_MODE_SILENT:
//                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//                            //Toast.makeText(mContext,"RingerModeVibrate",Toast.LENGTH_LONG).show();
//                            break;
//                        case AudioManager.RINGER_MODE_VIBRATE:
//                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
//                            break;
//                        default:
//                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//                            //Toast.makeText(mContext,"RingerModeNormal",Toast.LENGTH_LONG).show();
//                            break;
//                    }
//                    resetSmoothCount();
//
//                break;
//            case 2:
//                //Volume up
//
//                    VolumeUp();
//                    //smoothcount[poseNum]=-1;
//                    resetSmoothCount();
//
//                break;
//            case 3:
//                //Volume Down
//
//                    VolumeDown();
//                    //smoothcount[poseNum]=-1;
//                    resetSmoothCount();
//
//                break;
//            case 4:
//                //Brightness up
//
//                    currentBrightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS,0);
//                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, ((currentBrightness + BrightnessDiff)>95)?currentBrightness:currentBrightness + BrightnessDiff);
//                    resetSmoothCount();
//                    //smoothcount[poseNum]=-1;
//
//                break;
//            case 5:
//                //Brightness down
//
//                    currentBrightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS,0
//                    );
//                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, ((currentBrightness - BrightnessDiff)<20)?currentBrightness:currentBrightness - BrightnessDiff);
//                    resetSmoothCount();
//
//                break;
//            default :
//                break;
//        }
//
//    }
//
//}
}