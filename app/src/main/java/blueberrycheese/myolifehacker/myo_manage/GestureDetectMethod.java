package blueberrycheese.myolifehacker.myo_manage;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Seongho on 2017-12-01.
 * ver 1.1 Manhattan, Norm-3 distance, Weight Ratio, CorrelationEfficient detect Method
 */

public class GestureDetectMethod {
    private final static int COMPARE_NUM = 6;
    private final static int STREAM_DATA_LENGTH = 5;
    private final static Double THRESHOLD = 0.01;
    //private final static Double THRESHOLD = 0.60;
    private final static int KMEANS_K = 128;
    private final static String FileList_kmeans = "KMEANS_DATA.dat";


    private final ArrayList<EmgData> compareGesture;

    private int streamCount = 0;
    private EmgData streamingMaxData;
    private Double detect_distance;
    private int detect_Num;
    private Context mcontext;
    private TextView maxDataTextView;
    private NumberSmoother numberSmoother;
    private double[] maxData = new double[8];
    private StringBuilder sb;
    private Handler handler;
    public GestureDetectMethod(ArrayList<EmgData> gesture) {
        compareGesture = gesture;
        numberSmoother = new NumberSmoother();
    }


    public GestureDetectMethod(Handler handler, ArrayList<EmgData> gesture, TextView textView) {
        compareGesture = gesture;
        maxDataTextView=textView;
        this.handler=handler;
        numberSmoother = new NumberSmoother();
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

    private GestureState getEnum(int i_gesture) {
        switch (i_gesture) {
            case 0:
                return GestureState.Gesture_1;
            case 1:
                return GestureState.Gesture_2;
            case 2:
                return GestureState.Gesture_3;
            case 3:
                return GestureState.Gesture_4;
            case 4:
                return GestureState.Gesture_5;
            case 5:
                return GestureState.Gesture_6;
            default:
                return GestureState.No_Gesture;
        }
    }

    /*public GestureState getDetectGesture(byte[] data) {
        EmgData streamData = new EmgData(new EmgCharacteristicData(data));
        streamCount++;

        if (streamCount == 1){
            streamingMaxData = streamData;

        } else {

            for (int i_element = 0; i_element < 8; i_element++) {
                if (streamData.getElement(i_element) > streamingMaxData.getElement(i_element)) {
                    streamingMaxData.setElement(i_element, streamData.getElement(i_element));
                }
            }
            if (streamCount == STREAM_DATA_LENGTH){
                double[] classifi=new double[COMPARE_NUM];
                sb= new StringBuilder();
                for (int i_gesture = 0;i_gesture < COMPARE_NUM ;i_gesture++) {
                    EmgData compData = compareGesture.get(i_gesture);

                    classifi[i_gesture]=Euclidean_distanceCalculation(streamingMaxData,compData);
                    //Log.d("classification","gesture "+(i_gesture+1)+" : "+classifi[i_gesture]);
                    sb.append("Gesture "+(i_gesture+1)+" ["+classifi[i_gesture]+"]\n");
                }


                double max=0;
                for(int i=0;i<COMPARE_NUM;i++){
                    max=Math.max(classifi[i],max);
                }
                for(int i=0;i<COMPARE_NUM;i++){
                    if(max==classifi[i]){
                        Log.d("classification_match:","match: "+(i+1));
                        numberSmoother.addArray(i);
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        maxDataTextView.setText(sb.toString());
                    }
                });

                streamCount = 0;
            }
        }
        return getEnum(numberSmoother.getSmoothingNumber());
    }*/

/*    public GestureState getDetectGesture(byte[] data) {
        EmgData streamData = new EmgData(new EmgCharacteristicData(data));
        streamCount++;

        if (streamCount == 1){
            streamingMaxData = streamData;
            arrayList= new ArrayList<>();
            arrayList.add(streamData);
        } else {
            arrayList.add(streamData);
            for (int i_element = 0; i_element < 8; i_element++) {
                if (streamData.getElement(i_element) > streamingMaxData.getElement(i_element)) {
                    streamingMaxData.setElement(i_element, streamData.getElement(i_element));
                }
            }
            if (streamCount == STREAM_DATA_LENGTH){
                detect_distance = 0.7;
                detect_Num = -1;
                for (int i_gesture = 0;i_gesture < COMPARE_NUM ;i_gesture++) {
                    EmgData compData = compareGesture.get(i_gesture);
                    double distance = correlationCalculation(streamingMaxData, compData);
                    if (detect_distance < distance) {
                        Log.d("classification","gesture "+(i_gesture+1)+" : "+distance);
                        detect_distance = distance;
                        detect_Num = i_gesture;
                    }
                }
                numberSmoother.addArray(detect_Num);
                streamCount = 0;
            }
        }
        return getEnum(numberSmoother.getSmoothingNumber());
    }*/

    public GestureState getDetectGesture(byte[] data) {
        EmgData streamData = new EmgData(new EmgCharacteristicData(data));
        streamCount++;
        streamingMaxData = streamData;

        detect_distance = 100000.0;
        detect_Num = -1;
        Log.e("detect_gesture",""+streamingMaxData.getElement(0)+","+streamingMaxData.getElement(1)+","+streamingMaxData.getElement(2)+","+streamingMaxData.getElement(3)+","+streamingMaxData.getElement(4)+","+streamingMaxData.getElement(5)+","+streamingMaxData.getElement(6)+","+streamingMaxData.getElement(7)+",");
        //Log.e("detect_gesture",""+compareGesture.get(0).getElement(0)+","+compareGesture.get(0).getElement(1)+","+compareGesture.get(0).getElement(2)+","+compareGesture.get(0).getElement(3)+","+compareGesture.get(0).getElement(4)+","+compareGesture.get(0).getElement(5)+","+compareGesture.get(0).getElement(6)+","+compareGesture.get(0).getElement(7)+",");

        int[] aaa = new int[6];
        for (int i_gesture = 0;i_gesture < COMPARE_NUM*KMEANS_K ;i_gesture++) {

            EmgData compData = compareGesture.get(i_gesture);
            double distance = dist(streamingMaxData, compData);

            if (detect_distance >= distance) {
                detect_distance = distance;
                Log.d("detect_gesture",(int)i_gesture+"distance ("+distance+") -> "+(int)(i_gesture/KMEANS_K));
                detect_Num = (int)(i_gesture/KMEANS_K);

            }
        }

        Log.d("detect_gesture","distance ("+detect_distance+") -> "+(int)(detect_Num));
        numberSmoother.addArray((Integer) (detect_Num));
        streamCount = 0;



//        detect_distance = 100.0;
//        detect_Num = -1;
//        Log.e("detect_gesture",""+streamingMaxData.getElement(0)+","+streamingMaxData.getElement(1)+","+streamingMaxData.getElement(2)+","+streamingMaxData.getElement(3)+","+streamingMaxData.getElement(4)+","+streamingMaxData.getElement(5)+","+streamingMaxData.getElement(6)+","+streamingMaxData.getElement(7)+",");
//        //Log.e("detect_gesture",""+compareGesture.get(0).getElement(0)+","+compareGesture.get(0).getElement(1)+","+compareGesture.get(0).getElement(2)+","+compareGesture.get(0).getElement(3)+","+compareGesture.get(0).getElement(4)+","+compareGesture.get(0).getElement(5)+","+compareGesture.get(0).getElement(6)+","+compareGesture.get(0).getElement(7)+",");
//
//        int[] aaa = new int[6];
//        for (int i_gesture = 0;i_gesture < COMPARE_NUM*KMEANS_K ;i_gesture++) {
//
//            EmgData compData = compareGesture.get(i_gesture);
//            double distance = Euclidean_distanceCalculation(streamingMaxData, compData);
//
//            if (detect_distance >= distance) {
//                detect_distance = distance;
//                Log.d("detect_gesture",(int)i_gesture+"distance ("+distance+") -> "+(int)(i_gesture/KMEANS_K));
//                detect_Num = (int)(i_gesture/KMEANS_K);
//
//            }
//        }
//
////            if (30 >= distance) {
////            //detect_distance = distance;
////            //Log.d("detect_gesture",(int)i_gesture+"distance ("+distance+") -> "+(int)(i_gesture/KMEANS_K));
////            detect_Num = (int)(i_gesture/KMEANS_K);
////            aaa[detect_Num]++;
////        }
////    }
////    int maxii = 0;
////        Log.e("detect_gesture","count : "+aaa[0]+","+aaa[1]+","+aaa[2]+","+aaa[3]+","+aaa[4]+","+aaa[5]);
////        for(int x=0;x<6;x++){
////        if(aaa[maxii]<aaa[x]){
////            maxii=x;
////        }
////    }
//        Log.d("detect_gesture","distance ("+detect_distance+") -> "+(int)(detect_Num));
//        numberSmoother.addArray((Integer) (detect_Num));
//        numberSmoother.addArray((Integer) (maxii));
        //Log.d("detect",""+(int)(detect_Num));
        return getEnum(numberSmoother.getSmoothingNumber());
    }

    private double getThreshold() {
        return THRESHOLD;
//        return 0.9;
    }

    // 2 vectors distance devied from each vectors norm.
    private double distanceCalculation(EmgData streamData, EmgData compareData){
        double return_val = streamData.getDistanceFrom(compareData)/streamData.getNorm()/compareData.getNorm();
        return return_val;
    }

    private double Euclidean_distanceCalculation(EmgData streamData, EmgData compareData){
        EmgData diffData = new EmgData(streamData.getEmgArray());

        for(int index=0;index<8;index++){
            diffData.setElement(index, Math.abs(streamData.getElement(index)-compareData.getElement(index)));
        }

        double return_val = diffData.get_2Norm()/streamData.get_2Norm()/compareData.get_2Norm();
        return diffData.get_2Norm();
        //return return_val;
    }

    private double dist(EmgData streamData, EmgData compareData){
        double sum = 0;
        for(int index=0;index<8;index++){
            sum += Math.pow((streamData.getElement(index)-compareData.getElement(index)),2);
        }

        return sum;
        //return return_val;
    }

    private double Manhattan_distanceCalculation(EmgData streamData, EmgData compareData){
        EmgData diffData = new EmgData();
        for(int index=0;index<8;index++){
            diffData.setElement(index,streamData.getElement(index)-compareData.getElement(index));
        }
        double return_val = diffData.get_1Norm()/streamData.get_1Norm()/compareData.get_1Norm();
        return return_val;
    }

    private double distanceCalculation1(EmgData streamData, EmgData compareData){
        double return_val = streamData.getDistanceFrom1(compareData)/streamData.get_1Norm()/compareData.get_1Norm();
        return return_val;
    }
    private double distanceCalculation3(EmgData streamData, EmgData compareData){
        double return_val = streamData.getDistanceFrom3(compareData)/streamData.getNorm3()/compareData.getNorm3();
        return return_val;
    }

    private double distanceRatioCalculation_weight(EmgData streamData, EmgData compareData){
        double MatchedWeightCount=0;
        for(int i=0;i<8;i++){
            double vv= Math.abs(streamData.getElement(i)/compareData.getElement(i));
            if(0.60<vv&&vv<1.40) {
                MatchedWeightCount+=0.1;
                if (0.70 < vv && vv < 1.30) {
                    MatchedWeightCount += 0.6;
                    if (0.80 < vv && vv < 1.20) {
                        MatchedWeightCount += 0.1;
                        if (0.90 < vv && vv < 1.10) {
                            MatchedWeightCount += 0.2;
                        }
                    }
                }
            }

        }
        return MatchedWeightCount;
    }

    private int distanceRatioCalculation_normal(EmgData streamData, EmgData compareData){
        int MatchedCount=0;
        for(int i=0;i<8;i++){
            double vv= Math.abs(streamData.getElement(i)/compareData.getElement(i));
            if(0.70<vv&&vv<1.30){
                MatchedCount++;
            }
        }
        return MatchedCount;
    }

    private double correlationCalculation(EmgData streamData,EmgData compareData){
        double[] streamemg = new double[8];
        double[] compareemg = new double[8];
        double avg_streamm = 0.0;
        double avg_comapre= 0.0;
        double result;
        double bunja = 0.0;
        double bunmox = 0.0;
        double bunmoy = 0.0;
        for(int i=0;i<8;i++){
            streamemg[i]=streamData.getElement(i);
            compareemg[i]=compareData.getElement(i);
            avg_streamm += streamemg[i];
            avg_comapre += compareemg[i];
        }
        avg_comapre/=8; avg_streamm/=8;


        for(int i=0;i<8;i++){
            bunja += (streamData.getElement(i)-avg_streamm)*(compareData.getElement(i)-avg_comapre);
            bunmox += (streamData.getElement(i)-avg_streamm)*(streamData.getElement(i)-avg_streamm);
            bunmoy += (compareData.getElement(i)-avg_comapre)*(compareData.getElement(i)-avg_comapre);
        }

        result = bunja/ Math.sqrt(bunmox)/ Math.sqrt(bunmoy);
        return Math.abs(result);
    }


    // Mathematical [sin] value of 2 vectors' inner angle.
    private double distanceCalculation_sin(EmgData streamData, EmgData compareData){
        double return_val = streamData.getInnerProductionTo(compareData)/streamData.getNorm()/compareData.getNorm();
        return return_val;
    }

    // Mathematical [cos] value of 2 vectors' inner angle from low of cosines.
    private double distanceCalculation_cos(EmgData streamData, EmgData compareData){
        double streamNorm  = streamData.getNorm();
        double compareNorm = compareData.getNorm();
        double distance    = streamData.getDistanceFrom(compareData);
        return (Math.pow(streamNorm,2.0)+ Math.pow(compareNorm,2.0)- Math.pow(distance,2.0))/streamNorm/compareNorm/2;
    }

}
