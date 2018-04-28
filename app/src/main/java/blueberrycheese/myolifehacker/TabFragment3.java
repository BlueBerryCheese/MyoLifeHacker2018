package blueberrycheese.myolifehacker;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import blueberrycheese.myolifehacker.commons.LoadingDialog;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectSendResultAction;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
import blueberrycheese.myolifehacker.myo_manage.MyoDataFileReader;
import blueberrycheese.myolifehacker.myo_manage.MyoGattCallback;
import blueberrycheese.myolifehacker.myo_manage.NopModel;

import static android.content.Context.BLUETOOTH_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabFragment3.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabFragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment3 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String TAG = "TabFragment3";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final long SCAN_PERIOD = 5000;
    private static final int REQUEST_ENABLE_BT = 1;

    private final static String FileList_kmeans = "KMEANS_DATA.dat";
    private final static String FileList[] = {"Gesture1.txt","Gesture2.txt","Gesture3.txt","Gesture4.txt","Gesture5.txt","Gesture6.txt"}; //

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    private Activity mactivity;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private BluetoothDevice device;
    private OnFragmentInteractionListener mListener;
    String deviceName;

    private TextView emgDataText;
    private TextView gestureText;
    private TextView maxDataTextView;
    private NumberPicker gesturenNumberPicker;
    private NumberPicker remove_gesturenNumberPicker;
    private NumberPicker adapter_gesturenNumberPicker;
    private View views[]=new View[5];

    private GestureSaveModel saveModel;
    private GestureSaveMethod   saveMethod;
    private GestureDetectModel  detectModel;
    private GestureDetectMethod detectMethod;
    private Button btn_ready,btn_remove,btn_sync,btn_save;
    private View view;
    private int inds_num=0;
    private int inds_remove=0;
    private int inds_adapter=0;
    private double pass_adapter=0;

    private  Dialog dialog;
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public TabFragment3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment3.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment3 newInstance(String param1, String param2) {
        TabFragment3 fragment = new TabFragment3();
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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tab_fragment3, container, false);
        /*
        GraphView graph = (GraphView)view.findViewById(R.id.graph);
        //TODO:: 실시간 데이터 변화 만들기(나중에)
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {    //추후에 데이터가 실시간으로 어떻게 들어오는지를 보여줄려고해서 만들어봤는데 어려우면 걍 뒤엎을각오가 있습니다.
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
        */
        emgDataText = (TextView)view.findViewById(R.id.emgDataTextView);
        gestureText = (TextView)view.findViewById(R.id.gestureTextView);

        maxDataTextView=(TextView)view.findViewById(R.id.maxData);
        gesturenNumberPicker = (NumberPicker)view.findViewById(R.id.gestureNumberPicker);
        remove_gesturenNumberPicker = (NumberPicker)view.findViewById(R.id.remove_gestureNumberPicker); //
        adapter_gesturenNumberPicker = (NumberPicker)view.findViewById(R.id.AdapterNumberPicker); //
        //btn_ready = (Button)view.findViewById(R.id.btnReady);
        btn_remove = (Button)view.findViewById(R.id.btnRemove);
        btn_sync = (Button)view.findViewById(R.id.btn_Sync);
        btn_save = (Button)view.findViewById(R.id.btn_Save);
        views[0] = (View)view.findViewById(R.id.view1);
        views[1] = (View)view.findViewById(R.id.view2);
        views[2] = (View)view.findViewById(R.id.view3);
        views[3] = (View)view.findViewById(R.id.view4);
        views[4] = (View)view.findViewById(R.id.view5);

        //views[0].setBackgroundResource(R.drawable.imgbtn_pressed);
        mHandler = new Handler();
        BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        Log.d(TAG,deviceName+"--connected");
        //saveMethod = new GestureSaveMethod(0,view.getContext());
        saveMethod = new GestureSaveMethod();
        Log.d(TAG,"Value changes "+(inds_num+1)+" to "+(inds_num+1));
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
            gestureText.setText("\'Gesture"+(inds_num+1)+"\'"+"SAVE complete. Save more?");
        } else {
            gestureText.setText("Teach me \'Gesture"+(inds_num+1)+"\'");
        }

        /////////  파일 삭제하는 numberPicker 설정.
        remove_gesturenNumberPicker.setMinValue(0);
        remove_gesturenNumberPicker.setMaxValue(8);
        remove_gesturenNumberPicker.setDisplayedValues(new String[]{"Model","All","All_Gesture","Gesture 1","Gesture 2","Gesture 3","Gesture 4","Gesture 5","Gesture 6"});
        remove_gesturenNumberPicker.setWrapSelectorWheel(false);

        // 어댑터 설정하는 numberPicker 설정.
        adapter_gesturenNumberPicker.setMinValue(0);
        adapter_gesturenNumberPicker.setMaxValue(4);
        adapter_gesturenNumberPicker.setWrapSelectorWheel(false);
        adapter_gesturenNumberPicker.setDisplayedValues(new String[]{"100%","80%","60%","40%","20%"});

        ///////

        //현재 기본적으로 numberpicker는 0~5까지 하지만 번호변환으로 1~6으로 보이게 하였음
        //6까지 올리면 더이상올라가지 않게 함
        // 제스처 세이브하는 numberpicker 설정.
        gesturenNumberPicker.setMinValue(0);
        gesturenNumberPicker.setMaxValue(5);
        gesturenNumberPicker.setWrapSelectorWheel(false);;
        gesturenNumberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return Integer.toString(value+1);
            }
        });


        ////// 파일삭제  numberPicker 값 변동되면 변동되는 값 저장
        remove_gesturenNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                inds_remove=newVal;
            }
        });

        ////// 어댑터  numberPicker 값 변동되면 변동되는 값 저장
        adapter_gesturenNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                inds_adapter=newVal;
            }
        });
        /////

        ////// 제스처 세이브 numberPicker 값 변동되면 변동되는 값 저장
        gesturenNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                for(int i=0;i<views.length;i++){        // 동그라미 5개 빈동그라미로 초기화
                    views[i].setBackgroundResource(R.drawable.imgbtn_default);
                }
                inds_num = newVal;
                saveMethod = new GestureSaveMethod(inds_num,view.getContext(),1);   //세이브 실행
                Log.d(TAG,"Value changes "+(oldVal+1)+" to "+(newVal+1));
                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                    gestureText.setText("\'Gesture"+(newVal+1)+"\'"+"SAVE complete. Save more?");
                } else {
                    gestureText.setText("Teach me \'Gesture"+(newVal+1)+"\'");
                }
            }
        });
/*
        btn_ready.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){            //이게 왜인지모르겠는데 xml쪽에서 함수시행 바로붙이면 안되는 경향이 있어서 이렇게 setonclicklistener 에서  붙이는 형식으로 했음
                if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
                    Log.d(TAG,"False EMG");
                } else {
                    saveMethod  = new GestureSaveMethod(inds_num,v.getContext());
                    if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                        gestureText.setText("DETECT Ready");
                    } else {
                        gestureText.setText("Teach me \'Gesture\'");
                    }
                }
            }
        });
*/
        //삭제 버튼
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           /*     if (mBluetoothGatt == null
                        || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
                        || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())) {
                    Log.d(TAG,"False Data Stop");
                }
                */
                MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG,FileList_kmeans);
                dataFileReader.removeFile(inds_remove);     //removeFile 메소드 호출
            }
        });


        // 세이브 버튼
        btn_save.setOnClickListener(new View.OnClickListener() {
            //TODO: 적응모델 적용하기
            @Override
            public void onClick(View v) {
                //inds_num=0;
                // for(inds_num=0; inds_num<6; inds_num++) {
                inds_num=saveMethod.getSaveIndex();
                saveModel = new GestureSaveModel(saveMethod, inds_num);
                startSaveModel();
                saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);        // SaveState 저장중으로 변경
                // 제스처의 카운트가 0일 때
                if(saveMethod.getGestureCounter()==0) {     //위에 setValue로는 setOnValueChangedListener가 인식을 못해서 따로 빼줌.
                    gesturenNumberPicker.setValue(inds_num);        //제스처 세이브 numberPicker 값 설정.
                    for(int i=0;i<views.length;i++){        //동그라미 빈칸으로 바꿔줌
                        views[i].setBackgroundResource(R.drawable.imgbtn_default);
                    }
                }
                gestureText.setText("Gesture" + (inds_num + 1) + "'s Saving Count : " + (saveMethod.getGestureCounter() + 1));
                //  views[inds_num].setDrawingCacheBackgroundColor(Color.BLUE);
                // views[inds_num].setBackground(imgbtn_passed);
                views[saveMethod.getGestureCounter()].setBackgroundResource(R.drawable.imgbtn_pressed); // 동그라미 채워줌
                //   }
            }



        });

        //sync 버튼 눌렀을 때
        btn_sync.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                switch (inds_adapter) {
                    case 0:     // 100%
                        pass_adapter=1;
                        break;
                    case 1:     // 80%
                        pass_adapter=0.8;
                        break;
                    case 2:     // 60%
                        pass_adapter=0.6;
                        break;
                    case 3:     // 40%
                        pass_adapter=0.4;
                        break;
                    case 4:     // 20%
                        pass_adapter=0.2;
                        break;
                }


                Log.e(TAG,"pass_adapter: "+pass_adapter);
                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Ready ||
                        saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {

                    saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);
                    dialog= new LoadingDialog().setProgress(mactivity);
                    dialog.show();  // 로딩이미지 표현.


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(dialog.isShowing()) {
                                saveMethod = new GestureSaveMethod(inds_num, mactivity,pass_adapter);   // GestureSaveMethod로 (제스처 인덱스값, 메인액티?, 어댑터 값)넘겨줌
                                saveModel = new GestureSaveModel(saveMethod, inds_num);
                                startSaveModel();
                            }
                            dialog.dismiss();
                        }
                    },1000);
                } else if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Not_Saved) {
                    saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);
                    dialog= new LoadingDialog().setProgress(mactivity);
                    dialog.show();


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(dialog.isShowing()) {
                                saveMethod = new GestureSaveMethod(inds_num, mactivity,pass_adapter);
                                saveModel = new GestureSaveModel(saveMethod, inds_num);
                                startSaveModel();
                            }
                            dialog.dismiss();
                        }
                    },1000);

                    IGestureDetectModel model = saveModel;
                    model.setAction(new GestureDetectSendResultAction(mactivity,TabFragment3.this));
                    GestureDetectModelManager.setCurrentModel(model);
                    startSaveModel();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mactivity = getActivity();

    }
/*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
*/
    public void startSaveModel() {
        IGestureDetectModel model = saveModel;
        model.setAction(new GestureDetectSendResultAction(mactivity,TabFragment3.this));
        GestureDetectModelManager.setCurrentModel(model);
    }
/*
    public void startDetectModel(View v) {
        IGestureDetectModel model = detectModel;
        model.setAction(new GestureDetectSendResultAction(getActivity()));
        GestureDetectModelManager.setCurrentModel(model);
    }
*/
    public void setGestureText(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                gestureText.setText(message);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            mactivity = (Activity) context;
        }

//        if (context instanceof OnFragmentInteractionListener) {   //이함수써서 원래 데이터 전송인데 어렵다.
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }


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

    /*
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void testEvent(EventData event){
        Log.e("test_event", event.device.getName() + "connected !!");
        HashMap<String,View> views = new HashMap<String,View>();

        device = event.device;
        mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views,maxDataTextView,inds_num);  //이곳에서 문제가 일어나서 현재 이페이지에서밖에 시행이 안됨 inds_num(제스처 몇번을 저장할 것인가에 대한 내용이 담겨져 있음)
        mBluetoothGatt = device.connectGatt(getContext(), true, mMyoCallback);
        mMyoCallback.setBluetoothGatt(mBluetoothGatt);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
                    Log.d(TAG,"False EMG");
                } else {
                    saveMethod  = new GestureSaveMethod(inds_num, view.getContext(),1);
                    Log.d(TAG,"True EMG22");
                    if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                        gestureText.setText("DETECT Ready");
                    } else {
                        gestureText.setText("Teach me \'Gesture\'");
                    }
                }
            }
        },500);
    }
*/
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


    public void startNopModel() {
        GestureDetectModelManager.setCurrentModel(new NopModel());
    }
    //  public int getIndex_num() { return inds_num;}
   // public int getRemoveIndex() {
      //  return inds_remove;
  //  }  //
    // public double getAdapterIndex() {
   //   return pass_adapter;
  //   }  //
}