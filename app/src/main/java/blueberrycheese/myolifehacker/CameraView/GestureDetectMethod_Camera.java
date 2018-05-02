package blueberrycheese.myolifehacker.CameraView;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import java.util.ArrayList;

import blueberrycheese.myolifehacker.SystemControl.SystemFeature;
import blueberrycheese.myolifehacker.myo_manage.EmgCharacteristicData;
import blueberrycheese.myolifehacker.myo_manage.EmgData;
import blueberrycheese.myolifehacker.myo_manage.NumberSmoother;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import blueberrycheese.myolifehacker.myo_manage.EmgCharacteristicData;
import blueberrycheese.myolifehacker.myo_manage.EmgData;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.NumberSmoother;

/**
 * Created by Seongho on 2017-12-01.
 * ver 1.1 Manhattan, Norm-3 distance, Weight Ratio, CorrelationEfficient detect Method
 * ver 2.0 K-Means++ Clustering Algorithm Apply(2018-03-21)
 * ver 2.1 Speed improvement(2018-04-07)
 */

/*
+added System Control
 */

public class GestureDetectMethod_Camera {
    private final int COMPARE_NUM = 6;          // 제스처 추가의 여지를 만듦
    private final static int STREAM_DATA_LENGTH = 5;
    private final static Double THRESHOLD = 0.01;
    private final static int KMEANS_K = 128;    //sampling한 KMEANS_K의 갯수
    private final static String FileList_kmeans = "KMEANS_DATA.dat";    //적용되는 KMEANS_DATA파일 우리가 생성함


    private final ArrayList<EmgData> compareGesture;

    private int streamCount = 0;
    private EmgData streamingMaxData;
    private Double detect_distance;
    private int detect_Num;
    private Context mcontext;
    private TextView maxDataTextView;
    private NumberSmoother numberSmoother;
    private NumberSmoother numberSmoother2;
    private double[] maxData = new double[8];
    private StringBuilder sb;
    private Handler handler;
    public GestureDetectMethod_Camera(ArrayList<EmgData> gesture) {
        compareGesture = gesture;
        numberSmoother = new NumberSmoother();
    }
    public GestureDetectMethod_Camera(Handler handler, ArrayList<EmgData> gesture){
        compareGesture = gesture;
        this.handler=handler;
        numberSmoother = new NumberSmoother();
    }

    public GestureDetectMethod_Camera(Handler handler, ArrayList<EmgData> gesture, TextView textView,SystemFeature systemFeature) {
        compareGesture = gesture;
        maxDataTextView=textView;
        this.handler=handler;
        numberSmoother = new NumberSmoother(systemFeature);
    }


    public enum GestureState {
        No_Gesture,
        Gesture_1,
        Gesture_2,
        Gesture_3,
        Gesture_4,
        Gesture_5,
        Gesture_6
    }


    private blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState getEnum(int i_gesture) {
        switch (i_gesture) {
            case 0:
                return blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState.Gesture_1;
            case 1:
                return blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState.Gesture_2;
            case 2:
                return blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState.Gesture_3;
            case 3:
                return blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState.Gesture_4;
            case 4:
                return blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState.Gesture_5;
            case 5:
                return blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState.Gesture_6;
            default:
                return blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState.No_Gesture;
        }

    }

    int cnt = 0;
    int old_gesture_num=-1;
    //getDetectGesture Use By K-MEANS
    public blueberrycheese.myolifehacker.CameraView.GestureDetectMethod_Camera.GestureState getDetectGesture(byte[] data) {
        EmgData streamData = new EmgData(new EmgCharacteristicData(data));
        //streamCount++;
        streamingMaxData = streamData;

        detect_distance = 100000.0;
        detect_Num = -1;
//        Log.e("detect_gesture",""+streamingMaxData.getElement(0)+","+streamingMaxData.getElement(1)+","+streamingMaxData.getElement(2)+","
//                +streamingMaxData.getElement(3)+","+streamingMaxData.getElement(4)+","+streamingMaxData.getElement(5)+","+streamingMaxData.getElement(6)+","
//                +streamingMaxData.getElement(7)+",");

        for (int i_gesture = 0;i_gesture < COMPARE_NUM*KMEANS_K ;i_gesture++) {

            EmgData compData = compareGesture.get(i_gesture);
            double distance = dist(streamingMaxData, compData);     //Calculate Euclidean distance to compare similarity

            if (detect_distance >= distance) {
                detect_distance = distance;
                //Log.d("detect_gesture",(int)i_gesture+"distance ("+distance+") -> "+(int)(i_gesture/KMEANS_K));
                detect_Num = (int)(i_gesture/KMEANS_K);

            }
        }


        //Log.d("detect_gesture","distance ("+detect_distance+") -> "+(int)(detect_Num));
        numberSmoother.addArray((Integer) (detect_Num));
        //streamCount = 0;
        cnt++;
        if(cnt%10==0){
            cnt=0;
            int result = numberSmoother.getSmoothingNumber();
            if(old_gesture_num!=result)
                old_gesture_num = result;

        }
        return getEnum(old_gesture_num);

    }

    private double getThreshold() {
        return THRESHOLD;
//        return 0.9;
    }



    private double dist(EmgData streamData, EmgData compareData){
        double sum = 0;
        for(int index=0;index<8;index++){
            sum += Math.pow((streamData.getElement(index)-compareData.getElement(index)),2);
        }

        return sum;
    }


}