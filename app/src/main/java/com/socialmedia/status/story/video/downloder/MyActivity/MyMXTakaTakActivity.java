package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.MyApi.CommonClassForAPI;
import com.socialmedia.status.story.video.downloder.databinding.LayoutGlobalUiBinding;
import com.socialmedia.status.story.video.downloder.MyModel.MyTiktokModel;
import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.MySharePrefs;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.facebook.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import io.reactivex.observers.DisposableObserver;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.ROOTDIRECTORYMX;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyMXTakaTakActivity extends AppCompatActivity {
    MyMXTakaTakActivity myMXTakaTakActivity;
    CommonClassForAPI commonClassForAPI;
    AppLangSessionManager appLangSessionManager;
    private LayoutGlobalUiBinding layoutGlobalUiBinding;
    private String VideoUrl;
    private ClipboardManager clipboardManager;
    private InterstitialAd fbInterstitialAd;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;
    private final DisposableObserver<MyTiktokModel> mxObserver = new DisposableObserver<MyTiktokModel>() {
        @Override
        public void onNext(MyTiktokModel myTiktokModel) {
            Utils.hideProgressDialog(myMXTakaTakActivity);
            try {
                if (myTiktokModel.getResponsecode().equals("200")) {
                    startDownload(myTiktokModel.getData().getMainvideo(),
                            ROOTDIRECTORYMX, myMXTakaTakActivity, "MX_" + System.currentTimeMillis() + ".mp4");
                    layoutGlobalUiBinding.etText.setText("");
                    showInterstitial();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable e) {
            Utils.hideProgressDialog(myMXTakaTakActivity);
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            Utils.hideProgressDialog(myMXTakaTakActivity);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myMXTakaTakActivity = this;

        mContext = MyMXTakaTakActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My MX Taka Tak Activity");

        appLangSessionManager = new AppLangSessionManager(myMXTakaTakActivity);
        setLocale(appLangSessionManager.getLanguage());
        commonClassForAPI = CommonClassForAPI.getInstance(myMXTakaTakActivity);
        createFileFolder();
        initViews();

        /*AdsUtils.showFBBannerAd(myMXTakaTakActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/

        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.mxtakatak));
        layoutGlobalUiBinding.imAppIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cld));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.mxtakatak_app_name));


    }

    @Override
    protected void onResume() {
        super.onResume();
        myMXTakaTakActivity = this;
        assert myMXTakaTakActivity != null;
        clipboardManager = (ClipboardManager) myMXTakaTakActivity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myMXTakaTakActivity.getSystemService(CLIPBOARD_SERVICE);

        layoutGlobalUiBinding.imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        layoutGlobalUiBinding.imInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
            }
        });

        Glide.with(myMXTakaTakActivity)
                .load(R.drawable.tt1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myMXTakaTakActivity)
                .load(R.drawable.tt2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myMXTakaTakActivity)
                .load(R.drawable.tt3)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myMXTakaTakActivity)
                .load(R.drawable.tt4)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);


        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.LLHowToOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadTwo.setText(getResources().getString(R.string.how_to_download));

        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_mx));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.cop_link_from_mx));
        if (!MySharePrefs.getInstance(myMXTakaTakActivity).getBoolean(MySharePrefs.ISSHOWHOWTOTT)) {
            MySharePrefs.getInstance(myMXTakaTakActivity).putBoolean(MySharePrefs.ISSHOWHOWTOTT, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            pasteText();
        });

        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString().trim();
            if (LL.equals("")) {
                Utils.setToast(myMXTakaTakActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myMXTakaTakActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                GetMXTakaTakData();
            }
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Intent launchIntent = myMXTakaTakActivity.getPackageManager().getLaunchIntentForPackage("com.next.innovation.takatak");
            Intent launchIntent1 = myMXTakaTakActivity.getPackageManager().getLaunchIntentForPackage("com.next.innovation.takatak");
            if (launchIntent != null) {
                myMXTakaTakActivity.startActivity(launchIntent);
            } else if (launchIntent1 != null) {
                myMXTakaTakActivity.startActivity(launchIntent1);
            } else {
                Utils.setToast(myMXTakaTakActivity, getResources().getString(R.string.app_not_available));
            }

        });
    }

    private void GetMXTakaTakData() {
        try {
            createFileFolder();
            String host = layoutGlobalUiBinding.etText.getText().toString().trim();
            if (host.contains("mxtakatak")) {
                Utils.showProgressDialog(myMXTakaTakActivity);
                callVideoDownload(layoutGlobalUiBinding.etText.getText().toString().trim());
                showInterstitial();

            } else {
                Utils.setToast(myMXTakaTakActivity, "Enter Valid Url");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callVideoDownload(String Url) {
        try {
            Utils utils = new Utils(myMXTakaTakActivity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    commonClassForAPI.callTiktokVideo(mxObserver, Url);
                }
            } else {
                Utils.setToast(myMXTakaTakActivity, "No Internet Connection");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void pasteText() {
        try {
            layoutGlobalUiBinding.etText.setText("");
            String CopyIntent = getIntent().getStringExtra("CopyIntent");
            if (CopyIntent.equals("")) {

                if (!(clipboardManager.hasPrimaryClip())) {

                } else if (!(clipboardManager.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("mxtakatak")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("mxtakatak")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("mxtakatak")) {
                    layoutGlobalUiBinding.etText.setText(CopyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);


    }

    // fb interstitial ads
    public void fBInterstitialAdsINIT() {
        fbInterstitialAd = new InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    // fb interstitial ads

}