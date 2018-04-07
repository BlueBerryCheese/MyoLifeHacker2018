package blueberrycheese.myolifehacker.myo_manage;

import java.util.ArrayList;

/**
 * Modified by Seongho on 2017-12-01.
 *
 * This class help you to read the raw EMG-data from Myo.
 * One raw byte array has 16 byte data. But Myo sensors are only 8.
 * From [https://github.com/thalmiclabs/myo-bluetooth/],
 * EMG-Data will send
 * ------------------------------------------------------------
 *   int8_t sample1[8];       ///< 1st sample of EMG data.
 *   int8_t sample2[8];       ///< 2nd sample of EMG data.
 * ------------------------------------------------------------ .
 *
 */

public class EmgCharacteristicData {
    private ByteReader emgData = new ByteReader();

    public EmgCharacteristicData(byte[] byteData) {
        emgData.setByteData(byteData);
    }

    public EmgCharacteristicData(ByteReader byteReader){
        emgData = byteReader;
    }

    public String getLine() {
        StringBuilder return_SB = new StringBuilder();
        for (int i_emg_num = 0; i_emg_num < 16; i_emg_num++) {
            return_SB.append(String.format("%d,", emgData.getByte()));
        }
        return return_SB.toString();
    }

    public EmgData getEmg8Data_abs() {      //우리가 현재 받는 데이터는 절대값을 씌운데이터
        EmgData emg8Data_max_abs = new EmgData();
        ArrayList<Double> temp_Array = new ArrayList<>();
        for (int i_emg_num = 0; i_emg_num < 16; i_emg_num++) {
            double temp = emgData.getByte();
            temp_Array.add(temp);
        }
        for (int i_emg8 = 0; i_emg8 < 8; i_emg8++) {
            if (Math.abs(temp_Array.get(i_emg8)) < Math.abs(temp_Array.get(i_emg8 + 8))){
                emg8Data_max_abs.addElement(Math.abs(temp_Array.get(i_emg8 + 8)));
            } else {
                emg8Data_max_abs.addElement(Math.abs(temp_Array.get(i_emg8)));
            }
        }
        return emg8Data_max_abs;
    }

    public EmgData getEmg8Data() {
        /*
            현재 사용하지 않음
         */
        EmgData emg8Data_max = new EmgData();
        ArrayList<Double> temp_Array = new ArrayList<>();
        for (int i_emg_num = 0; i_emg_num < 16; i_emg_num++) {
            double temp = emgData.getByte();
            temp_Array.add(temp);
        }
        for (int i_emg8 = 0; i_emg8 < 8; i_emg8++) {
            if (Math.abs(temp_Array.get(i_emg8)) < Math.abs(temp_Array.get(i_emg8 + 8))){
                emg8Data_max.addElement(temp_Array.get(i_emg8 + 8));
            } else {
                emg8Data_max.addElement(temp_Array.get(i_emg8));
            }
        }
        return emg8Data_max;
    }


}

