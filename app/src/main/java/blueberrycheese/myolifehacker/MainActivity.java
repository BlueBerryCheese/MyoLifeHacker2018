package blueberrycheese.myolifehacker;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import blueberrycheese.myolifehacker.events.ServiceEvent;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import blueberrycheese.myolifehacker.myo_manage.MyoService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,BluetoothAdapter.LeScanCallback{

    /** Intent code for requesting Bluetooth enable */
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    BluetoothDevice bluetoothDevice;
    private String deviceName;
    private static final long SCAN_PERIOD = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //For fragment tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        FragmentAdapter pagerAdapter = new FragmentAdapter(getSupportFragmentManager());

        pagerAdapter.addFragment(new TabFragment1(), "Main");
        pagerAdapter.addFragment(new TabFragment2(), "Setting");
        pagerAdapter.addFragment(new TabFragment3(), "Adaptation");
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(listener);    //페이지 변할때마다 이벤트 발생하도록 이벤트 리스너 붙착
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //서비스 위해 주석처리
/*
        mHandler = new Handler();
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(ScanListActivity.TAG);

        Log.d(TAG,deviceName+"--connected");
        if (deviceName != null) {
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // Scanning Time out by Handler.
                // The device scanning needs high energy.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(MainActivity.this);    //이벤트 핸들러로 지속해서 블루투스 연결을 확인한다고 보면 될듯합니다(베터리소모가 좀 클것으로 예상함)
                    }
                }, SCAN_PERIOD);
                mBluetoothAdapter.startLeScan(this);
            }


        }
*/

//        //Service Test
//        //TODO: ScanActivity쪽에서 itemclick시 바로 service 실행시키고 string 넘겨준 후 service에서 bluetoothdevice 선언까지 하는쪽으로..
//        startService(new Intent(this, MyoService.class));
//        EventBus.getDefault().postSticky(new ServiceEvent.testEvent("post to service test text"));
//        if(bluetoothDevice != null){
//            EventBus.getDefault().postSticky(new ServiceEvent.MyoDeviceEvent(bluetoothDevice));
//        }



    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    // Adapter for the viewpager using FragmentPagerAdapter
    class FragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {    //페이지 변환 리스너 함수(scrolled랑 selected는 알겠는데 statechanged가 언제 뭐가 발생하는지를 모르곘음
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            EventBus.getDefault().post(new EventData(bluetoothDevice));
        }

        @Override
        public void onPageSelected(int position) {
//            Toast.makeText(getApplicationContext(), "Current position: "+position, Toast.LENGTH_SHORT).show();

//서비스 위해 주석처리
//            EventBus.getDefault().post(new EventData(bluetoothDevice));     //다음부분에서 페이지가 변환이 발생되는 이벤트가 발생할때 이벤트버스를 발동시킴 다음 3페이지에서 이 이벤트에 의해서 디바이스데이터전송이 일어남을 알 수가 있다.
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {       //우측 상단의 버튼 추후에 구현 필수
        //TODO:: 세팅화면 구현
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {    //아마 현우가 뭘쓸지를 몰라서 걍 내비둠 마요연결부분만 따로 하나 추가해봄.
        //TODO:: drawer 메뉴 구현
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_tutorial) {

        } else if (id == R.id.nav_myo) {
            Intent intent = new Intent(this,ScanListActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_manage) {

        }
//        else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //서비스 위해 주석처리 예정 - 할때 implement도 함께 제거 요망
    /** Define of BLE Callback */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) { //현
        /* 재는 1번페이지에선 블루투스 연결이 발생해서 나타나는 함수시행을 못하도록 만든상태입니다...
        나중에 해결해야합니다.(textview에 대한 내용이 해당 페이지에 없어서 myocharacteristic 부분인가에서 오류가 뜰것입니다..
         */
         //TODO: 추후 블루투스 연결문제 해결 필요
        if (deviceName.equals(device.getName())) {
            mBluetoothAdapter.stopLeScan(this);
            // Trying to connect GATT
            HashMap<String,View> views = new HashMap<String,View>();
            bluetoothDevice = device;
//            mMyoCallback = new MyoGattCallback(mHandler, views);
//            mBluetoothGatt = device.connectGatt(this, false, mMyoCallback);

            EventBus.getDefault().post(new EventData(bluetoothDevice)); //post위치변경
//            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(MainActivity.this);
                    EventBus.getDefault().post(new EventData(bluetoothDevice));
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(this);
            Log.d(TAG,resultCode+"");

        }
    }


    @Override
    public void onDestroy(){
        Log.d(TAG,"MainActivity onDestroy!");
        //Service Test
        stopService(new Intent(this, MyoService.class));

        super.onDestroy();

    }

}
