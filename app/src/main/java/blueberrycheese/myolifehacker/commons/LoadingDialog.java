package blueberrycheese.myolifehacker.commons;

import android.app.Activity;

import android.app.Application;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;


import blueberrycheese.myolifehacker.R;

public class LoadingDialog {

    public Dialog setProgress(Activity mActivity){
        Dialog mDialog = new Dialog(mActivity);
        mDialog.setCancelable(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading_dialog);
        mDialog.show();
        ImageView img_loading_frame = mDialog.findViewById(R.id.iv_loading_image);
        TextView tv = mDialog.findViewById(R.id.tv_progress_message);
        tv.setText("Loading\n멈추어도 계속 진행중이오니 기다려주시기 바랍니다.");
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(new Runnable() {
            @Override
            public void run() {
                frameAnimation.start();
            }
        });//apache commons math의 kmeans++사용시에 핸드폰이 thread전부를 사용하게 되면서 이미작 결국 멈추는 결과가 나타난다. 결국 돌아가는 이미지를 못한다.

        return mDialog;
    }

}
