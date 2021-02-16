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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.socialmedia.status.story.video.downloder.MyBounceInterpolator;
import com.socialmedia.status.story.video.downloder.R;

import java.util.Arrays;
import java.util.List;

public class ProgressActivityWithAdMobNativeAd extends AppCompatActivity {
    private static final String TAG = ProgressActivityWithAdMobNativeAd.class.getSimpleName();
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
    FrameLayout frameLayout;
    private ProgressBar pgsBar;
    private int i = 0;
    private UnifiedNativeAd nativeAd;
    private TextView txtView, loadingMain;


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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_admob);
        appName = findViewById(R.id.tvAppName);
        pgsBar = findViewById(R.id.pBar);
        iv_btnstart = findViewById(R.id.btnLetsStart);
        txtView = findViewById(R.id.tView);
        mContext = ProgressActivityWithAdMobNativeAd.this;
        loadingMain = findViewById(R.id.loadingMain);
        frameLayout = findViewById(R.id.fl_adplaceholder);


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
                refreshAd();
                showLoading();
            }
        }, 100);
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
                                            R.color.cld), PorterDuff.Mode.MULTIPLY);

                            if (pgsBar.getProgress() == 20) {
                                loadingMain.setVisibility(View.GONE);
                                frameLayout.setVisibility(View.VISIBLE);

                            } else if (pgsBar.getProgress() == 100) {

                                iv_btnstart.setVisibility(View.VISIBLE);
                                pgsBar.setVisibility(View.GONE);
                                txtView.setVisibility(View.GONE);
                                didTapButton(iv_btnstart);

                                iv_btnstart.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(ProgressActivityWithAdMobNativeAd.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                            }

                        }
                    });
                    try {
                        // Sleep for 100 milliseconds to show the progress slowly.
                        Thread.sleep(60);
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


    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.GONE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.VISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getVideoController();


    }

    private void refreshAd() {

        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.admob_native_id));

        List<String> testDeviceIds = Arrays.asList("5A2F32BB13FE27013C04395EDE71F6ED", "E63A0CE8FE5F8D4A468DFED3A175147F",
                "828E03F935B3E010C1192B52E2B7F3AC");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                        .inflate(R.layout.admob_ad_unified, null);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }

        });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d("CheckAd", "Ad Failed");
            }
        }).build();


        adLoader.loadAd(new AdRequest.Builder().build());

    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDestroy();
    }

}
