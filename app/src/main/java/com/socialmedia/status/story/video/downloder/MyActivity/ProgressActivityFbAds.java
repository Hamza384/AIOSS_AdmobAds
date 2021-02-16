package com.socialmedia.status.story.video.downloder.MyActivity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.socialmedia.status.story.video.downloder.MyBounceInterpolator;
import com.socialmedia.status.story.video.downloder.R;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaViewListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class ProgressActivityFbAds extends AppCompatActivity  {
    private static final String TAG = ProgressActivityFbAds.class.getSimpleName();
    private final Handler hdlr = new Handler();
    protected int seconds = 2;
    TextView iv_btnstart;
    TextView appName;
    Handler handler = new Handler();
    Context mContext;
    LinearLayout contentLL;
    private final Runnable runnable = new Runnable() {
        public void run() {
            long currentMilliseconds = System.currentTimeMillis();
            seconds--;
            if (seconds > 0) {
                handler.postAtTime(this, currentMilliseconds);
                handler.postDelayed(runnable, 500);
            } else {

                MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                    }
                });


                contentLL.setVisibility(View.GONE);
                handler.removeCallbacks(runnable);

            }
        }
    };
    String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    int PERMISSION_ALL = 303;
    RelativeLayout nativeRL;
    private ProgressBar pgsBar;
    private int i = 0;
    private TextView txtView, loadingMain;
    private @Nullable
    TextView nativeAdStatus;
    private @Nullable
    LinearLayout adChoicesContainer;
    private @Nullable
    NativeAdLayout nativeAdLayout;
    private @Nullable
    NativeAd nativeAd;
    private @Nullable
    AdOptionsView adOptionsView;
    private com.facebook.ads.MediaView nativeAdMedia;


    public static boolean hasPermissions(Context context, String... permissions) {
        if (!(Build.VERSION.SDK_INT < 23 || context == null || permissions == null)) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static MediaViewListener getMediaViewListener() {
        return new MediaViewListener() {
            @Override
            public void onVolumeChange(com.facebook.ads.MediaView mediaView, float volume) {
                Log.i(TAG, "MediaViewEvent: Volume " + volume);
            }

            @Override
            public void onPause(com.facebook.ads.MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: Paused");
            }

            @Override
            public void onPlay(com.facebook.ads.MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: Play");
            }

            @Override
            public void onFullscreenBackground(com.facebook.ads.MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: FullscreenBackground");
            }

            @Override
            public void onFullscreenForeground(com.facebook.ads.MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: FullscreenForeground");
            }

            @Override
            public void onExitFullscreen(com.facebook.ads.MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: ExitFullscreen");
            }

            @Override
            public void onEnterFullscreen(com.facebook.ads.MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: EnterFullscreen");
            }

            @Override
            public void onComplete(com.facebook.ads.MediaView mediaView) {
                Log.i(TAG, "MediaViewEvent: Completed");
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_fb);
        appName = findViewById(R.id.tvAppName);
        pgsBar = findViewById(R.id.pBar);
        iv_btnstart = findViewById(R.id.btnLetsStart);
        txtView = findViewById(R.id.tView);
        mContext = ProgressActivityFbAds.this;






        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        if (!hasPermissions(this, this.PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, this.PERMISSIONS, this.PERMISSION_ALL);
        }

        pgsBar.setVisibility(View.INVISIBLE);
        txtView.setVisibility(View.INVISIBLE);
        pgsBar.postDelayed(new Runnable() {
            public void run() {
                pgsBar.setVisibility(View.VISIBLE);
                txtView.setVisibility(View.VISIBLE);
                showLoading();
            }
        }, 5);
    }


    @Override
    protected void onStart() {
        super.onStart();


    }

    public void showLoading() {
        i = pgsBar.getProgress();
        new Thread(new Runnable() {
            public void run() {
                while (i < 100) {
                    i += 1;
                    // Update the progress bar and display the current value in text view
                    hdlr.post(new Runnable() {
                        public void run() {
                            pgsBar.setProgress(i);
                            pgsBar.getProgressDrawable()
                                    .setColorFilter(ContextCompat.getColor(getApplicationContext(),
                                            R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);

                            if (pgsBar.getProgress() == 100) {
                                iv_btnstart.setVisibility(View.VISIBLE);
                                pgsBar.setVisibility(View.GONE);
                                txtView.setVisibility(View.GONE);
                                didTapButton(iv_btnstart);

                                iv_btnstart.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(ProgressActivityFbAds.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                            }

                        }
                    });
                    try {
                        // Sleep for 100 milliseconds to show the progress slowly.
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }


    public void didTapButton(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.btn_animation_bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.4, 30);
        myAnim.setInterpolator(interpolator);


        iv_btnstart.startAnimation(myAnim);
        myAnim.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationStart(Animation arg0) {

            }


            public void onAnimationRepeat(Animation arg0) {

            }

            public void onAnimationEnd(Animation arg0) {


            }
        });
    }



}
