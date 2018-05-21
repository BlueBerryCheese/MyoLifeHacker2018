package blueberrycheese.myolifehacker;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import blueberrycheese.myolifehacker.events.ServiceEvent;
import blueberrycheese.myolifehacker.myo_manage.MyoService;

/**
 * Created by pc on 2018-04-05.
 */

public class ScanListActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    public static final int MENU_SCAN = 0;
    public static final int LIST_DEVICE_MAX = 5;

    public static String TAG = "BluetoothList";

    /** Device Scanning Time (ms) */
    private static final long SCAN_PERIOD = 5000;

    /** Intent code for requesting Bluetooth enable */
    private static final int REQUEST_ENABLE_BT = 1;

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private ArrayList<String> deviceNames = new ArrayList<>();
    private String myoName = null;

    private ArrayAdapter<String> adapter;

    //For service
    private ArrayList<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    BluetoothDevice bluetoothDevice_Selected;
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanlist);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        permissionCheck_ble(this);

        BluetoothManager mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mHandler = new Handler();

        ListView lv = (ListView) findViewById(R.id.listView1);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_expandable_list_item_1, deviceNames);

        lv.setAdapter(adapter);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("Shared",prefs.getString("lock_vibrate_power",""));
        Log.d("Shared",prefs.getString("recog_vibrate_power",""));
        Log.d("Shared",prefs.getString("conn_vibrate_power",""));


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " connect", Toast.LENGTH_SHORT).show();
                myoName = item;

                Intent intent;
                intent = new Intent(getApplicationContext(), MainActivity.class);

//서비스 위해 주석처리
//                intent.putExtra(TAG, myoName);

                startActivity(intent);


//              //Service Test
                //TODO: Need to improve code of getting bluetooth device info for connection
                for(int i = 0;i<deviceNames.size();i++){
                    BluetoothDevice bluetoothDeviceCmp = bluetoothDeviceList.get(i);
                    if(bluetoothDeviceCmp.getName().equals(myoName)){
                        bluetoothDevice_Selected = bluetoothDeviceCmp;
                        Log.d("BLEActivity", "Selected BluetoothDevice : " + bluetoothDevice_Selected.getName());
                        break;
                    }
                }

                startService(new Intent(getApplicationContext(), MyoService.class));
                EventBus.getDefault().postSticky(new ServiceEvent.MyoDeviceEvent(bluetoothDevice_Selected));


//                String vp = prefs.getString("vibrate_power","강하게");
//                int vpp;
//                int rc = Integer.parseInt(prefs.getString("recognizing_count","30"));
//                boolean iv = prefs.getBoolean("vibrate",true);
//                if(vp.equals("강하게"))
//                    vpp=3;
//                else if(vp.equals("보통"))
//                    vpp=2;
//                else if(vp.equals("약하게"))
//                    vpp=1;
//                else
//                    vpp=3;
//                Log.d(TAG,"setting_event : " + vp+" , " + rc+" , " + iv);
//                EventBus.getDefault().postSticky(new ServiceEvent.SettingEvent(vpp,iv,rc));
            }
        });
    }

    public void onClickScan(View v) {
        scanDevice();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        // Device Log
        ParcelUuid[] uuids = device.getUuids();
        String uuid = "";
        if (uuids != null) {
            for (ParcelUuid puuid : uuids) {
                uuid += puuid.toString() + " ";
            }
        }

        String msg = "name=" + device.getName() + ", bondStatus="
                + device.getBondState() + ", address="
                + device.getAddress() + ", type" + device.getType()
                + ", uuids=" + uuid;
        Log.d("BLEActivity", msg);

        if (device.getName() != null && !deviceNames.contains(device.getName())) {
            deviceNames.add(device.getName());
            //For service
            bluetoothDeviceList.add(device);
        }
    }

    public void scanDevice() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            deviceNames.clear();
            // Scanning Time out by Handler.
            // The device scanning needs high energy.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(ScanListActivity.this);

                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Stop Device Scan", Toast.LENGTH_SHORT).show();

                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(ScanListActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            scanDevice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {

            for(int index=0;index<grantResults.length;index++){
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {

                }
                else {
                    Toast.makeText(ScanListActivity.this, "권한 요청을 거부했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public void permissionCheck_ble(final Context context){
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)+checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    +checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)&&
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)&&shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 단말기의 권한들이 필요합니다. 계속하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
                                    }

                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(context, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }else{
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                }
            }
        }
    }
}
