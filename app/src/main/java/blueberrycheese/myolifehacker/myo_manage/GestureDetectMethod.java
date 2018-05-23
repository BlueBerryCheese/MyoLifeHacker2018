package blueberrycheese.myolifehacker.myo_manage;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Seongho on 2017-12-01.
 * ver 1.1 Manhattan, Norm-3 distance, Weight Ratio, CorrelationEfficient detect Method
 * ver 2.0 K-Means++ Clustering Algorithm Apply(2018-03-21)
 * ver 2.1 Speed improvement(2018-04-07)
 * ver 2.2 NO ACTION EMG threshold added
 */

public class GestureDetectMethod {
    private final int COMPARE_NUM = 6;          // 제스처 추가의 여지를 만듦
    private final static int STREAM_DATA_LENGTH = 5;
    private final static Double THRESHOLD = 0.01;
    private final static int KMEANS_K = 128;    //sampling한 KMEANS_K의 갯수
    private final static String FileList_kmeans = "KMEANS_DATA.dat";    //적용되는 KMEANS_DATA파일 우리가 생성함
    private static int NOACTIONEMG = 5;

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

    private int mod_cnt=30;

    public GestureDetectMethod(ArrayList<EmgData> gesture) {
        compareGesture = gesture;
        numberSmoother = new NumberSmoother();
    }
    public GestureDetectMethod(Handler handler,ArrayList<EmgData> gesture){
        compareGesture = gesture;
        this.handler=handler;
        numberSmoother = new NumberSmoother();
    }

    public GestureDetectMethod(Handler handler,ArrayList<EmgData> gesture,int mod_cnt){
        compareGesture = gesture;
        this.handler=handler;
        this.mod_cnt=mod_cnt;
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

    int cnt = 0;
    int cnt_thr = 0;
    int old_gesture_num=-1;

    //getDetectGesture Use By K-MEANS <-? It is for measuring euclidean distance between incoming emg data and K-means model data. to get the gesture user's giving.
    public GestureState getDetectGesture(byte[] data) {
        EmgData streamData = new EmgData(new EmgCharacteristicData(data));
        //streamCount++;
        streamingMaxData = streamData;

        detect_distance = 100000.0;
        detect_Num = -1;
        Log.d("detect_gesture",""+streamingMaxData.getElement(0)+","+streamingMaxData.getElement(1)+","+streamingMaxData.getElement(2)+","
                +streamingMaxData.getElement(3)+","+streamingMaxData.getElement(4)+","+streamingMaxData.getElement(5)+","+streamingMaxData.getElement(6)+","
                +streamingMaxData.getElement(7)+",");
        double sum_emg = 0.0;
        for(int element=0;element<8;element++)
            sum_emg += streamingMaxData.getElement(element);
        sum_emg /=8;
        if(sum_emg>NOACTIONEMG) {

            for (int i_gesture = 0; i_gesture < COMPARE_NUM * KMEANS_K; i_gesture++) {

                EmgData compData = compareGesture.get(i_gesture);
                double distance = dist(streamingMaxData, compData);     //Calculate Euclidean distance to compare similarity

                if (detect_distance >= distance) {
                    detect_distance = distance;
//                Log.d("detect_gesture",(int)i_gesture+"distance ("+distance+") -> "+(int)(i_gesture/KMEANS_K));
                    detect_Num = (int) (i_gesture / KMEANS_K);
                }
            }

            Log.d("detect_gesture", "distance (" + detect_distance + ") -> " + (int) (detect_Num));
            numberSmoother.addArray((Integer) (detect_Num));
        }
        //streamCount = 0;

        cnt++;
        if(cnt%mod_cnt==0){
            cnt=0;
            int result = numberSmoother.getSmoothingNumber();


//            if(old_gesture_num!=result){
//                old_gesture_num = result;
//                cnt_thr=0;
//            }else{
//                cnt_thr++;
//                if(cnt_thr>3){
//                    numberSmoother.clearArray();    // 같은게 계속 반복되면 클리어 한판!
//                    cnt_thr=0;
//                    old_gesture_num=-1;
//                }
//            }
        }

        return getEnum(old_gesture_num);
    }

    private double getThreshold() {
        return THRESHOLD;
//        return 0.9;
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
    }

    private double Manhattan_distanceCalculation(EmgData streamData, EmgData compareData){
        EmgData diffData = new EmgData();
        for(int index=0;index<8;index++){
            diffData.setElement(index,streamData.getElement(index)-compareData.getElement(index));
        }
        double return_val = diffData.get_1Norm()/streamData.get_1Norm()/compareData.get_1Norm();
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
