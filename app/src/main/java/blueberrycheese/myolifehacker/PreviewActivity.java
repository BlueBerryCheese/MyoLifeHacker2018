package blueberrycheese.myolifehacker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewActivity extends AppCompatActivity {

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.video)
    VideoView videoView;

    @BindView(R.id.actualResolution)
    TextView actualResolution;

    @BindView(R.id.approxUncompressedSize)
    TextView approxUncompressedSize;

    @BindView(R.id.captureLatency)
    TextView captureLatency;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);

        setupToolbar();

        byte[] jpeg = ResultHolder.getImage();
        File video = ResultHolder.getVideo();

        if (jpeg != null) {
            imageView.setVisibility(View.VISIBLE);

            Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

            if (bitmap == null) {
                finish();
                return;
            }

            imageView.setImageBitmap(bitmap);

            actualResolution.setText(bitmap.getWidth() + " x " + bitmap.getHeight());
            approxUncompressedSize.setText(getApproximateFileMegabytes(bitmap) + "MB");
            captureLatency.setText(ResultHolder.getTimeToCallback() + " milliseconds");
        }

        else if (video != null) {
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(video.getAbsolutePath()));
            MediaController mediaController = new MediaController(this);
            mediaController.setVisibility(View.GONE);
            videoView.setMediaController(mediaController);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mp.start();

                    float multiplier = (float) videoView.getWidth() / (float) mp.getVideoWidth();
                    videoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (mp.getVideoHeight() * multiplier)));
                }
            });
            //videoView.start();
        }

        else {
            finish();
            return;
        }
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            View toolbarView = getLayoutInflater().inflate(R.layout.camera_action_bar, null, false);
            TextView titleView = toolbarView.findViewById(R.id.toolbar_title);
            titleView.setText(Html.fromHtml("<b>Camera</b>"));

            getSupportActionBar().setCustomView(toolbarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024 / 1024;
    }

}

