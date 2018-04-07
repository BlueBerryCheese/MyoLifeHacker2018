package blueberrycheese.myolifehacker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.os.Handler;

import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;

/**
 * Created by pc on 2018-04-05.
 */

public class EventData {    //Eventbus라는 라이브러리로 데이터를 이벤트 발생시 전송하는데 그때 보내주려는 데이터를 클래스로 묶어서 보낸준다고 이해하면 될 듯 함.
    public BluetoothDevice device;

    public EventData(BluetoothDevice device){
        this.device=device;
    }

}
