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
import java.io.FileReader;
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

    private final static int COMPARE_NUM = 6;       //제스처 갯수
    private final static int SAVE_DATA_LENGTH = 5;      // 세이브 할 데이터 갯수
    private final static int AVERAGING_LENGTH = 10;
    private final static int READING_LENGTH = 1000;  // 안드로이드에 저장되있는 파일의 길이
    private final static int JUST_SAVE_DATA_LEN = 5;    // 세이브 할 데이터 갯수
   // private final static int JUST_SAVE_DATA_LEN = 5;

    private ArrayList<EmgCharacteristicData> rawDataList = new ArrayList<>();
    private ArrayList<EmgData> maxDataList = new ArrayList<>();
    private ArrayList<EmgData> compareGesture = new ArrayList<>();
    private ArrayList<EmgData> rawcompareGesture = new ArrayList<>();
    private ArrayList<EmgData> compareGesture_k = new ArrayList<>();
    private SaveState saveState = SaveState.Ready;

    private int dataCounter = 0;
    private int gestureCounter = 0;
    private int save_index = 0;     //
    private  int count_adap=0;
    private final static int KMEANS_K = 128;       // K-means의 k값
    private final static String FileList_kmeans = "KMEANS_DATA.dat";
    private final static String FileList[] = {"Gesture1.txt", "Gesture2.txt", "Gesture3.txt", "Gesture4.txt", "Gesture5.txt", "Gesture6.txt"};
    private final static String FileList_Raw[] = {"Gesture1_Raw.txt", "Gesture2_Raw.txt", "Gesture3_Raw.txt", "Gesture4_Raw.txt", "Gesture5_Raw.txt", "Gesture6_Raw.txt"};


    private Clusterer<DoublePoint> clusterer;       //apache commons math structure
    private List<DoublePoint> doublePointList;

    // defalut 값. 보통 이 아래거 변수 넣는 거만씀
    public GestureSaveMethod() {
        saveState = SaveState.Ready;
        Log.d(TAG, "GestureSaveMethod None");
    }

    // sync를 눌렀을 때 모델 만드는 부분. (i: 제스처 인덱스, context, percent: 어댑터 값)
    // TODO: GestureSaveMethod 생성자에 int i (Gesture 넘버)넣어서 adaptive fragment 에서 numberpicker 계속 바꿀때마다...
    // TODO: object를 다시 재생성 하게 하지말고 차라리 int 변수 하나 따로 선언하고 그것을 바꿀 수 있는 Method 하나 만들어서 이 i를 바꾸는게 효율적일듯 => 추가: int i 아예 쓸모 없는 부분인 듯
    public GestureSaveMethod(int i, Context context,double percent) {
        Log.d(TAG, "GestureSaveMethod None2");


        MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG, FileList_kmeans);
        if (!dataFileReader.getMyoDataFile().exists()) {
            dataFileReader.getMyoDataFile().getParentFile().mkdirs();
        }
        clusterer = new KMeansPlusPlusClusterer<DoublePoint>(KMEANS_K, -1, new EuclideanDistance());
        if (dataFileReader.load().size() == KMEANS_K * COMPARE_NUM) {   // 사이즈가 (K-means k값 * 제스처 갯수) 와 같을 때
            compareGesture_k = dataFileReader.load();
            saveState = SaveState.Have_Saved;
        } else {
            try {
                saveState = SaveState.Now_Saving;
                InputStream in;
                PrintWriter writer = null;
                writer = new PrintWriter(dataFileReader.getMyoDataFile());
                FileReader fr = null; //
                BufferedReader br = null;
                for (int j = 0; j < COMPARE_NUM; j++) {     // 어댑터와 안드로이드 내 txt 파일을 불러오는 부분.
                    doublePointList = new ArrayList<>();
                    int resID = context.getResources().getIdentifier("gesture" + (j + 1), "raw", context.getPackageName());
                    Log.e(TAG,"resID is "+resID);
                    in = context.getResources().openRawResource(resID);
                    InputStreamReader streamReader = new InputStreamReader(in, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    String line;
                    StringTokenizer stringTokenizer;

                    try {       // 어댑터 txt 불러오는 부분
                        MyoDataFileReader dataFileReader2 = new MyoDataFileReader(TAG, FileList_Raw[j]);  //
                        fr = new FileReader(dataFileReader2.getMyoDataFile()); //
                        br = new BufferedReader(fr);  //
                        // if (fr.ready()) {

                        int cnt_adapter = 0;
                        while ((line = br.readLine()) != null) {    // 어댑터 txt 끝까지 불러옴.
                            //while(cnt_adapter++<READING_LENGTH&&((line = br.readLine())!=null)){
                            stringTokenizer = new StringTokenizer(line, ",");
                            double[] emgDat = new double[8];
                            for (int k = 0; k < 8; k++) {
                                emgDat[k] = Double.parseDouble(stringTokenizer.nextToken());
                            }
                            doublePointList.add(new DoublePoint(emgDat));
                            Log.e(TAG, "Loading Adapter txt size is " + doublePointList.size());
                        }
                        //   }
                        fr.close();
                        br.close();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    ////////////////////////////////
                    count_adap=0; // 안드로이드 파일쪽 count세는 변수
                    // 안드로이드 파일내 txt 가져옴.
                    // 가져올 때 (안드로이드 파일 길이 지정 값* 몇 %나 불러올건지) && 끝까지 불러옴
                    while (READING_LENGTH*percent>count_adap && ((line = bufferedReader.readLine()) != null)) {
                        stringTokenizer = new StringTokenizer(line, ",");
                        double[] emgDat = new double[8];
                        for (int k = 0; k < 8; k++) {
                            emgDat[k] = Double.parseDouble(stringTokenizer.nextToken());
                        }
                        doublePointList.add(new DoublePoint(emgDat));
                        count_adap++;
                    }
                    streamReader.close();
                    bufferedReader.close();

                    stringTokenizer = null;
                    Log.e(TAG, "Loading txt size is================ " + doublePointList.size());
//                    Toast.makeText(context,"K-MEANS_lization about"+(i+1)+" data", Toast.LENGTH_LONG).show();
                    List<? extends Cluster<DoublePoint>> res = clusterer.cluster(doublePointList);

                    try {
                        for (Cluster<DoublePoint> re : res) {
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
                            writer.println("" + avg_emg[0] + "," + avg_emg[1] + "," + avg_emg[2] + "," + avg_emg[3] + "," + avg_emg[4] + "," + avg_emg[5] + "," + avg_emg[6] + "," + avg_emg[7] + ","); //출력
                            Log.d(TAG, "" + avg_emg[0] + "," + avg_emg[1] + "," + avg_emg[2] + "," + avg_emg[3] + "," + avg_emg[4] + "," + avg_emg[5] + "," + avg_emg[6] + "," + avg_emg[7] + ",");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (writer != null) {
                    writer.close();
                }
//                Toast.makeText(context,"Loading Default Data... Plz Wait", Toast.LENGTH_LONG).show();
                compareGesture_k = dataFileReader.load();
                saveState = SaveState.Have_Saved;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    // 제스처 상태
    public enum SaveState {
        Ready,
        Not_Saved,
        Now_Saving,
        Have_Saved,
    }

    // 이부분 부터 이 아래 gesturecount()는 쓰진 않음.
    // 추후에 삭제할 수도
    ////////////////////////////////////////////////////////////////
    /*
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
            MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG, FileName);
            dataFileReader.saveMAX(getCompareDataList());
        }
    }
    */
///////////////////////////////////////////////////////////

    // 값들을 추가하는 메소드
    public void addData(byte[] data, int num) {
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
        if (dataCounter == SAVE_DATA_LENGTH * AVERAGING_LENGTH) {   //
            saveState = SaveState.Not_Saved;
            makeCompareData();  // 모델 생성
            gestureCount(num);
            dataCounter = 0;
        }
    }

    // 세이브 도중에 numberPicker 변경하였을 때 세이브 제스처 번호 초기화
    public void change_save_index_numberPicker() {
        gestureCounter=0;
    }

    // 세이브 numberPicker 변경하였을 때 세이브 번호 값 저장
    public void change_save_numer_numberPicker(int i) {save_index=i; }

    // 제스처를 count 함.
    private void gestureCount(int num) {
        gestureCounter++;

        Log.e("GestureDetect", "CompareData Size : " + compareGesture.size());
        if (gestureCounter == JUST_SAVE_DATA_LEN) {     //제스처count가 (지정한 저장 길이) 와 같을 때
            saveState = SaveState.Have_Saved;
            gestureCounter = 0;         // count 초기화
            MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG, FileList[num]);   // Gesture .txt 파일 저장
            dataFileReader.saveMAX(getCompareDataList());
            MyoDataFileReader dataFileReader2 = new MyoDataFileReader(TAG, FileList_Raw[num]);  // Gesture _Raw.txt 파일 저장
            dataFileReader2.saveRAW_max(getRawCompareDataList());
            compareGesture = new ArrayList<EmgData>();
            rawcompareGesture = new ArrayList<EmgData>();
            if (save_index == COMPARE_NUM - 1) {     //  6번 제스처 까지 저장 완료하면
                save_index = 0;       //    다시 0번으로 돌아가기위해 변수값 변경
            } else {                 //
                save_index++;       //  다음 번호로 가기 위해 번호 ++
            }                       // 제스처 세이브 NumberPicker 위해서
        }
    }

    // 모델 만드는 메소드
    private void makeCompareData() {
        EmgData tempData = new EmgData();

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
    }  // 지정한 제스처 세이브 길이값 리턴

    public int getSaveIndex() {
        return save_index;
    }  // 현재 몇번 제스처인지 리턴


    public ArrayList<EmgData> getCompareDataList() {
        return compareGesture_k;
    }

    public ArrayList<EmgData> getRawCompareDataList() {
        return rawcompareGesture;
    }
}