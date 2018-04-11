package blueberrycheese.myolifehacker;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import blueberrycheese.myolifehacker.commons.LoadingDialog;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectSendResultAction;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import blueberrycheese.myolifehacker.myo_manage.NopModel;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraView;

import java.util.ArrayList;
import java.util.List;

import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import butterknife.BindView;
import butterknife.ButterKnife;

import static java.security.AccessController.getContext;


public class CameraMainActivity extends AppCompatActivity {

    private static final String TAG = CameraMainActivity.class.getSimpleName();

    @BindView(R.id.cameraactivityMain)
    DrawerLayout drawerLayout;

    @BindView(R.id.leftDrawer)
    ListView leftDrawer;

    ActionBarDrawerToggle drawerToggle;

    @BindView(R.id.contentFrame)
    ViewGroup parent;

    @BindView(R.id.camera)
    CameraView camera;

    private ArrayAdapter drawerAdapter;

    private int cameraMethod = CameraKit.Constants.METHOD_STILL;
    private boolean cropOutput = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    private BluetoothDevice device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_main);
        ButterKnife.bind(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setupDrawerAndToolbar();

        camera.setMethod(cameraMethod);
        camera.setCropOutput(cropOutput);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
        camera.setZoom(1);
        camera.setMethod(CameraKit.Constants.METHOD_STILL);
    }

    @Override
    protected void onPause() {
        camera.stop();
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_main_about: {
                Drawable icon = ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
                new AlertDialog.Builder(this)
                        .setIcon(icon)
                        .setTitle(getString(R.string.about_dialog_title))
                        .setMessage(getString(
                                R.string.about_dialog_message
//                                BuildConfig.VERSION_NAME,
//                                BuildConfig.VERSION_CODE,
////                                com.wonderkiln.camerakit.BuildConfig.VERSION_NAME 주석하고 아래껄로 바꿈
//                                BuildConfig.VERSION_NAME
                        ))
                        .setPositiveButton("DONE", null)
                        .show();
                return true;
            }

            case R.id.menu_main_github: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github)));
                startActivity(intent);
                return true;
            }

            case R.id.menu_main_website: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_website)));
                startActivity(intent);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupDrawerAndToolbar() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

        };

        drawerLayout.setDrawerListener(drawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            View toolbarView = getLayoutInflater().inflate(R.layout.camera_action_bar, null, false);
            TextView titleView = toolbarView.findViewById(R.id.toolbar_title);
            titleView.setText(Html.fromHtml("<b>Camera</b>"));

            getSupportActionBar().setCustomView(toolbarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        final List<CameraSetting> options = new ArrayList<>();
        options.add(captureMethodSetting);
        options.add(cropSetting);

        drawerAdapter = new ArrayAdapter<CameraSetting>(this, R.layout.camera_drawer_list_item, R.id.text1, options) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(R.id.text1);
                text1.setTextColor(Color.WHITE);

                final TextView text2 = view.findViewById(R.id.text2);
                text2.setTextColor(Color.WHITE);

                text1.setText(options.get(position).getTitle());
                text2.setText(options.get(position).getValue());

                return view;
            }
        };

        leftDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                options.get(i).toggle();
                drawerAdapter.notifyDataSetChanged();
            }
        });
        leftDrawer.setAdapter(drawerAdapter);
    }

    private static abstract class CameraSetting {

        abstract String getTitle();
        abstract String getValue();
        abstract void toggle();

    }

    private CameraSetting captureMethodSetting = new CameraSetting() {
        @Override
        String getTitle() {
            return "ckMethod";
        }

        @Override
        String getValue() {
            switch (cameraMethod) {
                case CameraKit.Constants.METHOD_STANDARD: {
                    return "standard";
                }

                case CameraKit.Constants.METHOD_STILL: {
                    return "still";
                }

                default: return null;
            }
        }

        @Override
        void toggle() {
            if (cameraMethod == CameraKit.Constants.METHOD_STANDARD) {
                cameraMethod = CameraKit.Constants.METHOD_STILL;
            } else {
                cameraMethod = CameraKit.Constants.METHOD_STANDARD;
            }

            camera.setMethod(cameraMethod);
        }
    };

    private CameraSetting cropSetting = new CameraSetting() {
        @Override
        String getTitle() {
            return "ckCropOutput";
        }

        @Override
        String getValue() {
            if (cropOutput) {
                return "true";
            } else {
                return "false";
            }
        }

        @Override
        void toggle() {
            cropOutput = !cropOutput;
            camera.setCropOutput(cropOutput);
        }
    };


}
