package blueberrycheese.myolifehacker.myo_manage;

import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.Queue;

import blueberrycheese.myolifehacker.CameraView.CameraEvent;
import blueberrycheese.myolifehacker.MenuControl.MenuEvent;
import blueberrycheese.myolifehacker.SystemControl.SystemFeature;

/**
 * Created by Seongho on 2017-12-01.
 */

public class NumberSmoother {
    private final static int SMOOTHING_LENGTH = 50;//5
    private final static int THRESHOLD_LENGTH = 20;//3
    private final static int MAX_SAVE_LENGTH=6;
    private final static int DECLINE_LENGTH = 5;
    private Queue<Integer> gestureNumArray = new LinkedList<>();
    private int[] numCounter =  new int[MAX_SAVE_LENGTH];
    private int storageDataCount = 0;

    private SystemFeature systemFeature;


    private Context mcontext;
    public NumberSmoother(){

    }

    public  NumberSmoother(SystemFeature systemFeature){
        this.systemFeature=systemFeature;
    }


    public void addArray(Integer gestureNum) {
        gestureNumArray.offer(gestureNum);
        if (gestureNum != -1) {
            numCounter[gestureNum]++;
        }
        storageDataCount++;
        if (storageDataCount > SMOOTHING_LENGTH) {
            int deleteNumber = gestureNumArray.peek();
            if (deleteNumber != -1) {
                numCounter[deleteNumber]--;
            }
            gestureNumArray.poll();
            storageDataCount--;
        }

        Log.d("numbersmoother","["+numCounter[0]+","+numCounter[1]+","+numCounter[2]+","+numCounter[3]+","+numCounter[4]+","+numCounter[5]+"]");
    }

    public void clearArray(){
        gestureNumArray = new LinkedList<>();
        numCounter =  new int[MAX_SAVE_LENGTH];
        storageDataCount = 0;
    }

    public int getSmoothingNumber() {
        for (int i_element = 0; i_element < MAX_SAVE_LENGTH; i_element++) {
            if (numCounter[i_element] >= THRESHOLD_LENGTH) {//50개중 20개이상 일치하지 않으면 불일치(인지되지 않은 제스처)
                Log.d("number success","number success : "+(i_element+1));
                gestureNumArray=new LinkedList<>();
                numCounter =  new int[MAX_SAVE_LENGTH];
                return i_element;
            }
        }
        return -1;
    }

    /*
    SystemControl 부분 SmootingNumber
     */
    public int getSmoothingNumber_system() {
        for (int i_element = 0; i_element < MAX_SAVE_LENGTH; i_element++) {
            if (numCounter[i_element] >= THRESHOLD_LENGTH) {//50개중 20개이상 일치하지 않으면 불일치(인지되지 않은 제스처)
                Log.d("number success","number success : "+(i_element+1));
                //gestureNumArray=new LinkedList<>();
                //Log.d("detect_gesture","-> "+(int)(i_element+1));
                Log.d("system_gesture cnt","-> "+numCounter[0]+","+numCounter[1]+","+numCounter[2]+","+numCounter[3]+","+numCounter[4]+","+numCounter[5]);
                systemFeature.function(i_element);
                return i_element;
            }
        }
        return -1;
    }

    public int getSmoothingNumber_menu() {
        for (int i_element = 0; i_element < MAX_SAVE_LENGTH; i_element++) {
            if (numCounter[i_element] >= THRESHOLD_LENGTH) {//50개중 20개이상 일치하지 않으면 불일치(인지되지 않은 제스처)
                Log.d("number success","number success : "+(i_element+1));
                //gestureNumArray=new LinkedList<>();
                //Log.d("detect_gesture","-> "+(int)(i_element+1));
                Log.d("menu_gesture cnt","-> "+numCounter[0]+","+numCounter[1]+","+numCounter[2]+","+numCounter[3]+","+numCounter[4]+","+numCounter[5]);
                EventBus.getDefault().post(new MenuEvent(i_element));
                return i_element;
            }
        }
        return -1;
    }

    /*
    Camera 부분 SmootingNumber
     */
    public int getSmoothingNumber_camera() {
        for (int i_element = 0; i_element < MAX_SAVE_LENGTH; i_element++) {
            if (numCounter[i_element] >= THRESHOLD_LENGTH) {//50개중 20개이상 일치하지 않으면 불일치(인지되지 않은 제스처)
                Log.d("number success","number success : "+(i_element+1));
                //gestureNumArray=new LinkedList<>();
                //Log.d("detect_gesture","-> "+(int)(i_element+1));
                Log.d("detect_gesture cnt","-> "+numCounter[0]+","+numCounter[1]+","+numCounter[2]+","+numCounter[3]+","+numCounter[4]+","+numCounter[5]);
//                systemFeature.function(i_element);
//                Log.d("smoothGesture","SmoothGesture(i_element+1) : "+(i_element+1));
                EventBus.getDefault().post(new CameraEvent(i_element));
                return i_element;
            }
        }
        return -1;
    }



    //Delete
//    boolean flg = false;
//    public int getSmoothingNumber_system() {
//        if(flg){
//            gestureNumArray=new LinkedList<>();
//            numCounter =  new int[MAX_SAVE_LENGTH];
//            flg = false;
//        }
//
//        for (int i_element = 0; i_element < MAX_SAVE_LENGTH; i_element++) {
//            if (numCounter[i_element] >= THRESHOLD_LENGTH) {//50개중 20개이상 일치하지 않으면 불일치(인지되지 않은 제스처)
//                Log.d("number success","number success : "+(i_element+1));
//                //gestureNumArray=new LinkedList<>();
//                //Log.d("detect_gesture","-> "+(int)(i_element+1));
//                Log.d("detect_gesture cnt","-> "+numCounter[0]+","+numCounter[1]+","+numCounter[2]+","+numCounter[3]+","+numCounter[4]+","+numCounter[5]);
//                systemFeature.function(i_element);
//                flg = true;
//                return i_element;
//            }
//        }
//        return -1;
//    }

}
