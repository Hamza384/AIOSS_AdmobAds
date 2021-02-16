package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.MyApi.CommonClassForAPI;

import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.MySharePrefs;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.facebook.ads.InterstitialAd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import com.socialmedia.status.story.video.downloder.databinding.LayoutGlobalUiBinding;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.ContentValues.TAG;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryFacebook;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyFacebookActivity extends AppCompatActivity {
    LayoutGlobalUiBinding layoutGlobalUiBinding;
    MyFacebookActivity myFacebookActivity;
    CommonClassForAPI commonClassForAPI;
    private String VideoUrl;
    private ClipboardManager clipboardManager;

    InterstitialAd fbInterstitialAd;
    AppLangSessionManager appLangSessionManager;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myFacebookActivity = this;

        mContext = MyFacebookActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Facebook Activity");

        appLangSessionManager = new AppLangSessionManager(myFacebookActivity);
        setLocale(appLangSessionManager.getLanguage());

        commonClassForAPI = CommonClassForAPI.getInstance(myFacebookActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_fb));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.facebook_app_name));

        /*AdsUtils.showFBBannerAd(myFacebookActivity, layoutGlobalUiBinding.bannerContainer);
        fbInterstitialAdsInit();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        myFacebookActivity = this;
        assert myFacebookActivity != null;
        clipboardManager = (ClipboardManager) myFacebookActivity.getSystemService(CLIPBOARD_SERVICE);
        PasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myFacebookActivity.getSystemService(CLIPBOARD_SERVICE);
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



        Glide.with(myFacebookActivity)
                .load(R.drawable.fb1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myFacebookActivity)
                .load(R.drawable.fb2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myFacebookActivity)
                .load(R.drawable.fb3)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myFacebookActivity)
                .load(R.drawable.fb4)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);


        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.opn_fb));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.copy_video_link_frm_fb));

        if (!MySharePrefs.getInstance(myFacebookActivity).getBoolean(MySharePrefs.ISSHOWHOWTOFB)) {
            MySharePrefs.getInstance(myFacebookActivity).putBoolean(MySharePrefs.ISSHOWHOWTOFB,true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        }else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }

        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myFacebookActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myFacebookActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                getFacebookData();
                showInterstitial();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            PasteText();
        });
        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myFacebookActivity,"com.facebook.katana");
        });


    }

    private void getFacebookData() {
        try {
            createFileFolder();
            String url = layoutGlobalUiBinding.etText.getText().toString();
            if (url.contains("facebook.com")||url.contains("fb")) {
                Utils.showProgressDialog(myFacebookActivity);
                new callGetFacebookData().execute(layoutGlobalUiBinding.etText.getText().toString());
            } else {
                Utils.setToast(myFacebookActivity, getResources().getString(R.string.enter_valid_url));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void PasteText() {
        try {
            layoutGlobalUiBinding.etText.setText("");
            String CopyIntent = getIntent().getStringExtra("CopyIntent");
            if (CopyIntent.equals("")) {
                if (!(clipboardManager.hasPrimaryClip())) {

                } else if (!(clipboardManager.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("facebook.com")
                            || clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("fb")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("facebook.com")||
                            item.getText().toString().contains("fb")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            }else {
                if (CopyIntent.contains("facebook.com")||CopyIntent.contains("fb")) {
                    layoutGlobalUiBinding.etText.setText(CopyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class callGetFacebookData extends AsyncTask<String, Void, Document> {
        Document facebookDoc;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Document doInBackground(String... urls) {
            try {
                facebookDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            return facebookDoc;
        }

        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(myFacebookActivity);
            try {

                VideoUrl = result.select("meta[property=\"og:video\"]").last().attr("content");
                Log.e("onPostExecute: ", VideoUrl);
                if (!VideoUrl.equals("")) {
                    try {
                        startDownload(VideoUrl, RootDirectoryFacebook, myFacebookActivity, getFilenameFromURL(VideoUrl));
                        VideoUrl = "";
                        layoutGlobalUiBinding.etText.setText("");
                        showInterstitial();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFilenameFromURL(String url) {
        try {
            return new File(new URL(url).getPath()).getName()+".mp4";
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return System.currentTimeMillis() + ".mp4";
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

    //fb ads here
    public void fbInterstitialAdsInit() {
        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    // fb ads till here


}