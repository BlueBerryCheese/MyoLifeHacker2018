package blueberrycheese.myolifehacker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.L;

/**
 * Created by pjw12 on 2018-05-13.
 */

public class FontConfig {
    public static void setGlobalFont(Context context, View view){
        if (view != null) {
            if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                int len = vg.getChildCount();
                for (int i = 0; i < len; i++) {
                    View v = vg.getChildAt(i);
                    if (v instanceof TextView) {
                        ((TextView) v).setTypeface(Typeface.createFromAsset(
                                context.getAssets(), "BMYEONSUNG_ttf.ttf"));
                    }
                    setGlobalFont(context, v);
                }
            }
        } else {
            //L.("This is null);
        }

    }
}
