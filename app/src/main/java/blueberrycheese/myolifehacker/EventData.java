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

public class EventData {
    public BluetoothDevice device;

    public EventData(BluetoothDevice device){
        this.device=device;
    }

}
