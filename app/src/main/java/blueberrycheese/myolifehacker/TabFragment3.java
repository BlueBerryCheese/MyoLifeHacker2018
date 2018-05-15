package blueberrycheese.myolifehacker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.greenrobot.eventbus.EventBus;

import blueberrycheese.myolifehacker.events.ServiceEvent;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectModelManager;
import blueberrycheese.myolifehacker.myo_manage.GestureDetectSendResultAction;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveMethod;
import blueberrycheese.myolifehacker.myo_manage.GestureSaveModel;
import blueberrycheese.myolifehacker.myo_manage.IGestureDetectModel;
import blueberrycheese.myolifehacker.myo_manage.MyoDataFileReader;
import blueberrycheese.myolifehacker.myo_manage.NopModel;


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
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "TabFragment3";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final static String FileList_kmeans = "KMEANS_DATA.dat";
    private final static String FileList[] = {"Gesture1.txt","Gesture2.txt","Gesture3.txt","Gesture4.txt","Gesture5.txt","Gesture6.txt"};
    private static int[] remove_gesture_image = {R.drawable.gesture_model_nb,R.drawable.gesture_all_nb,R.drawable.gesture_allgesture_nb,R.drawable.gesture_1_nb,R.drawable.gesture_2_nb,R.drawable.gesture_3_nb,R.drawable.gesture_4_nb,R.drawable.gesture_5_nb,R.drawable.gesture_6_nb};
    private static int[] adapt_image = {R.drawable.percent_100_nb,R.drawable.percent_80_nb,R.drawable.percent_60_nb,R.drawable.percent_40_nb,R.drawable.percent_20_nb,R.drawable.percent_0_nb};
    private static int[] save_gesture_image = {R.drawable.gesture_1_nb,R.drawable.gesture_2_nb,R.drawable.gesture_3_nb,R.drawable.gesture_4_nb,R.drawable.gesture_5_nb,R.drawable.gesture_6_nb};


    private View view;
    private Button btn_saveScroll;
    private Button btn_syncScroll;
    private Button btn_removeScroll;
    private RelativeLayout showRelativelayout;
    private LinearLayout main_linearlayout;
    private Activity mactivity;
    String deviceName;

    private TextView textView_tutorial;
    private TextView gestureText;
    private TextView maxDataTextView;

    private View views[]=new View[5];

    private GestureSaveModel saveModel;
    private GestureSaveMethod saveMethod;
    private GestureDetectModel detectModel;
    private GestureDetectMethod detectMethod;
    private Button btn_ready,btn_remove,btn_sync,btn_save,btn_tutorial;
    private ImageView saveGesture_Image;
    private int inds_num=0;
    private int inds_remove=0;
    private int inds_adapter=0;
    private int inds_gesture_num =0;
    private double pass_adapter=0;
    private Dialog dialog;
    private LinearLayout topLinearLayout,AdaptiveNumberPickerLinearLayout,SelectLinearLayout;
    private ImageButton btn_backward;
    private ImageButton btn_forward;
    private ViewAnimator viewAnimator;
    private ImageButton btn_backward_adapt;
    private ImageButton btn_forward_adapt;
    private ViewAnimator viewAnimator_adapt;
    private ImageButton btn_backward_save;
    private ImageButton btn_forward_save;
    private ViewAnimator viewAnimator_save;

    private Handler mHandler;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    //0:NONE,1:TOP_OPENED,2:MIDDLE_OPENED,3:BOTTOM_OPENED
    private int state = 0;


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
        FontConfig.setGlobalFont(getActivity(),getActivity().getWindow().getDecorView());  // 폰트 설정.
    }

/*
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view_font=inflater.inflate(R.layout.fragment_tab_fragment3, parent,false);
        FontConfig.setGlobalFont(getActivity(), view);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }
*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tab_fragment3, container, false);
        main_linearlayout = view.findViewById(R.id.main_linearlayout);
        btn_saveScroll = view.findViewById(R.id.btn_saveScroll);
        btn_syncScroll = view.findViewById(R.id.btn_syncScroll);
        btn_removeScroll = view.findViewById(R.id.btn_removeScroll);
        showRelativelayout = view.findViewById(R.id.showRelativelayout);
        //
        btn_forward = (ImageButton)view.findViewById(R.id.btn_forward);
        btn_backward = (ImageButton)view.findViewById(R.id.btn_back);
        viewAnimator = (ViewAnimator)view.findViewById(R.id.viewanimator);
        btn_forward_adapt = (ImageButton)view.findViewById(R.id.btn_forward_adapt);
        btn_backward_adapt = (ImageButton)view.findViewById(R.id.btn_back_adapt);
        viewAnimator_adapt = (ViewAnimator)view.findViewById(R.id.viewanimator_adapt);
        btn_forward_save = (ImageButton)view.findViewById(R.id.btn_forward_save);
        btn_backward_save = (ImageButton)view.findViewById(R.id.btn_back_save);
        viewAnimator_save = (ViewAnimator)view.findViewById(R.id.viewanimator_save);
        //
        //gesturenNumberPicker = (NumberPicker)view.findViewById(R.id.gestureNumberPicker);
        //remove_gesturenNumberPicker = (NumberPicker)view.findViewById(R.id.remove_gestureNumberPicker); //
        //adapter_gesturenNumberPicker = (NumberPicker)view.findViewById(R.id.AdapterNumberPicker); //
        textView_tutorial = (TextView)view.findViewById(R.id.textView_tutorial);
        btn_remove = (Button)view.findViewById(R.id.btnRemove);
        btn_sync = (Button)view.findViewById(R.id.btn_Sync);
        btn_save = (Button)view.findViewById(R.id.btn_Save);
        btn_tutorial=(Button)view.findViewById(R.id.btnTutorial);
        gestureText = (TextView)view.findViewById(R.id.gestureTextView);
        views[0] = (View)view.findViewById(R.id.view1);
        views[1] = (View)view.findViewById(R.id.view2);
        views[2] = (View)view.findViewById(R.id.view3);
        views[3] = (View)view.findViewById(R.id.view4);
        views[4] = (View)view.findViewById(R.id.view5);
        //saveGesture_Image  = (ImageView)view.findViewById(R.id.saveGesture_Image);
        topLinearLayout = (LinearLayout)view.findViewById(R.id.topLinearLayout);
        AdaptiveNumberPickerLinearLayout = (LinearLayout)view.findViewById(R.id.AdaptiveNumberPickerLinearLayout);
        SelectLinearLayout= (LinearLayout)view.findViewById(R.id.SelectLinearLayout);
        //saveMethod = new GestureSaveMethod(0,view.getContext());
        saveMethod = new GestureSaveMethod();
        mHandler = new Handler();

        // Below 4 lines are for preventing IndexOutOfBoundsException Error
//        remove_gesturenNumberPicker.setSaveFromParentEnabled(false);
//        remove_gesturenNumberPicker.setSaveEnabled(true);
//        adapter_gesturenNumberPicker.setSaveFromParentEnabled(false);
//        adapter_gesturenNumberPicker.setSaveEnabled(true);


        // 파일 삭제하는 numberPicker 설정.
//        remove_gesturenNumberPicker.setMinValue(0);
//        remove_gesturenNumberPicker.setMaxValue(8);
//        remove_gesturenNumberPicker.setDisplayedValues(new String[]{"Model","All","All_Gesture","Gesture 1","Gesture 2","Gesture 3","Gesture 4","Gesture 5","Gesture 6"});
//        remove_gesturenNumberPicker.setWrapSelectorWheel(false);

        // 어댑터 설정하는 numberPicker 설정.
//        adapter_gesturenNumberPicker.setMinValue(0);
//        adapter_gesturenNumberPicker.setMaxValue(4);
//        adapter_gesturenNumberPicker.setWrapSelectorWheel(false);
//        adapter_gesturenNumberPicker.setDisplayedValues(new String[]{"100%","80%","60%","40%","20%"});

        //텍스트뷰 폰트설정
        ///////
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "NanumBarunGothic.ttf");
        textView_tutorial.setTypeface(font);
        gestureText.setTypeface(font);

        String boldText = "\n\n제스처 러닝";
        String normalText = "\n\n제스처가 잘 인식될 수 있도록\n 학습하는 메뉴입니다.";
        SpannableString str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
        str.setSpan(new RelativeSizeSpan(1.2f), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set size
        str.setSpan(new ForegroundColorSpan(Color.BLACK), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
        textView_tutorial.setText(str);
        //애니메이션
        ////////////////


        ///////

        //첫화면 안내문(다시보지 않기)
        /////////////////
/*
        LayoutInflater first_tuto = LayoutInflater.from(mactivity);
        View notshowaginLayout = first_tuto.inflate(R.layout.fragment3_noshow, null);
        final CheckBox dontShowAgain = (CheckBox)eulaLayout.findViewById(R.id.noshow);
        new AlertDialog.Builder(mactivity)
                .setMessage("환영합니다")
                .setView(notshowaginLayout)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

*/



        //현재 기본적으로 numberpicker는 0~5까지 하지만 번호변환으로 1~6으로 보이게 하였음
        //6까지 올리면 더이상올라가지 않게 함
        // 제스처 세이브하는 numberpicker 설정.
//        gesturenNumberPicker.setMinValue(0);
//        gesturenNumberPicker.setMaxValue(5);
//        gesturenNumberPicker.setWrapSelectorWheel(false);;
//        gesturenNumberPicker.setFormatter(new NumberPicker.Formatter() {
//            @Override
//            public String format(int value) {
//                return Integer.toString(value+1);
//            }
//        });


        ////// 파일삭제  numberPicker 값 변동되면 변동되는 값 저장
//        remove_gesturenNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                inds_remove=newVal;
//            }
//        });

        for (int i = 0; i < remove_gesture_image.length; i++) {
            ImageView imageView = new ImageView(getContext()); // create a new object  for ImageView
            imageView.setImageResource(remove_gesture_image[i]); // set image resource for ImageView
            viewAnimator.addView(imageView); // add child view in ViewAnimator
        }
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        viewAnimator.setInAnimation(in);
        viewAnimator.setOutAnimation(out);
        viewAnimator.setAnimateFirstView(true);

        for (int i = 0; i < adapt_image.length; i++) {
            ImageView imageView = new ImageView(getContext()); // create a new object  for ImageView
            imageView.setImageResource(adapt_image[i]); // set image resource for ImageView
            viewAnimator_adapt.addView(imageView); // add child view in ViewAnimator
        }
        viewAnimator_adapt.setInAnimation(in);
        viewAnimator_adapt.setOutAnimation(out);
        viewAnimator_adapt.setAnimateFirstView(true);

        for (int i = 0; i < save_gesture_image.length; i++) {
            ImageView imageView = new ImageView(getContext()); // create a new object  for ImageView
            imageView.setImageResource(save_gesture_image[i]); // set image resource for ImageView
            viewAnimator_save.addView(imageView); // add child view in ViewAnimator
        }
        viewAnimator_save.setInAnimation(in);
        viewAnimator_save.setOutAnimation(out);
        viewAnimator_save.setAnimateFirstView(true);
        ////// 어댑터  numberPicker 값 변동되면 변동되는 값 저장
//        adapter_gesturenNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                inds_adapter=newVal;
//            }
//        });
        btn_backward.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // show the next view of ViewAnimator `     `
                viewAnimator.showPrevious();
                inds_remove=viewAnimator.getDisplayedChild();
            }
        });
        btn_forward.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // show the next view of ViewAnimator `     `
                viewAnimator.showNext();
                inds_remove=viewAnimator.getDisplayedChild();
               // Log.d("nextbtn","click~ "+inds_remove);
            }
        });
        btn_backward_adapt.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // show the next view of ViewAnimator `     `
                viewAnimator_adapt.showPrevious();
                inds_adapter=viewAnimator_adapt.getDisplayedChild();
            }
        });
        btn_forward_adapt.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // show the next view of ViewAnimator `     `
                viewAnimator_adapt.showNext();
                inds_adapter=viewAnimator_adapt.getDisplayedChild();
            }
        });
        btn_backward_save.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // show the next view of ViewAnimator `     `
                viewAnimator_save.showPrevious();
                inds_num=viewAnimator_save.getDisplayedChild();
                for(int i=0;i<views.length;i++){        // 동그라미 5개 빈동그라미로 초기화
                    views[i].setBackgroundResource(R.drawable.imgbtn_default);
                }
                saveMethod.change_save_numer_numberPicker(inds_num);  // 제스처 넘버 값 저장
                saveModel = new GestureSaveModel(saveMethod, inds_num);
            }
        });
        btn_forward_save.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                // show the next view of ViewAnimator `     `
                viewAnimator_save.showNext();
                inds_num=viewAnimator_save.getDisplayedChild();
                for(int i=0;i<views.length;i++){        // 동그라미 5개 빈동그라미로 초기화
                    views[i].setBackgroundResource(R.drawable.imgbtn_default);
                }
                saveMethod.change_save_numer_numberPicker(inds_num);  // 제스처 넘버 값 저장
                saveModel = new GestureSaveModel(saveMethod, inds_num);
            }
        });
        /////


        //삭제 버튼
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bundle args  = new Bundle();
                AlertDialog.Builder alertBuiler = new AlertDialog.Builder(v.getContext());
                final Context ncontext = v.getContext();
                // AlertDialog.Builder builder = new AlertDialog.Builder(this);
                alertBuiler.setTitle("파일을 삭제하시겠습니까?");
                alertBuiler.setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MyoDataFileReader dataFileReader = new MyoDataFileReader(TAG,FileList_kmeans);
                                dataFileReader.removeFile(inds_remove);     //removeFile 메소드 호출
                                saveMethod.setState(GestureSaveMethod.SaveState.Not_Saved);
                                Toasty.success(ncontext, "Delete succes", Toast.LENGTH_SHORT, true).show();
                            }
                        }
                );
                alertBuiler.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = alertBuiler.create();
                dialog.show();

            }
        });
        btn_tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuiler2 = new AlertDialog.Builder(v.getContext());
                AlertDialog.Builder alertBuiler3 = new AlertDialog.Builder(v.getContext());
                alertBuiler2.setTitle("File Delete / Tutorial");
                alertBuiler2.setMessage("생성한 모델 또는 각각의 제스처들의 데이터들을 삭제합니다. \n 삭제하고 싶은 항목을 선택하고 delete를 눌러주세요");
                alertBuiler3.setTitle("Data Percent");
                alertBuiler3.setMessage("제스처 인식이 본인에게 좀 더 잘 되게 하도록 하는 Adaptation 부분 입니다.\n 각각의 제스처들을 저장한 데이터들을 합쳐서 만들 때 기존 데이터들의 비율을 정합니다.");
                AlertDialog dialog2 = alertBuiler3.create();
                alertBuiler2.setPositiveButton("다음", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = alertBuiler2.create();
                dialog.show();

            }    });
        // 세이브 버튼
        btn_save.setOnClickListener(new View.OnClickListener() {
            //TODO: 적응모델 적용하기
            @Override
            public void onClick(View v) {
                //inds_num=0;
                // for(inds_num=0; inds_num<6; inds_num++) {
                final Context ncontext = v.getContext();
                inds_num=saveMethod.getSaveIndex();  // 현재 몇번 제스처인지 값 가져옴.
                saveModel = new GestureSaveModel(saveMethod, inds_num);  // (saveMethod, 몇번제스처 인지 값 넘겨줌)
                startSaveModel();  // 세이브 시작
                saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);        // SaveState 저장중으로 변경
                // 제스처의 카운트가 0일 때
                if(saveMethod.getGestureCounter()==4) {// 동그라미 5개일때 제스처이미지 변경
                    viewAnimator_save.showNext();
                    inds_num=viewAnimator_save.getDisplayedChild();
                    saveModel = new GestureSaveModel(saveMethod, inds_num);
                    Toasty.info(ncontext, "Gesture "+(inds_num)+" save complete", Toast.LENGTH_SHORT, true).show();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for(int i=0;i<views.length;i++){        //동그라미 빈칸으로 바꿔줌
                                views[i].setBackgroundResource(R.drawable.imgbtn_default);
                            }
                        }
                    },300);
                    if(inds_num==5) {

                    }
                    else {

                    }
                }


                gestureText.setText("Gesture" + (inds_num + 1) + "'s Saving Count : " + (saveMethod.getGestureCounter() + 1)); // 아래쪽 텍스트 변경

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
                    case 5:
                        pass_adapter=0.0;
                        break;
                }

                final Context ncontext = v.getContext();
                Log.e(TAG,"pass_adapter: "+pass_adapter);
                if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Ready ||
                        saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {

                    //saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);
                    //dialog= new LoadingDialog().setProgress(mactivity);
                    //ProgressDialog dialog = ProgressDialog.show(DialogSam)
                    dialog= ProgressDialog.show(getContext(), "","Loading, Please Wait..",true,true);
                    dialog.show();  // 로딩이미지 표현.


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(dialog.isShowing()) {
                                saveMethod = new GestureSaveMethod(inds_num, mactivity,pass_adapter);   // GestureSaveMethod로 (제스처 인덱스값, 메인액티?, 어댑터 값)넘겨줌
                                saveModel = new GestureSaveModel(saveMethod, inds_num);
                                //startSaveModel();
                            }
                            dialog.dismiss();
                        }
                    },1000);

                    Toasty.error(ncontext, "Please delete model first", Toast.LENGTH_LONG,true).show();
                } else if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Not_Saved || saveMethod.getSaveState() == GestureSaveMethod.SaveState.Now_Saving) {
                    saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);
                    // dialog= new LoadingDialog().setProgress(mactivity);
                    dialog=ProgressDialog.show(getContext(), "","Loading, Please Wait..",true,true);
                    dialog.show();


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(dialog.isShowing()) {
                                saveMethod = new GestureSaveMethod(inds_num, mactivity,pass_adapter);
                                saveModel = new GestureSaveModel(saveMethod, inds_num);
                                //startSaveModel();
                            }
                            dialog.dismiss();
                        }
                    },2000);
                    saveMethod.setState(GestureSaveMethod.SaveState.Have_Saved);
                    IGestureDetectModel model = saveModel;
                    //  model.setAction(new GestureDetectSendResultAction(mactivity,TabFragment3.this));
                    GestureDetectModelManager.setCurrentModel(model);

                    //   startSaveModel();
                    textView_tutorial.setText("제스처가 준비되었습니다. \n원하는 기능을 사용하는 데 제스처를 사용할 수 있습니다.");
                    Toasty.info(ncontext,  "Model creation complete", Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        btn_saveScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //0:NONE,1:TOP_OPENED,2:MIDDLE_OPENED,3:BOTTOM_OPENED
                Animation animation;
                Animation animation1;
                String boldText = "잘 인식되지 않는 제스처를 선택";
                String normalText = "하여";
                String boldText2 = "\n제스처를 반복적으로 취한 후\nLearn버튼";
                String normalText2 = "을 누르세요.";
                SpannableString str = new SpannableString(boldText + normalText+boldText2+normalText2);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                //str.setSpan(new ForegroundColorSpan(Color.BLACK), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                str.setSpan(new StyleSpan(Typeface.BOLD), boldText.length()+normalText.length(), boldText.length()+normalText.length()+boldText2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                //str.setSpan(new ForegroundColorSpan(Color.BLACK), boldText.length()+normalText.length(), boldText.length()+normalText.length()+boldText2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                textView_tutorial.setText(str);

                switch (state) {
                    case 0:
                        //Additional text for tutorial
                        //각각의 제스처에 대해 자신의 데이터를 저장합니다.
                        //저장한 데이터는 SYNC DATA 수행을 위해 사용됩니다.
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.save_riseup1);
                        btn_saveScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.layout_dropdown1);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    case 1:
                        break;
                    case 2:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.dropdown_button);
                        btn_syncScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.riseup2);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    case 3:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.dropdown_button);
                        btn_syncScroll.startAnimation(animation);
                        btn_removeScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.riseup3);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    default:
                        break;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gestureText.setVisibility(View.VISIBLE);
                        btn_save.setVisibility(View.VISIBLE);
                        topLinearLayout.setVisibility(View.GONE);
                        AdaptiveNumberPickerLinearLayout.setVisibility(View.GONE);
                        SelectLinearLayout.setVisibility(View.VISIBLE);
                        main_linearlayout.removeView(showRelativelayout);
                        main_linearlayout.addView(showRelativelayout,1);


                    }
                },1010);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        main_linearlayout.removeView(showRelativelayout);
                        main_linearlayout.addView(showRelativelayout,1);


                    }
                },1020);
                state=1;

            }
        });
        btn_syncScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //0:NONE,1:TOP_OPENED,2:MIDDLE_OPENED,3:BOTTOM_OPENED
                Animation animation;
                Animation animation1;
                String boldText = "Adapt 시 사용될 기존 제스처 비율(%)을 선택 ";
                String normalText = "후\nAdapt 버튼을 누르세요.";

                SpannableString str = new SpannableString(boldText + normalText);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                //str.setSpan(new ForegroundColorSpan(Color.BLACK), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                textView_tutorial.setText(str);
                //Additional text for tutorial
                //자신이 저장한 제스처 데이터와 기존의 데이터를 이용하여 제스처 인식을 위한 새로운 기준을 생성합니다.
                switch (state) {
                    case 0:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.save_riseup1);
                        btn_saveScroll.startAnimation(animation);
                        btn_syncScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.layout_dropdown2);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    case 1:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.save_riseup1);
                        btn_syncScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.layout_dropdown1);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    case 2:
                        break;
                    case 3:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.dropdown_button);
                        btn_removeScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.riseup2);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    default:
                        break;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gestureText.setVisibility(View.GONE);
                        btn_save.setVisibility(View.GONE);
                        topLinearLayout.setVisibility(View.GONE);
                        AdaptiveNumberPickerLinearLayout.setVisibility(View.VISIBLE);
                        SelectLinearLayout.setVisibility(View.GONE);
                        main_linearlayout.removeView(showRelativelayout);
                        main_linearlayout.addView(showRelativelayout,2);
                    }
                },1010);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        main_linearlayout.removeView(showRelativelayout);
                        main_linearlayout.addView(showRelativelayout,2);
                    }
                },1020);
                state=2;
            }
        });
        btn_removeScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //0:NONE,1:TOP_OPENED,2:MIDDLE_OPENED,3:BOTTOM_OPENED
                Animation animation;
                Animation animation1;
                String boldText = "삭제하고 싶은 제스처를 선택";
                String normalText = "한 후\nReset버튼을 누르세요.";

                SpannableString str = new SpannableString(boldText + normalText);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                //str.setSpan(new ForegroundColorSpan(Color.BLACK), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                textView_tutorial.setText(str);
                //Additional text for tutorial
                //생성한 기준 또는 자신이 저장한 제스처 데이터들을 삭제합니다.
                //*All 선택 시 기준과 저장된 제스처 데이터 모두 삭제됩니다!
                switch (state) {
                    case 0:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.save_riseup1);
                        btn_saveScroll.startAnimation(animation);
                        btn_syncScroll.startAnimation(animation);
                        btn_removeScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.layout_dropdown3);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    case 1:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.save_riseup1);
                        btn_syncScroll.startAnimation(animation);
                        btn_removeScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.layout_dropdown2);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    case 2:
                        animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.save_riseup1);
                        btn_removeScroll.startAnimation(animation);
                        animation1 = AnimationUtils.loadAnimation(view.getContext(),R.anim.layout_dropdown1);
                        showRelativelayout.startAnimation(animation1);
                        break;
                    case 3:

                        break;
                    default:
                        break;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gestureText.setVisibility(View.GONE);
                        btn_save.setVisibility(View.GONE);
                        topLinearLayout.setVisibility(View.VISIBLE);
                        AdaptiveNumberPickerLinearLayout.setVisibility(View.GONE);
                        SelectLinearLayout.setVisibility(View.GONE);
                        main_linearlayout.removeView(showRelativelayout);
                        main_linearlayout.addView(showRelativelayout,3);
                    }
                },1010);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        main_linearlayout.removeView(showRelativelayout);
                        main_linearlayout.addView(showRelativelayout,3);
                    }
                },1020);
                state=3;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mactivity = getActivity();

    }
    public void startSaveModel() {
        IGestureDetectModel model = saveModel;
        model.setAction(new GestureDetectSendResultAction(mactivity,TabFragment3.this));
        GestureDetectModelManager.setCurrentModel(model);
    }

    public void gestureSaveImageChange(int i) {
        switch (i) {
            case 0:     // fist
                //saveGesture_Image.setImageResource(R.drawable.gesture_fist);
                saveGesture_Image.setImageResource(R.drawable.gesture_1_nb);
                break;
            case 1:     // wavein
                //saveGesture_Image.setImageResource(R.drawable.gesture_wavein);
                saveGesture_Image.setImageResource(R.drawable.gesture_2_nb);
                break;
            case 2:     // waveout
                //saveGesture_Image.setImageResource(R.drawable.gesture_waveout);
                saveGesture_Image.setImageResource(R.drawable.gesture_3_nb);
                break;
            case 3:     // spread
                //saveGesture_Image.setImageResource(R.drawable.gesture_spread);
                saveGesture_Image.setImageResource(R.drawable.gesture_4_nb);
                break;
            case 4:     // 새끼손가락
                saveGesture_Image.setImageResource(R.drawable.gesture_5_nb);
                break;
            case 5:     // 가위
                saveGesture_Image.setImageResource(R.drawable.gesture_6_nb);
                break;
        }

    }
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
    }

    @Override
    public void onDetach() {
        startDetectModel_Event();
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        //Fragment1으로 가거나 HomeButton 눌러서 앱 나간경우 onStop.
        try{
            startDetectModel_Event();
            EventBus.getDefault().unregister(this);         //이벤트버스는 시행되면 계속 그곳에서 이벤트가 발생하는데 문제가 일어날수있다 생각하여 멈추거나할때 이벤트를 꺼주는것을 해야함 아니면 베터리소모가 크답니다.
        }catch (Exception e){}
        super.onStop();
    }


    @Override
    public void onResume(){
        //Fragment 2 선택했을때부터 onResume 됨 + Fragment3오거나, Home나갔었다가 다시 돌아오거나 등..
        super.onResume();
        try {
            EventBus.getDefault().register(this);           //이벤트 버스 다시 키는 역활
        }catch (Exception e){}
    }


    public void startDetectModel_Event(){
        EventBus.getDefault().post(new ServiceEvent.setDetectModel_Event(1));
    }
    public void startNopModel() {
        GestureDetectModelManager.setCurrentModel(new NopModel());
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

}
