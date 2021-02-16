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
import static android.content.ContentValues.TAG;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryRoposo;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyRoposoActivity extends AppCompatActivity {
    MyRoposoActivity myRoposoActivity;
    CommonClassForAPI commonClassForAPI;
    AppLangSessionManager appLangSessionManager;
    InterstitialAd fbInterstitialAd;
    private LayoutGlobalUiBinding layoutGlobalUiBinding;
    private String VideoUrl;
    private ClipboardManager clipboardManager;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myRoposoActivity = this;


        mContext = MyRoposoActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Roposo Activity");

        commonClassForAPI = CommonClassForAPI.getInstance(myRoposoActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_roposo));
        layoutGlobalUiBinding.imAppIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cld));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.roposo_app_name));


        appLangSessionManager = new AppLangSessionManager(myRoposoActivity);
        setLocale(appLangSessionManager.getLanguage());

        /*AdsUtils.showFBBannerAd(myRoposoActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        myRoposoActivity = this;
        assert myRoposoActivity != null;
        clipboardManager = (ClipboardManager) myRoposoActivity.getSystemService(CLIPBOARD_SERVICE);
        PasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myRoposoActivity.getSystemService(CLIPBOARD_SERVICE);

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

        Glide.with(myRoposoActivity)
                .load(R.drawable.r1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myRoposoActivity)
                .load(R.drawable.r2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myRoposoActivity)
                .load(R.drawable.r1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myRoposoActivity)
                .load(R.drawable.r2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.LLHowToOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadTwo.setText(getResources().getString(R.string.how_to_download));

        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_roposo));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.cop_link_from_roposo));


        if (!MySharePrefs.getInstance(myRoposoActivity).getBoolean(MySharePrefs.ISSHOWHOWTOROPOSO)) {
            MySharePrefs.getInstance(myRoposoActivity).putBoolean(MySharePrefs.ISSHOWHOWTOROPOSO, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myRoposoActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myRoposoActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                Utils.showProgressDialog(myRoposoActivity);
                GetRoposoData();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            PasteText();
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myRoposoActivity, "com.roposo.android");
        });
    }

    private void GetRoposoData() {
        try {
            createFileFolder();
            URL url = new URL(layoutGlobalUiBinding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains("roposo")) {
                Utils.showProgressDialog(myRoposoActivity);
                new callGetRoposoData().execute(layoutGlobalUiBinding.etText.getText().toString());
                showInterstitial();
            } else {
                Utils.setToast(myRoposoActivity, getResources().getString(R.string.enter_url));
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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("roposo")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("roposo")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("roposo")) {
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

    //fb interstitial ads
    public void fBInterstitialAdsINIT() {

        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    //fb interstitial ads

    class callGetRoposoData extends AsyncTask<String, Void, Document> {
        Document RoposoDoc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... urls) {
            try {
                RoposoDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            return RoposoDoc;
        }

        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(myRoposoActivity);
            try {
                VideoUrl = result.select("meta[property=\"og:video\"]").last().attr("content");
                if (VideoUrl == null || VideoUrl.equals("")) {
                    VideoUrl = result.select("meta[property=\"og:video:url\"]").last().attr("content");
                }
                Log.e("onPostExecute: ", VideoUrl);
                if (!VideoUrl.equals("")) {
                    try {
                        startDownload(VideoUrl, RootDirectoryRoposo, myRoposoActivity, "roposo_" + System.currentTimeMillis() + ".mp4");
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


}