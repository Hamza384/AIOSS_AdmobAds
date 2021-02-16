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
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryShareChat;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyShareChatActivity extends AppCompatActivity {
    MyShareChatActivity myShareChatActivity;
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
        myShareChatActivity = this;


        mContext = MyShareChatActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Share chat Activity");


        commonClassForAPI = CommonClassForAPI.getInstance(myShareChatActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_sharechat));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.sharechat_app_name));


        appLangSessionManager = new AppLangSessionManager(myShareChatActivity);
        setLocale(appLangSessionManager.getLanguage());

        /*AdsUtils.showFBBannerAd(myShareChatActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        myShareChatActivity = this;
        assert myShareChatActivity != null;
        clipboardManager = (ClipboardManager) myShareChatActivity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myShareChatActivity.getSystemService(CLIPBOARD_SERVICE);

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

        Glide.with(myShareChatActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myShareChatActivity)
                .load(R.drawable.sc2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myShareChatActivity)
                .load(R.drawable.sc1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myShareChatActivity)
                .load(R.drawable.sc2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);

        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.LLHowToOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadTwo.setText(getResources().getString(R.string.how_to_download));

        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_sharechat));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.cop_link_from_sharechat));
        if (!MySharePrefs.getInstance(myShareChatActivity).getBoolean(MySharePrefs.ISSHOWHOWTOSHARECHAT)) {
            MySharePrefs.getInstance(myShareChatActivity).putBoolean(MySharePrefs.ISSHOWHOWTOSHARECHAT, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myShareChatActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myShareChatActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                Utils.showProgressDialog(myShareChatActivity);
                GetSharechatData();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            pasteText();
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myShareChatActivity, "in.mohalla.sharechat");
        });
    }

    private void GetSharechatData() {
        try {
            createFileFolder();
            URL url = new URL(layoutGlobalUiBinding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains("sharechat")) {
                Utils.showProgressDialog(myShareChatActivity);
                new callGetShareChatData().execute(layoutGlobalUiBinding.etText.getText().toString());
                showInterstitial();
            } else {
                Utils.setToast(myShareChatActivity, getResources().getString(R.string.enter_url));
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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("sharechat")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("sharechat")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("sharechat")) {
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

    class callGetShareChatData extends AsyncTask<String, Void, Document> {
        Document ShareChatDoc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... urls) {
            try {
                ShareChatDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            return ShareChatDoc;
        }

        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(myShareChatActivity);
            try {

                VideoUrl = result.select("meta[property=\"og:video:secure_url\"]").last().attr("content");
                Log.e("onPostExecute: ", VideoUrl);
                if (!VideoUrl.equals("")) {
                    try {
                        startDownload(VideoUrl, RootDirectoryShareChat, myShareChatActivity, "sharechat_" + System.currentTimeMillis() + ".mp4");
                        VideoUrl = "";
                        layoutGlobalUiBinding.etText.setText("");

                        showInterstitial();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }


}