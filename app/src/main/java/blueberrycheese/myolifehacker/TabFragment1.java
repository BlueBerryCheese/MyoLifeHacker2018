package blueberrycheese.myolifehacker;

import android.animation.Animator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;


import com.imangazaliev.circlemenu.CircleMenu;
import com.imangazaliev.circlemenu.CircleMenuButton;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Grid;
import com.otaliastudios.cameraview.SessionType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import blueberrycheese.myolifehacker.CameraView.CameraActivity;
import blueberrycheese.myolifehacker.CameraView.CameraEvent;
import blueberrycheese.myolifehacker.ImageViewer.GalleryActivity;
import blueberrycheese.myolifehacker.MenuControl.GestureDetectMethod_Menu;
import blueberrycheese.myolifehacker.MenuControl.GestureDetectModel_Menu;
import blueberrycheese.myolifehacker.MenuControl.GestureDetectSendResultAction_Menu;
import blueberrycheese.myolifehacker.MenuControl.MenuEvent;
import blueberrycheese.myolifehacker.SystemControl.SystemControlActivity;
import blueberrycheese.myolifehacker.events.ServiceEvent;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import com.airbnb.lottie.LottieAnimationView;
import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabFragment1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "TabFragment1";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final long SCAN_PERIOD = 5000;

    private static final int VIBRATION_A = 1;
    private static final int VIBRATION_B = 2;
    private static final int VIBRATION_C = 3;
    private static final int ADDITIONAL_DELAY = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private CircleMenu circleMenu;
    private CircleMenuButton circleMenuButton_volume ;
    private CircleMenuButton circleMenuButton_camera;
    private CircleMenuButton circleMenuButton_music ;
    private CircleMenuButton circleMenuButton_gallery;
    private Button next_;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    private Activity mactivity;
    private BluetoothDevice device;
    private OnFragmentInteractionListener mListener;
    String deviceName;
    private Button btn_im,btn_im2,btn_im3;

    private GestureSaveModel saveModel;
    private GestureSaveMethod   saveMethod;
    private GestureDetectModel_Menu detectModel;
    private GestureDetectMethod_Menu detectMethod;



    int[] smoothcount = new int[6];
    private int gestureNum = -1;

    public TabFragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment1 newInstance(String param1, String param2) {
        TabFragment1 fragment = new TabFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        FontConfig.setGlobalFont(getActivity(),getActivity().getWindow().getDecorView());
    }
    public Button btn_hello;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_tab_fragment1, container, false);
        mHandler = new Handler();
        BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //Main CirecleMenu 관련
        circleMenu = (CircleMenu) view.findViewById(R.id.circleMenu);
        circleMenuButton_music = (CircleMenuButton)view.findViewById(R.id.music);
        circleMenuButton_volume = (CircleMenuButton)view.findViewById(R.id.volume);
        circleMenuButton_camera = (CircleMenuButton)view.findViewById(R.id.camera_button);
        circleMenuButton_gallery = (CircleMenuButton)view.findViewById(R.id.gallery_button);
//        circleMenu.setBackgroundColor(getResources().getColor(R.color.FontColor));
        btn_im = (Button)view.findViewById(R.id.btnin);
        btn_im2 = (Button)view.findViewById(R.id.btnin_2);
        btn_im3 = (Button)view.findViewById(R.id.btnin_3);


        final LottieAnimationView animationView = (LottieAnimationView) view.findViewById(R.id.lottie);
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
               //animationView.playAnimation();
                animationView.loop(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
               // animationView.setVisibility(view.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        btn_im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animationView.setAnimation("timer.json");

                animationView.cancelAnimation();
            }    });


        btn_im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.setVisibility(View.VISIBLE);
                animationView.playAnimation();
            }    });
        btn_im3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.setVisibility(View.GONE);
            }    });
        animationView.playAnimation();
        //animationView.setAnimation(View.INVISIBLE);
       // animationView.setVisibility(view.GONE);
       // animationView.setAnimation("timer.json");
       // animationView.loop(true);
        //animationView.setAnimation("cup_game_loader_2.json");

       // animationView.cancelAnimation();

        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();

        circleMenu.setOnItemClickListener(new CircleMenu.OnItemClickListener() {
            @Override
            public void onItemClick(CircleMenuButton menuButton) {
                int viewId = menuButton.getId(); // 클릭한 메뉴 id를 가져온 후 switch 문 으로
               // animationView.setVisibility(view.INVISIBLE);
                //animationView.setAnimation("cup_game_loader_2.json");
                //animationView.playAnimation();
               // animationView.loop(true);
               // animationView.cancelAnimation();

                switch(viewId){
                    case R.id.camera_button:
                        Log.d("cameracircle","cameraclicked");
                        Intent intent = new Intent(getActivity().getApplicationContext(), CameraActivity.class);
//                        intent.putExtra("bluetoothDevice", device);
                        startActivity(intent);
                        Toasty.success(getContext(), "Open camera", Toast.LENGTH_SHORT, false).show();
                       // animationView.cancelAnimation();
                       // animationView.setVisibility(View.GONE);
                        break;
                    case R.id.volume:
                        Log.d("volumecircle", "volume_clicked");

                        Intent intent2 = new Intent(getActivity().getApplicationContext(), SystemControlActivity.class);
                        intent2.putExtra("bluetoothDevice", device);
                        startActivity(intent2);
                        Toasty.success(getContext(), "Open interior function", Toast.LENGTH_SHORT, false).show();
                        //animationView.cancelAnimation();
                       // animationView.setVisibility(View.GONE);
                        break;
                    case R.id.music:
                        Log.d("music_circle","music_clicked");
                        Intent intent3 = new Intent(getActivity().getApplicationContext(), blueberrycheese.myolifehacker.myo_music.activities.activitys.MainActivity.class);
                        intent3.putExtra("bluetoothDevice", device);
                        startActivity(intent3);
                        Toasty.success(getContext(), "Open music", Toast.LENGTH_SHORT, false).show();
                       // animationView.cancelAnimation();
                       // animationView.setVisibility(View.GONE);
                        break;
                    case R.id.gallery_button:
                        Log.d("gallerycircle","galleryclicked");
                        Intent intent4 = new Intent(getActivity().getApplicationContext(), GalleryActivity.class);
                        intent4.putExtra("bluetoothDevice", device);
                        startActivity(intent4);
                        Toasty.success(getContext(), "Open gallery", Toast.LENGTH_SHORT, false).show();
                        //animationView.cancelAnimation();
                        //animationView.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });




        circleMenu.setEventListener(new CircleMenu.EventListener() {
            @Override
            public void onMenuOpenAnimationStart() {
                Log.d("CircleMenuStatus", "onMenuOpenAnimationStart");
            }

            @Override
            public void onMenuOpenAnimationEnd() {
                Log.d("CircleMenuStatus", "onMenuOpenAnimationEnd");
            }

            @Override
            public void onMenuCloseAnimationStart() {
                Log.d("CircleMenuStatus", "onMenuCloseAnimationStart");
            }

            @Override
            public void onMenuCloseAnimationEnd() {
                Log.d("CircleMenuStatus", "onMenuCloseAnimationEnd");
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuButton menuButton) {
                Log.d("CircleMenuStatus", "onButtonClickAnimationStart");
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuButton menuButton) {
                Log.d("CircleMenuStatus", "onButtonClickAnimationEnd");
            }

        });

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_tab_fragment1, container, false);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {

        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        try{
            EventBus.getDefault().unregister(this);         //이벤트버스는 시행되면 계속 그곳에서 이벤트가 발생하는데 문제가 일어날수있다 생각하여 멈추거나할때 이벤트를 꺼주는것을 해야함 아니면 베터리소모가 크답니다.
        }catch (Exception e){}
        super.onStop();
    }

    @Override
    public void onResume(){

        super.onResume();
        try {
            EventBus.getDefault().register(this);           //이벤트 버스 다시 키는 역활
        }catch (Exception e){}

    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void testEvent(EventData event){
//        if(event.device!=null){
//            Log.e("test_event", event.device.getName() + "connected !! Tab1");
//            HashMap<String,View> views = new HashMap<String,View>();
//
//            device = event.device;
////            android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
////            ft.detach(this).attach(this).commit();  //처음에 보내졌을당시에 refresh 한번시킴.
//            mMyoCallback = new MyoGattCallback(mHandler);
//            mBluetoothGatt = device.connectGatt(view.getContext(), false, mMyoCallback);
//            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
//                        Log.d(TAG,"False EMG");
//                    } else {
//                        saveMethod  = new GestureSaveMethod(-1, view.getContext(),1);
//                        Log.d(TAG,"True EMG");
//                        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//                            detectMethod = new GestureDetectMethod_Menu(mHandler, saveMethod.getCompareDataList());    //아예 새롭게 각각의 detectMethod를 구현하는것이 빠를것으로 예상된다.
//                            detectModel = new GestureDetectModel_Menu(detectMethod);
//                            startDetectModel();
//                        }
//
//                        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//                            //gestureText.setText("DETECT Ready");
//                        } else {
//                            //gestureText.setText("Teach me \'Gesture\'");
//                        }
//                    }
//                }
//            },SCAN_PERIOD);
//
//        }
//    }

    public void startDetectModel() {
        IGestureDetectModel model = detectModel;
        model.setAction(new GestureDetectSendResultAction_Menu(this));    //변경
        GestureDetectModelManager.setCurrentModel(model);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceEvent.GestureEvent event) {
        gestureNum = event.gestureNumber;
        Log.d("MenuEvent","MenuEvent Gesture num : "+event.gestureNumber);

        switch(gestureNum){
            case 0 :
                if(smoothcount[gestureNum]>1) {
                    circleMenu.onOpenAnimationStart();
                    circleMenu.toggle();
                    circleMenu.onOpenAnimationEnd();

                    //Send Vibration Event
                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_A));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    smoothcount[gestureNum]=-1;
                    resetSmoothCount();
                    Toasty.success(getContext(), "Open menu", Toast.LENGTH_SHORT, false).show();
                }
                smoothcount[gestureNum]++;

                break;

            case 1 :
                if(smoothcount[gestureNum]>1) {
                    circleMenu.onSelectAnimationStart(circleMenuButton_volume);
                    circleMenu.onSelectAnimationEnd(circleMenuButton_volume);

                    //Send Vibration Event

                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_A));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    resetSmoothCount();
                    smoothcount[gestureNum]=-1;
                    Toasty.success(getContext(), "Open interior function", Toast.LENGTH_SHORT, false).show();
                }
                smoothcount[gestureNum]++;
                break;

            case 2 :
                if(smoothcount[gestureNum]>1) {
                    circleMenu.onSelectAnimationStart(circleMenuButton_camera);
                    circleMenu.onSelectAnimationEnd(circleMenuButton_camera);

                    //Send Vibration Event

                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_A));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    resetSmoothCount();
                    smoothcount[gestureNum]=-1;

                    Toasty.success(getContext(), "Open camera", Toast.LENGTH_SHORT, false).show();
                }
                smoothcount[gestureNum]++;
                break;

            case 3 :
                if(smoothcount[gestureNum]>1) {
                    circleMenu.onSelectAnimationStart(circleMenuButton_gallery);
                    circleMenu.onSelectAnimationEnd(circleMenuButton_gallery);

                    //Send Vibration Event

                    EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_A));
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                    resetSmoothCount();
                    smoothcount[gestureNum]=-1;

                    Toasty.success(getContext(), "Open gallery", Toast.LENGTH_SHORT, false).show();
                }
                smoothcount[gestureNum]++;
                break;
            case 4 :
                if(smoothcount[gestureNum]>1) {
                circleMenu.onSelectAnimationStart(circleMenuButton_music);
                circleMenu.onSelectAnimationEnd(circleMenuButton_music);

                //Send Vibration Event

                EventBus.getDefault().post(new ServiceEvent.VibrateEvent(VIBRATION_A));
                //Restart lock Timer so user can use gesture continuously
                EventBus.getDefault().post(new ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY));

                resetSmoothCount();
                smoothcount[gestureNum]=-1;

                Toasty.success(getContext(), "Open music", Toast.LENGTH_SHORT, false).show();
            }
                smoothcount[gestureNum]++;
                break;
            default :
                break;

        }
        //Log.e("Hello",smoothcount[0]+" " + smoothcount[1]+" " + smoothcount[2]+" " + smoothcount[3]);
    }

    public void resetSmoothCount(){
        for(int i=0;i<smoothcount.length;i++){
            smoothcount[i]=0;
        }
    }
}
