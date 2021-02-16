package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.MyApi.CommonClassForAPI;
import com.socialmedia.status.story.video.downloder.databinding.LayoutGlobalUiBinding;
import com.socialmedia.status.story.video.downloder.MyModel.MyTwitterResponse;
import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.MySharePrefs;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.facebook.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import io.reactivex.observers.DisposableObserver;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryTwitter;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyTwitterActivity extends AppCompatActivity {
    MyTwitterActivity myTwitterActivity;
    CommonClassForAPI commonClassForAPI;
    AppLangSessionManager appLangSessionManager;
    private LayoutGlobalUiBinding layoutGlobalUiBinding;
    private String VideoUrl;
    private ClipboardManager clipboardManager;
    private InterstitialAd fbInterstitialAd;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;
    private final DisposableObserver<MyTwitterResponse> observer = new DisposableObserver<MyTwitterResponse>() {
        @Override
        public void onNext(MyTwitterResponse myTwitterResponse) {
            Utils.hideProgressDialog(myTwitterActivity);
            try {
                VideoUrl = myTwitterResponse.getVideos().get(0).getUrl();
                if (myTwitterResponse.getVideos().get(0).getType().equals("image")) {
                    startDownload(VideoUrl, RootDirectoryTwitter, myTwitterActivity, getFilenameFromURL(VideoUrl, "image"));
                    layoutGlobalUiBinding.etText.setText("");
                    showInterstitial();
                } else {
                    VideoUrl = myTwitterResponse.getVideos().get(myTwitterResponse.getVideos().size() - 1).getUrl();
                    startDownload(VideoUrl, RootDirectoryTwitter, myTwitterActivity, getFilenameFromURL(VideoUrl, "mp4"));
                    layoutGlobalUiBinding.etText.setText("");
                    showInterstitial();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Utils.setToast(myTwitterActivity, getResources().getString(R.string.no_media_on_tweet));
            }
        }

        @Override
        public void onError(Throwable e) {
            Utils.hideProgressDialog(myTwitterActivity);
            e.printStackTrace();

        }

        @Override
        public void onComplete() {
            Utils.hideProgressDialog(myTwitterActivity);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myTwitterActivity = this;

        mContext = MyTwitterActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Twitter Activity");

        commonClassForAPI = CommonClassForAPI.getInstance(myTwitterActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_twitter));
        layoutGlobalUiBinding.imAppIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cld));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.twitter_app_name));


        appLangSessionManager = new AppLangSessionManager(myTwitterActivity);
        setLocale(appLangSessionManager.getLanguage());

        /*AdsUtils.showFBBannerAd(myTwitterActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);


    }

    @Override
    protected void onResume() {
        super.onResume();
        myTwitterActivity = this;
        assert myTwitterActivity != null;
        clipboardManager = (ClipboardManager) myTwitterActivity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myTwitterActivity.getSystemService(CLIPBOARD_SERVICE);

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


        Glide.with(myTwitterActivity)
                .load(R.drawable.tw1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myTwitterActivity)
                .load(R.drawable.tw2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myTwitterActivity)
                .load(R.drawable.tw3)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myTwitterActivity)
                .load(R.drawable.tw4)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);


        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_twitter));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.open_twitter));
        if (!MySharePrefs.getInstance(myTwitterActivity).getBoolean(MySharePrefs.ISSHOWHOWTOTWITTER)) {
            MySharePrefs.getInstance(myTwitterActivity).putBoolean(MySharePrefs.ISSHOWHOWTOTWITTER, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myTwitterActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myTwitterActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                Utils.showProgressDialog(myTwitterActivity);
                GetTwitterData();
                showInterstitial();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            pasteText();
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myTwitterActivity, "com.twitter.android");
        });
    }

    private void GetTwitterData() {
        try {
            createFileFolder();
            URL url = new URL(layoutGlobalUiBinding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains("twitter.com")) {
                Long id = getTweetId(layoutGlobalUiBinding.etText.getText().toString());
                if (id != null) {
                    callGetTwitterData(String.valueOf(id));
                }
            } else {
                Utils.setToast(myTwitterActivity, getResources().getString(R.string.enter_url));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);
        } catch (Exception e) {
            Log.d("TAG", "getTweetId: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void pasteText() {
        try {
            layoutGlobalUiBinding.etText.setText("");
            String CopyIntent = getIntent().getStringExtra("CopyIntent");
            if (CopyIntent.equals("")) {

                if (!(clipboardManager.hasPrimaryClip())) {

                } else if (!(clipboardManager.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("twitter.com")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("twitter.com")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("twitter.com")) {
                    layoutGlobalUiBinding.etText.setText(CopyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callGetTwitterData(String id) {
        String URL = "https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php";
        try {
            Utils utils = new Utils(myTwitterActivity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    Utils.showProgressDialog(myTwitterActivity);
                    commonClassForAPI.callTwitterApi(observer, URL, id);
                }
            } else {
                Utils.setToast(myTwitterActivity, getResources().getString(R.string.no_net_conn));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String getFilenameFromURL(String url, String type) {
        if (type.equals("image")) {
            try {
                return new File(new URL(url).getPath()).getName() + "";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return System.currentTimeMillis() + ".jpg";
            }
        } else {
            try {
                return new File(new URL(url).getPath()).getName() + "";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return System.currentTimeMillis() + ".mp4";
            }
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

    // fb interstital ads
    public void fBInterstitialAdsINIT() {
        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    // fb interstital ads
}