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
    public void removeFile() {
        // File dir = getDirectory();
        String mPath = "/sdcard/"+TAG+"/";
        File dir = new File(mPath);
        //dir.getParentFile().mkdirs();
        String[] d_file=dir.list();
        // Log.e(TAG,BASE_DIR);
        //   Log.e(TAG,dirname);
        Log.e(TAG,dirname);
        Log.e(TAG,"remove in :  "+d_file.length);
        //Log.e(TAG,);
        if(d_file !=null) {
            for(int i=0; i<d_file.length; i++) {
                String d_filename = d_file[i];
                Log.e(TAG,"remove file name : "+filename+ " 인식 성공");
                File f = new File(mPath+d_filename);

                if(f.exists()) {
                    Log.e(TAG,"remove file name : "+filename+ " 삭제 성공");
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