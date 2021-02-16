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
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryTikTok;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyTikTokActivity extends AppCompatActivity {
    MyTikTokActivity myTikTokActivity;
    CommonClassForAPI commonClassForAPI;
    AppLangSessionManager appLangSessionManager;
    InterstitialAd fbInterstitialAd;
    private LayoutGlobalUiBinding layoutGlobalUiBinding;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    private final DisposableObserver<MyTiktokModel> tiktokObserver = new DisposableObserver<MyTiktokModel>() {
        @Override
        public void onNext(MyTiktokModel myTiktokModel) {
            Utils.hideProgressDialog(myTikTokActivity);
            try {
                if (myTiktokModel.getResponsecode().equals("200")) {
                    startDownload(myTiktokModel.getData().getMainvideo(),
                            RootDirectoryTikTok, myTikTokActivity, "tiktok_" + System.currentTimeMillis() + ".mp4");
                    layoutGlobalUiBinding.etText.setText("");
                    showInterstitial();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable e) {
            Utils.hideProgressDialog(myTikTokActivity);
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            Utils.hideProgressDialog(myTikTokActivity);
        }
    };
    private String VideoUrl;
    private ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myTikTokActivity = this;

        mContext = MyTikTokActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Tiktok Activity");

        appLangSessionManager = new AppLangSessionManager(myTikTokActivity);
        setLocale(appLangSessionManager.getLanguage());
        commonClassForAPI = CommonClassForAPI.getInstance(myTikTokActivity);
        createFileFolder();
        initViews();

        /*AdsUtils.showFBBannerAd(myTikTokActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_tiktok));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.tiktok_app_name));


    }

    @Override
    protected void onResume() {
        super.onResume();
        myTikTokActivity = this;
        assert myTikTokActivity != null;
        clipboardManager = (ClipboardManager) myTikTokActivity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myTikTokActivity.getSystemService(CLIPBOARD_SERVICE);

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

        Glide.with(myTikTokActivity)
                .load(R.drawable.tt1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myTikTokActivity)
                .load(R.drawable.tt2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myTikTokActivity)
                .load(R.drawable.tt3)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myTikTokActivity)
                .load(R.drawable.tt4)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);


        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_tiktok));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.open_tiktok));
        if (!MySharePrefs.getInstance(myTikTokActivity).getBoolean(MySharePrefs.ISSHOWHOWTOTT)) {
            MySharePrefs.getInstance(myTikTokActivity).putBoolean(MySharePrefs.ISSHOWHOWTOTT, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            pasteText();
        });

        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myTikTokActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myTikTokActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                GetTikTokData();
            }
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Intent launchIntent = myTikTokActivity.getPackageManager().getLaunchIntentForPackage("com.zhiliaoapp.musically.go");
            Intent launchIntent1 = myTikTokActivity.getPackageManager().getLaunchIntentForPackage("com.zhiliaoapp.musically");
            if (launchIntent != null) {
                myTikTokActivity.startActivity(launchIntent);
            } else if (launchIntent1 != null) {
                myTikTokActivity.startActivity(launchIntent1);
            } else {
                Utils.setToast(myTikTokActivity, getResources().getString(R.string.app_not_available));
            }

        });
    }

    private void GetTikTokData() {
        try {
            createFileFolder();
            String host = layoutGlobalUiBinding.etText.getText().toString();
            if (host.contains("tiktok")) {
                Utils.showProgressDialog(myTikTokActivity);
                callVideoDownload(layoutGlobalUiBinding.etText.getText().toString());
                showInterstitial();
            } else {
                Utils.setToast(myTikTokActivity, "Enter Valid Url");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callVideoDownload(String Url) {
        try {
            Utils utils = new Utils(myTikTokActivity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    commonClassForAPI.callTiktokVideo(tiktokObserver, Url);
                }
            } else {
                Utils.setToast(myTikTokActivity, "No Internet Connection");
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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("tiktok")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("tiktok")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("tiktok")) {
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

    //fb interstital ads
    public void fBInterstitialAdsINIT() {
        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    //fb interstital ads
}