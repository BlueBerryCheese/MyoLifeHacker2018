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

    public static class MyoDevice_StringEvent{

        public String MyoDevice_String;

        public MyoDevice_StringEvent(String device){
            this.MyoDevice_String = device;
        }
    }

    public static class GestureEvent{

        public int gestureNumber;

        public GestureEvent(int i_element){
            this.gestureNumber = i_element;
        }
    }

    public static class setDetectModel_Event{
        public int set;

        public setDetectModel_Event(int set){
            this.set = set;
        }
    }

    public static class DetectModel{
        public IGestureDetectModel detectModel;

        public DetectModel(IGestureDetectModel model){
            this.detectModel = model;
        }
    }


    public static class testEvent{
        public String text;
        public testEvent(String text){
            this.text = text;
        }
    }



}
