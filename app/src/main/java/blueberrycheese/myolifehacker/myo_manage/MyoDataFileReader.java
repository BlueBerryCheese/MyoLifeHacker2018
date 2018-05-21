package blueberrycheese.myolifehacker.myo_manage;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by pc on 2018-03-10.
 */



public class MyoDataFileReader {
    private static File BASE_DIR = new File("sdcard");
    public static void init(File base){
        BASE_DIR = base;
    }
    //private String TAG = "MyoDataFile";
    private String TAG = "Myo_KMEANS_compare";
    private String dirname = "";
    private String filename = "";

    public MyoDataFileReader(String dirname, String filename) {
        this.dirname = dirname;
        this.filename = filename;
    }

    public void saveRAW(ArrayList<EmgCharacteristicData> myoDataList) {
        File targetFile = getMyoDataFile();
            targetFile.getParentFile().mkdirs();

            PrintWriter writer = null;
            try {

                writer = new PrintWriter(targetFile);
                for(EmgCharacteristicData myoData : myoDataList){
                    writer.println(myoData.getLine());
                }
            } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(writer != null){
                writer.close();
            }
        }
    }

    public void saveRAW_max(ArrayList<EmgData> myoDataList) {
        File targetFile = getMyoDataFile();
        targetFile.getParentFile().mkdirs();

        PrintWriter writer = null;
        try {
            BufferedWriter bw= new BufferedWriter(new FileWriter(targetFile,true));  //
            writer = new PrintWriter(bw,true);                //
            //writer = new PrintWriter(targetFile);
            for(EmgData myoData : myoDataList){
                writer.println(myoData.getLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(writer != null){
                writer.close();
            }
        }
    }

    public void saveMAX(ArrayList<EmgData> myoDataList) {
        File targetFile = getMyoDataFile();
        targetFile.getParentFile().mkdirs();

        Log.d(TAG,"myoDataList size : " + myoDataList.size());
        PrintWriter writer = null;
        try {
            BufferedWriter bw= new BufferedWriter(new FileWriter(targetFile,true));  //
            writer = new PrintWriter(bw,true);                //
          //  writer = new PrintWriter(targetFile);
            for(EmgData myoData : myoDataList){
                writer.println(myoData.getLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(writer != null){
                writer.close();
            }
        }
    }


    ////////////////////////////////////////////////////
    public void removeFile(int index) { //파일 삭제 메소드
       // File dir = getDirectory();
        String mPath = "/sdcard/"+TAG+"/";      // Path 지정
        File dir = new File(mPath);
      //dir.getParentFile().mkdirs();
        String[] d_file=dir.list();     // Path내 파일 리스트 저장
       // Log.e(TAG,BASE_DIR);
     //   Log.e(TAG,dirname);

        //Log.e(TAG,dirname);
        //Log.e(TAG,"remove in :  "+d_file.length);
        //Log.e(TAG,);
        if(d_file !=null) {
            for(int i=0; i<d_file.length; i++) {   // for문 돌리면서 Fragment3에서 선택한 numberPicker와 값이 일치하는 것 찾아서 삭제
                File f=null;
                String d_filename = d_file[i];
               // Log.e(TAG,"remove file name : "+d_filename+ " 인식 성공");

                switch (index) {
                    case 0:         //Model
                        //Log.e(TAG, "switch 0 : " + d_filename + " 들어옴");
                        if (d_filename.equals("KMEANS_DATA.dat")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 0 : " + d_filename + " 인식 성공");
                        }
                        break;
                    case 1:         //All
                        //Log.e(TAG, "switch 1 : " + d_filename + " 들어옴");

                        f = new File(mPath + d_filename);
                        Log.e(TAG, "switch 1 : " + d_filename + " 인식 성공");
                        break;
                    case 2:         //All_Geusture
                        //Log.e(TAG, "switch 2 : " + d_filename + " 들어옴");
                        if (!d_filename.equals("KMEANS_DATA.dat")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 2 : " + d_filename + " 인식 성공");
                        }
                        break;
                    case 3:         // 1
                        //Log.e(TAG, "switch 3 : " + d_filename + " 들어옴");
                        if (d_filename.equals("Gesture1_Raw.txt")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 3 : " + d_filename + " 인식 성공");
                        }
                        break;
                    case 4:         //2
                       // Log.e(TAG, "switch 4 : " + d_filename + " 들어옴");
                        if (d_filename.equals("Gesture2_Raw.txt")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 4 : " + d_filename + " 인식 성공");
                        }
                        break;
                    case 5:         //3
                        //Log.e(TAG, "switch 5 : " + d_filename + " 들어옴");
                        if (d_filename.equals("Gesture3_Raw.txt")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 5 : " + d_filename + " 인식 성공");
                        }
                        break;
                    case 6:         //4
                       // Log.e(TAG, "switch 6 : " + d_filename + " 들어옴");
                        if (d_filename.equals("Gesture4_Raw.txt")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 6 : " + d_filename + " 인식 성공");
                        }
                        break;
                    case 7:         //5
                        //Log.e(TAG, "switch 7 : " + d_filename + " 들어옴");
                        if (d_filename.equals("Gesture5_Raw.txt")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 7 : " + d_filename + " 인식 성공");
                        }
                        break;
                    case 8:         //6
                        //Log.e(TAG, "switch 8 : " + d_filename + " 들어옴");
                        if (d_filename.equals("Gesture6_Raw.txt")) {
                            f = new File(mPath + d_filename);
                            Log.e(TAG, "switch 8 : " + d_filename + " 인식 성공");
                        }
                }
              //  Log.e(TAG, "1 : " + f.length() + " 인식 성공");
               // Log.e(TAG, "2 : " + f.exists() + " 인식 성공");
                if(f!=null) {
                    Log.e(TAG,"remove file name : "+d_filename+ " 삭제 성공");
                    f.delete();
                }

            }
        }
    }
//////////////////////////////////////////////////////////

    public ArrayList<EmgData> load(){


        File directory = getDirectory();


        ArrayList<EmgData> dataList = new ArrayList<EmgData>();
        if(!directory.isDirectory()){
            return dataList;
        }

        File targetFile = getMyoDataFile();
        FileReader fr = null;
        try{
            fr = new FileReader(targetFile);
            BufferedReader br = new BufferedReader(fr);

            String line = null;
            while((line = br.readLine()) != null){
                line = line.trim();
                EmgData data = new EmgData();
                data.setLine(line);

                dataList.add(data);
            }
            br.close();
        }
        catch(Exception e){
            if(fr != null){
                try {
                    fr.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return dataList;
    }


    public File getMyoDataFile(){
        return new File(getDirectory(), filename);
    }

    private File getDirectory(){
        return new File(BASE_DIR, dirname);
    }

}
