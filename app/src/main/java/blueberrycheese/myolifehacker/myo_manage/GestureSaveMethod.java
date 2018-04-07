package blueberrycheese.myolifehacker.myo_manage;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Seongho on 2017-12-01.
 * ver 1.2 save Emg raw_data
 */

public class GestureSaveMethod {
    private final static String TAG = "Myo_KMEANS_compare";
    private final static String FileName = "compareData.dat";

    private final static int COMPARE_NUM = 6;
    private final static int SAVE_DATA_LENGTH = 5;
    private final static int AVERAGING_LENGTH = 10;

    private final static int JUST_SAVE_DATA_LEN = 10;

    private ArrayList<EmgCharacteristicData> rawDataList = new ArrayList<>();
    private ArrayList<EmgData> maxDataList = new ArrayList<>();
    private ArrayList<EmgData> compareGesture = new ArrayList<>();
    private ArrayList<EmgData> rawcompareGesture = new ArrayList<>();
    private ArrayList<EmgData> compareGesture_k = new ArrayList<>();
    private SaveState saveState = SaveState.Ready;

    private int dataCounter = 0;
    private int gestureCounter = 0;

    private final static int KMEANS_K = 128;
    private final static String FileList_kmeans = "KMEANS_DATA.dat";
    private final static String FileList[] = {"Gesture1.txt","Gesture2.txt","Gesture3.txt","Gesture4.txt","Gesture5.txt","Gesture6.txt"};
    private final static String FileList_Raw[] = {"Gesture1_Raw.txt","Gesture2_Raw.txt","Gesture3_Raw.txt","Gesture4_Raw.txt","Gesture5_Raw.txt","Gesture6_Raw.txt"};


    private Clusterer<DoublePoint> clusterer;       //apache commons math structure
    private List<DoublePoint> doublePointList;

    public GestureSaveMethod(){
        saveState=SaveState.Ready;
        Log.d(TAG,"GestureSaveMethod None");
    }

    public GestureSaveMethod(int i,Context context) {
        Log.d(TAG,"GestureSaveMethod None2");


        MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG,FileList_kmeans);
        if(!dataFileReader.getMyoDataFile().exists()){
            dataFileReader.getMyoDataFile().getParentFile().mkdirs();
        }
        clusterer = new KMeansPlusPlusClusterer<DoublePoint>(KMEANS_K,-1,new EuclideanDistance());
        if (dataFileReader.load().size() == KMEANS_K*COMPARE_NUM) {
            compareGesture_k = dataFileReader.load();
            saveState = SaveState.Have_Saved;
        }else{
            try{
                saveState = SaveState.Now_Saving;
                InputStream in;
                PrintWriter writer = null;
                writer = new PrintWriter(dataFileReader.getMyoDataFile());
                for(int j=0;j<COMPARE_NUM;j++){
                    doublePointList = new ArrayList<>();
                    int resID= context.getResources().getIdentifier("gesture"+(j+1),"raw","blueberrycheese.myolifehacker");
                    in = context.getResources().openRawResource(resID);
                    InputStreamReader streamReader = new InputStreamReader(in,"UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    String line;
                    StringTokenizer stringTokenizer;
                    int cntt=1000;          //사실 지금 필요없는데 내 폰 상태가... ㅜㅜ 참고로 text파일 내전용임 내데이터만들가있음 실험할때 바꾸세요.
                    while(cntt-->0&&((line = bufferedReader.readLine())!=null)){
                        stringTokenizer = new StringTokenizer(line,",");
                        double[] emgDat = new double[8];
                        for(int k=0;k<8;k++){
                            emgDat[k] = Double.parseDouble(stringTokenizer.nextToken());
                        }
                        doublePointList.add(new DoublePoint(emgDat));
                    }
                    streamReader.close();
                    bufferedReader.close();
                    stringTokenizer = null;
                    Log.d(TAG,"Loading txt size is "+doublePointList.size());
//                    Toast.makeText(context,"K-MEANS_lization about"+(i+1)+" data", Toast.LENGTH_LONG).show();
                    List<? extends Cluster<DoublePoint>> res = clusterer.cluster(doublePointList);

                    try {

                        for(Cluster<DoublePoint> re : res){
                            List<DoublePoint> list1 = re.getPoints();
                            //Log.e(TAG,list1.toString());
                            double[] avg_emg = new double[8];
//                            for(DoublePoint rr:list1){
//                                double[] _emg = rr.getPoint();
//                                for(int aa=0;aa<8;aa++){
//                                    avg_emg[aa] += _emg[aa];
//                                }
//                            }
//                            for(int aa=0;aa<8;aa++){
//                                avg_emg[aa] /= list1.size();
//                            }
                            avg_emg = list1.get(0).getPoint(); //이것인가!
                            writer.println(""+avg_emg[0]+","+avg_emg[1]+","+avg_emg[2]+","+avg_emg[3]+","+avg_emg[4]+","+avg_emg[5]+","+avg_emg[6]+","+avg_emg[7]+",");
                            Log.d(TAG,""+avg_emg[0]+","+avg_emg[1]+","+avg_emg[2]+","+avg_emg[3]+","+avg_emg[4]+","+avg_emg[5]+","+avg_emg[6]+","+avg_emg[7]+",");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }

                if(writer != null){
                    writer.close();
                }
//                Toast.makeText(context,"Loading Default Data... Plz Wait", Toast.LENGTH_LONG).show();
                compareGesture_k = dataFileReader.load();
                saveState = SaveState.Have_Saved;
            }catch(Exception e){
                Log.e(TAG,e.getMessage());
            }
        }
    }

    public enum SaveState {
        Ready,
        Not_Saved,
        Now_Saving,
        Have_Saved,
    }

    public void addData(byte[] data) {
        rawDataList.add(new EmgCharacteristicData(data));
        dataCounter++;
        if (dataCounter % SAVE_DATA_LENGTH == 0) {
            EmgData dataMax = new EmgData();
            int count = 0;
            for (EmgCharacteristicData emg16Temp : rawDataList) {
                EmgData emg8Temp = emg16Temp.getEmg8Data_abs();
                //EmgData emg8Temp = emg16Temp.getEmg8Data();
                //rawbaseDataList.add(emg8Temp);
                if (count == 0) {
                    dataMax = emg8Temp;
                } else {
                    for (int i_element = 0; i_element < 8; i_element++) {
                        if (Math.abs(emg8Temp.getElement(i_element)) > Math.abs(dataMax.getElement(i_element))) {
                            dataMax.setElement(i_element, emg8Temp.getElement(i_element));
                        }
                    }
                }
                count++;
            }
            if (rawDataList.size() < SAVE_DATA_LENGTH) {
                Log.e("GestureDetect", "Small rawData : " + rawDataList.size());
            }
            maxDataList.add(dataMax);
            rawDataList = new ArrayList<>();
        }
        if (dataCounter == SAVE_DATA_LENGTH * AVERAGING_LENGTH) {
            saveState = SaveState.Not_Saved;
            makeCompareData();
            gestureCount();
            dataCounter = 0;
        }
    }

    private void gestureCount() {
        gestureCounter++;
        if (gestureCounter == COMPARE_NUM) {
            saveState = SaveState.Have_Saved;
            gestureCounter = 0;
            MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG,FileName);
            dataFileReader.saveMAX(getCompareDataList());
        }
    }

    public void addData(byte[] data,int num) {
        rawDataList.add(new EmgCharacteristicData(data));
        dataCounter++;
        if (dataCounter % SAVE_DATA_LENGTH == 0) {
            EmgData dataMax = new EmgData();
            int count = 0;
            for (EmgCharacteristicData emg16Temp : rawDataList) {
                EmgData emg8Temp = emg16Temp.getEmg8Data_abs();
                //EmgData emg8Temp = emg16Temp.getEmg8Data();
                rawcompareGesture.add(emg8Temp);
                if (count == 0) {
                    dataMax = emg8Temp;
                } else {
                    for (int i_element = 0; i_element < 8; i_element++) {
                        if (Math.abs(emg8Temp.getElement(i_element)) > Math.abs(dataMax.getElement(i_element))) {
                            dataMax.setElement(i_element, emg8Temp.getElement(i_element));
                        }
                    }
                }
                count++;
            }
            if (rawDataList.size() < SAVE_DATA_LENGTH) {
                Log.e("GestureDetect", "Small rawData : " + rawDataList.size());
            }
            maxDataList.add(dataMax);
            rawDataList = new ArrayList<>();
        }
        if (dataCounter == SAVE_DATA_LENGTH * AVERAGING_LENGTH) {
            saveState = SaveState.Not_Saved;
            makeCompareData();
            gestureCount(num);
            dataCounter = 0;
        }
    }

    private void gestureCount(int num) {
        gestureCounter++;
        Log.e("GestureDetect", "CompareData Size : " + getCompareDataList().size());
        if (gestureCounter == JUST_SAVE_DATA_LEN) {
            saveState = SaveState.Have_Saved;
            gestureCounter = 0;
            MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG,FileList[num]);
            dataFileReader.saveMAX(getCompareDataList());
            MyoDataFileReader dataFileReader2 = new MyoDataFileReader(TAG,FileList_Raw[num]);
            dataFileReader2.saveRAW_max(getRawCompareDataList());
            compareGesture = new ArrayList<EmgData>();
            rawcompareGesture = new ArrayList<EmgData>();
        }
    }

    private void makeCompareData() {
        EmgData tempData  = new EmgData();

        // Get each Max EMG-elements of maxDataList
        int count = 0;
        for (EmgData emg8Temp : maxDataList) {
            //rawcompareGesture.add(emg8Temp);
            if (count == 0) {
                tempData = emg8Temp;
            } else {
                for (int i_element = 0; i_element < 8; i_element++) {
                    if (Math.abs(emg8Temp.getElement(i_element)) > Math.abs(tempData.getElement(i_element))) {
                        tempData.setElement(i_element, emg8Temp.getElement(i_element));
                    }
                }
            }
            count++;
        }
        // Averaging EMG-elements of maxDataList
      /*  int count = 0;
        for (EmgData emg8Temp : maxDataList) {
            if (count == 0) {
                tempData = emg8Temp;
            } else {
                for (int i_element = 0; i_element < 8; i_element++) {
                    tempData.setElement(i_element, tempData.getElement(i_element) + emg8Temp.getElement(i_element));
                }
            }
            count++;
        }
        for (int i_element = 0; i_element < 8; i_element++) {
            tempData.setElement(i_element, tempData.getElement(i_element)/maxDataList.size());
        }*/

        if (maxDataList.size() < AVERAGING_LENGTH) {
            Log.e("GestureDetect", "Small aveData : " + maxDataList.size());
        }
        compareGesture.add(tempData);
        maxDataList = new ArrayList<>();
    }

    public SaveState getSaveState() {
        return saveState;
    }

    public void setState(SaveState state) {
        saveState = state;
    }

    public int getGestureCounter() {
        return gestureCounter;
    }

    public ArrayList<EmgData> getCompareDataList() {
        return compareGesture_k;
    }

    public ArrayList<EmgData> getRawCompareDataList() {
        return rawcompareGesture;
    }
}
