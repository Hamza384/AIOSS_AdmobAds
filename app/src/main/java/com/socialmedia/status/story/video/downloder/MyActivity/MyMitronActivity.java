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
import java.util.Locale;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.ROOTDIRECTORYMITRON;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyMitronActivity extends AppCompatActivity {
    LayoutGlobalUiBinding layoutGlobalUiBinding;
    MyMitronActivity myMitronActivity;
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
        myMitronActivity = this;

        mContext = MyMitronActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Mitron Activity");

        commonClassForAPI = CommonClassForAPI.getInstance(myMitronActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_moitroo));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.mitron_app_name));


        appLangSessionManager = new AppLangSessionManager(myMitronActivity);
        setLocale(appLangSessionManager.getLanguage());

        /*AdsUtils.showFBBannerAd(myMitronActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        myMitronActivity = this;
        assert myMitronActivity != null;
        clipboardManager = (ClipboardManager) myMitronActivity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myMitronActivity.getSystemService(CLIPBOARD_SERVICE);

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

        Glide.with(myMitronActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myMitronActivity)
                .load(R.drawable.sc2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myMitronActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myMitronActivity)
                .load(R.drawable.mi2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);

        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.LLHowToOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadTwo.setText(getResources().getString(R.string.how_to_download));

        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_mitron));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.cop_link_from_mitron));
        if (!MySharePrefs.getInstance(myMitronActivity).getBoolean(MySharePrefs.ISSHOWHOWTOMITRON)) {
            MySharePrefs.getInstance(myMitronActivity).putBoolean(MySharePrefs.ISSHOWHOWTOMITRON, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myMitronActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myMitronActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                Utils.showProgressDialog(myMitronActivity);
                getMitronData();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            pasteText();
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myMitronActivity, "com.mitron.tv");
        });
    }

    private void getMitronData() {
        try {
            createFileFolder();
            String url = layoutGlobalUiBinding.etText.getText().toString();
            if (url.contains("mitron")) {
                Utils.showProgressDialog(myMitronActivity);
                String[] splitUrl = url.split("=");
                url = "https://web.mitron.tv/video/" + splitUrl[splitUrl.length - 1];
                new callGetJoshData().execute(url);
                showInterstitial();
            } else {
                Utils.setToast(myMitronActivity, getResources().getString(R.string.enter_valid_url));
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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("mitron")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("mitron")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("mitron")) {
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

        @Override
        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(myMitronActivity);
            try {
                String url = result.select("script[id=\"__NEXT_DATA__\"]").last().html();

                if (!url.equals("")) {
                    JSONObject jsonObject = new JSONObject(url);
                    VideoUrl = jsonObject.getJSONObject("props")
                            .getJSONObject("pageProps").getJSONObject("video")
                            .getString("videoUrl");
                    startDownload(VideoUrl, ROOTDIRECTORYMITRON, myMitronActivity, "mitron_" + System.currentTimeMillis() + ".mp4");
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