package blueberrycheese.myolifehacker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.MyoCommandList;
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

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private BluetoothDevice device;
    private OnFragmentInteractionListener mListener;
    String deviceName;

    private TextView emgDataText;
    private TextView gestureText;
    private TextView maxDataTextView;

    private GestureSaveModel saveModel;
    private GestureSaveMethod   saveMethod;
    private Button btn_ready;

    private int inds_num=0;
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
        View view = inflater.inflate(R.layout.fragment_tab_fragment3, container, false);
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
        emgDataText = (TextView)view.findViewById(R.id.emgDataTextView);
        gestureText = (TextView)view.findViewById(R.id.gestureTextView);

        maxDataTextView=(TextView)view.findViewById(R.id.maxData);
        btn_ready = (Button)view.findViewById(R.id.btnReady);
        mHandler = new Handler();
        BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        Log.d(TAG,deviceName+"--connected");

        btn_ready.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){            //이게 왜인지모르겠는데 xml쪽에서 함수시행 바로붙이면 안되는 경향이 있어서 이렇게 setonclicklistener 에서  붙이는 형식으로 했음
                if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
                    Log.d(TAG,"False EMG");
                } else {
                    saveMethod  = new GestureSaveMethod();
                    if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                        gestureText.setText("DETECT Ready");
                    } else {
                        gestureText.setText("Teach me \'Gesture\'");
                    }
                }
            }
        });

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void testEvent(EventData event){
        Log.e("test_event", event.device.getName() + "connected !!");
        HashMap<String,View> views = new HashMap<String,View>();

        device = event.device;
        mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views,maxDataTextView,inds_num);  //이곳에서 문제가 일어나서 현재 이페이지에서밖에 시행이 안됨 inds_num(제스처 몇번을 저장할 것인가에 대한 내용이 담겨져 있음)
        mBluetoothGatt = device.connectGatt(getContext(), false, mMyoCallback);
        mMyoCallback.setBluetoothGatt(mBluetoothGatt);
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



    public void startNopModel() {
        GestureDetectModelManager.setCurrentModel(new NopModel());
    }

}
