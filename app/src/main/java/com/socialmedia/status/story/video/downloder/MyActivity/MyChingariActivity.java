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
import androidx.core.content.ContextCompat;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.ROOTDIRECTORYCHINGARI;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyChingariActivity extends AppCompatActivity {
    private LayoutGlobalUiBinding layoutGlobalUiBinding;
    MyChingariActivity myChingariActivity;
    CommonClassForAPI commonClassForAPI;
    private String VideoUrl;
    private ClipboardManager clipboardManager;
    AppLangSessionManager appLangSessionManager;
    InterstitialAd fbInterstitialAd;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myChingariActivity = this;
        mContext = MyChingariActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Chingari Activity");
        commonClassForAPI = CommonClassForAPI.getInstance(myChingariActivity);
        createFileFolder();
        initViews();

        appLangSessionManager = new AppLangSessionManager(myChingariActivity);
        setLocale(appLangSessionManager.getLanguage());

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.chingari));
        layoutGlobalUiBinding.imAppIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cld));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.chingari_app_name));


        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);
        /*FBInterstitialAdsINIT();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        myChingariActivity = this;
        assert myChingariActivity != null;
        clipboardManager = (ClipboardManager) myChingariActivity.getSystemService(CLIPBOARD_SERVICE);
        PasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myChingariActivity.getSystemService(CLIPBOARD_SERVICE);

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

        Glide.with(myChingariActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myChingariActivity)
                .load(R.drawable.sc2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myChingariActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myChingariActivity)
                .load(R.drawable.chi2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);

        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.LLHowToOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadTwo.setText(getResources().getString(R.string.how_to_download));

        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_chingari));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.cop_link_from_chingari));
        if (!MySharePrefs.getInstance(myChingariActivity).getBoolean(MySharePrefs.ISSHOWHOWTOCHINGARI)) {
            MySharePrefs.getInstance(myChingariActivity).putBoolean(MySharePrefs.ISSHOWHOWTOCHINGARI, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myChingariActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myChingariActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                Utils.showProgressDialog(myChingariActivity);
                getChingariData();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            PasteText();
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myChingariActivity, "io.chingari.app");
        });
    }

    private void getChingariData() {
        try {
            createFileFolder();
            URL url = new URL(layoutGlobalUiBinding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains("chingari")) {
                Utils.showProgressDialog(myChingariActivity);
                new CallGetChingariData().execute(layoutGlobalUiBinding.etText.getText().toString());
                /*showInterstitial();*/
            } else {
                Utils.setToast(myChingariActivity, getResources().getString(R.string.enter_url));
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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("chingari")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("chingari")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("chingari")) {
                    layoutGlobalUiBinding.etText.setText(CopyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class CallGetChingariData extends AsyncTask<String, Void, Document> {
        Document chingariDoc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... urls) {
            try {
                chingariDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return chingariDoc;
        }

        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(myChingariActivity);
            try {
                VideoUrl = result.select("meta[property=\"og:video:secure_url\"]").last().attr("content");
                if (!VideoUrl.equals("")) {
                    startDownload(VideoUrl, ROOTDIRECTORYCHINGARI, myChingariActivity, "chingari_"+System.currentTimeMillis()+".mp4");
                    VideoUrl = "";
                    layoutGlobalUiBinding.etText.setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
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


    //fb interstial ads
    /*public void FBInterstitialAdsINIT() {
        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }*/
    //till here
}