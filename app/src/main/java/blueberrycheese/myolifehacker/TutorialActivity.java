package blueberrycheese.myolifehacker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by LAB on 2018-05-17.
 */

public class TutorialActivity extends Activity {
    private static final String TAG = "TutorialActivity";
    private ImageButton btn_pointer;
    private ImageView imageGesture_view;
    private TextView imageGesture_textview;
    private TextView tutorialpage_textview;
    private Button tutorialpage_btn;
    private RelativeLayout.LayoutParams mLayoutParams;
    private int tutorial_cnt = 0;
    private static final int tutorial_page=8;
    private Context mcontext;
    private String normalText1,boldText,normalText2;
    private SpannableString str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        Typeface font = Typeface.createFromAsset(this.getAssets(), "NanumBarunGothic.ttf");
        btn_pointer = (ImageButton)findViewById(R.id.menu_pointer);
        imageGesture_view = (ImageView)findViewById(R.id.imageGesture_view);
        imageGesture_textview = (TextView)findViewById(R.id.imageGesture_texview);
        tutorialpage_textview = (TextView)findViewById(R.id.tutorialpage_textview);
        tutorialpage_btn = (Button)findViewById(R.id.tutorialpage_btn);
        mLayoutParams = (RelativeLayout.LayoutParams)tutorialpage_textview.getLayoutParams();
        mcontext = this;
        tutorial_cnt = 0;
        switchPage();

        tutorialpage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tutorial_cnt++;
                if(tutorial_cnt==tutorial_page)
                    finish();
                switchPage();
            }
        });

    }

    public void switchPage(){
        switch (tutorial_cnt%tutorial_page){
            case 0:
                mLayoutParams.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,mcontext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,30,mcontext.getResources().getDisplayMetrics()),0,0);
                String normalText1 = "좌측메뉴를 눌러서\n기계와";
                String boldText = " 블루투스";
                String normalText2 = "를 연결하세요!";
                imageGesture_view.setVisibility(View.INVISIBLE);
                btn_pointer.setVisibility(View.VISIBLE);
                imageGesture_textview.setVisibility(View.INVISIBLE);
                SpannableString str = new SpannableString(normalText1+ boldText + normalText2);
                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                tutorialpage_textview.setText(str);
                break;
            case 1:
                mLayoutParams.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,mcontext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,40,mcontext.getResources().getDisplayMetrics()),0,0);
                tutorialpage_textview.setText("블루투스 연결 후 ");
                normalText1 = "";
                boldText = "새끼손가락";
                normalText2 = "을 펴면\n잠금이 해제됩니다.";
                str = new SpannableString(normalText1+ boldText + normalText2);
                btn_pointer.setVisibility(View.INVISIBLE);
                imageGesture_view.setVisibility(View.VISIBLE);
                imageGesture_textview.setVisibility(View.VISIBLE);
                imageGesture_view.setImageResource(R.drawable.gesture_5_nb);
                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                imageGesture_textview.setText(str);
                break;
            case 2:
                tutorialpage_textview.setText("잠금 해제 후 ");
                normalText1 = "";
                boldText = "주먹";
                normalText2 = "을 쥐면\n시작됩니다.";
                str = new SpannableString(normalText1+ boldText + normalText2);
                btn_pointer.setVisibility(View.INVISIBLE);
                imageGesture_view.setVisibility(View.VISIBLE);
                imageGesture_textview.setVisibility(View.VISIBLE);
                imageGesture_view.setImageResource(R.drawable.gesture_1_nb);
                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                imageGesture_textview.setText(str);
                break;
            case 3:
                tutorialpage_textview.setText(" ");
                normalText1 = "손을 ";
                boldText = "안으로";
                normalText2 = "구부리면\n볼륨/밝기 조작을 시작합니다.";
                str = new SpannableString(normalText1+ boldText + normalText2);
                btn_pointer.setVisibility(View.INVISIBLE);
                imageGesture_view.setVisibility(View.VISIBLE);
                imageGesture_textview.setVisibility(View.VISIBLE);
                imageGesture_view.setImageResource(R.drawable.gesture_2_nb);
                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                imageGesture_textview.setText(str);
                break;
            case 4:
                tutorialpage_textview.setText(" ");
                normalText1 = "손을 ";
                boldText = "밖으로";
                normalText2 = "구부리면\n카메라를 시작합니다.";
                str = new SpannableString(normalText1+ boldText + normalText2);
                btn_pointer.setVisibility(View.INVISIBLE);
                imageGesture_view.setVisibility(View.VISIBLE);
                imageGesture_textview.setVisibility(View.VISIBLE);
                imageGesture_view.setImageResource(R.drawable.gesture_3_nb);
                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                imageGesture_textview.setText(str);
                break;
            case 5:
                tutorialpage_textview.setText(" ");
                normalText1 = "";
                boldText = "가위";
                normalText2 = "를 내면\n음악를 시작합니다.";
                str = new SpannableString(normalText1+ boldText + normalText2);
                btn_pointer.setVisibility(View.INVISIBLE);
                imageGesture_view.setVisibility(View.VISIBLE);
                imageGesture_textview.setVisibility(View.VISIBLE);
                imageGesture_view.setImageResource(R.drawable.gesture_6_nb);
                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                imageGesture_textview.setText(str);
                break;
            case 6:
                tutorialpage_textview.setText(" ");
                normalText1 = "";
                boldText = "보자기";
                normalText2 = "를 내면\n갤러리를 시작합니다.";
                str = new SpannableString(normalText1+ boldText + normalText2);
                btn_pointer.setVisibility(View.INVISIBLE);
                imageGesture_view.setVisibility(View.VISIBLE);
                imageGesture_textview.setVisibility(View.VISIBLE);
                imageGesture_view.setImageResource(R.drawable.gesture_4_nb);
                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                imageGesture_textview.setText(str);
                break;
            case 7:
                tutorialpage_textview.setText("각각의 기능들은\n기능마다 설명이 있습니다.");
                normalText1 = "만약 제스처가 인식이 안되면\n";
                boldText = "LEARNING";
                normalText2 = "페이지를 이용해주세요.";
                str = new SpannableString(normalText1+ boldText + normalText2);
                btn_pointer.setVisibility(View.INVISIBLE);
                imageGesture_view.setVisibility(View.INVISIBLE);
                imageGesture_textview.setVisibility(View.VISIBLE);

                str.setSpan(new StyleSpan(Typeface.BOLD), normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set bold
                str.setSpan(new ForegroundColorSpan(Color.RED),  normalText1.length(),normalText1.length()+ boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// set color
                imageGesture_textview.setText(str);
                tutorialpage_btn.setText("나가기");
                break;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    @Override
    public void onDestroy(){


        super.onDestroy();

    }

}
