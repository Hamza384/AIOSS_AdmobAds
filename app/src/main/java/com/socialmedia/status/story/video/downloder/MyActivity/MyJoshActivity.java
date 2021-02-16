package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
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
import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.MySharePrefs;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.facebook.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.ROOTDIRECTORYJOSH;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyJoshActivity extends AppCompatActivity {
    LayoutGlobalUiBinding layoutGlobalUiBinding;
    MyJoshActivity myJoshActivity;
    CommonClassForAPI commonClassForAPI;
    AppLangSessionManager appLangSessionManager;
    InterstitialAd fbInterstitialAd;
    private String VideoUrl;
    private ClipboardManager clipboardManager;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myJoshActivity = this;

        mContext = MyJoshActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Josh Activity");

        commonClassForAPI = CommonClassForAPI.getInstance(myJoshActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.josh_logo));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.josh_app_name));


        appLangSessionManager = new AppLangSessionManager(myJoshActivity);
        setLocale(appLangSessionManager.getLanguage());

        /*AdsUtils.showFBBannerAd(myJoshActivity, layoutGlobalUiBinding.bannerContainer);
        fbInterstitialAdsInit();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        myJoshActivity = this;
        assert myJoshActivity != null;
        clipboardManager = (ClipboardManager) myJoshActivity.getSystemService(CLIPBOARD_SERVICE);
        PasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myJoshActivity.getSystemService(CLIPBOARD_SERVICE);

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

        Glide.with(myJoshActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myJoshActivity)
                .load(R.drawable.sc2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myJoshActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myJoshActivity)
                .load(R.drawable.jo2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);

        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.LLHowToOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadTwo.setText(getResources().getString(R.string.how_to_download));

        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_josh));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.cop_link_from_josh));
        if (!MySharePrefs.getInstance(myJoshActivity).getBoolean(MySharePrefs.ISSHOWHOWTOJOSH)) {
            MySharePrefs.getInstance(myJoshActivity).putBoolean(MySharePrefs.ISSHOWHOWTOJOSH, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myJoshActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myJoshActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                Utils.showProgressDialog(myJoshActivity);
                getJoshData();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            PasteText();
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myJoshActivity, "com.eterno.shortvideos");
        });
    }

    private void getJoshData() {
        try {
            createFileFolder();
            URL url = new URL(layoutGlobalUiBinding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains("myjosh")) {
                Utils.showProgressDialog(myJoshActivity);
                new callGetJoshData().execute(layoutGlobalUiBinding.etText.getText().toString());
                showInterstitial();
            } else {
                Utils.setToast(myJoshActivity, getResources().getString(R.string.enter_url));
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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("myjosh")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("myjosh")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("myjosh")) {
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


    // fb intersitial ads
    public void fbInterstitialAdsInit() {
        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    // fb intersitial ads till here

    class callGetJoshData extends AsyncTask<String, Void, Document> {
        Document JoshDoc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... urls) {
            try {
                JoshDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return JoshDoc;
        }

        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(myJoshActivity);
            try {
                String url = result.select("script[id=\"__NEXT_DATA__\"]").last().html();
                if (!url.equals("")) {
                    JSONObject jsonObject = new JSONObject(url);
                    VideoUrl = jsonObject.getJSONObject("props")
                            .getJSONObject("pageProps").getJSONObject("detail")
                            .getJSONObject("data").
                                    getString("mp4_url");
                    startDownload(VideoUrl, ROOTDIRECTORYJOSH, myJoshActivity, "josh_" + System.currentTimeMillis() + ".mp4");
                    VideoUrl = "";
                    layoutGlobalUiBinding.etText.setText("");
                    showInterstitial();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}