package blueberrycheese.myolifehacker.events;

import android.bluetooth.BluetoothDevice;

import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;

public class ServiceEvent {
    public static class MyoDeviceEvent{

        public BluetoothDevice MyoDevice;

        public MyoDeviceEvent(BluetoothDevice device){
            this.MyoDevice = device;
        }
    }

    public static class GestureEvent_forService{

        public int gestureNumber;

        public GestureEvent_forService(int i_element){
            this.gestureNumber = i_element;
        }
    }

    public static class GestureEvent{

        public int gestureNumber;

        public GestureEvent(int i_element){
            this.gestureNumber = i_element;
        }
    }


    public static class VibrateEvent{
        public int vibrateNum;
        public VibrateEvent(int vNum){
            this.vibrateNum = vNum;
        }
    }

    public static class restartLockTimerEvent{
        public int addDelay = 0;
        public restartLockTimerEvent(int addDelay){
            this.addDelay = addDelay;
        }
    }

    public static class setDetectModel_Event{
        public int set;

        public setDetectModel_Event(int set){
            this.set = set;
        }
    }

    public static class myoConnected_Event{
        public boolean connection;
        public myoConnected_Event(boolean flag){
            this.connection = flag;
        }
    }

    public static class SettingEvent{
        public int vibrate_p;
        public boolean is_vibrate;
        public int recognizing_Num;
        public SettingEvent(int vNum,boolean is_v,int rNum){
            this.vibrate_p = vNum;
            is_vibrate = is_v;
            recognizing_Num = rNum;
        }
    }
    ///////Test

    public static class DetectModel{
        public IGestureDetectModel detectModel;

        public DetectModel(IGestureDetectModel model){
            this.detectModel = model;
        }
    }

    public static class MyoDevice_StringEvent{

        public String MyoDevice_String;

        public MyoDevice_StringEvent(String device){
            this.MyoDevice_String = device;
        }
    }

    public static class testEvent{
        public String text;
        public testEvent(String text){
            this.text = text;
        }
    }



}
