package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import io.reactivex.observers.DisposableObserver;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.ContentValues.TAG;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectorySnackVideo;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MySnackVideoActivity extends AppCompatActivity {
    MySnackVideoActivity mySnackVideoActivity;
    CommonClassForAPI commonClassForAPI;
    AppLangSessionManager appLangSessionManager;
    InterstitialAd fbInterstitialAd;
    private LayoutGlobalUiBinding layoutGlobalUiBinding;
    private String VideoUrl;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;
    private final DisposableObserver<JsonObject> observer = new DisposableObserver<JsonObject>() {
        @Override
        public void onNext(JsonObject jsonObject) {
            Utils.hideProgressDialog(mySnackVideoActivity);
            try {
                JsonObject photo = jsonObject.get("photo").getAsJsonObject();
                JsonArray mainArray = photo.get("main_mv_urls").getAsJsonArray();
                VideoUrl = mainArray.get(0).getAsJsonObject().get("url").getAsString();
                startDownload(VideoUrl, RootDirectorySnackVideo, mySnackVideoActivity, "snackvideo_" + System.currentTimeMillis() + ".mp4");
                VideoUrl = "";
                layoutGlobalUiBinding.etText.setText("");

                showInterstitial();

            } catch (Exception e) {
                e.printStackTrace();
                Utils.setToast(mySnackVideoActivity, getResources().getString(R.string.no_media_on_snackvideo));
            }
        }

        @Override
        public void onError(Throwable e) {
            Utils.hideProgressDialog(mySnackVideoActivity);
            e.printStackTrace();

        }

        @Override
        public void onComplete() {
            Utils.hideProgressDialog(mySnackVideoActivity);

        }
    };
    private ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        mySnackVideoActivity = this;

        mContext = MySnackVideoActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "MainActivity");

        commonClassForAPI = CommonClassForAPI.getInstance(mySnackVideoActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.snackvideo));
        layoutGlobalUiBinding.imAppIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cld));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.snack_app_name));


        appLangSessionManager = new AppLangSessionManager(mySnackVideoActivity);
        setLocale(appLangSessionManager.getLanguage());

        /*AdsUtils.showFBBannerAd(mySnackVideoActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mySnackVideoActivity = this;
        assert mySnackVideoActivity != null;
        clipboardManager = (ClipboardManager) mySnackVideoActivity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) mySnackVideoActivity.getSystemService(CLIPBOARD_SERVICE);

        layoutGlobalUiBinding.imBack.setOnClickListener(view -> onBackPressed());
        layoutGlobalUiBinding.imInfo.setOnClickListener(view -> layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE));

        Glide.with(mySnackVideoActivity)
                .load(R.drawable.sn1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(mySnackVideoActivity)
                .load(R.drawable.sn2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(mySnackVideoActivity)
                .load(R.drawable.sn1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(mySnackVideoActivity)
                .load(R.drawable.sn2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);

        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.LLHowToOne.setVisibility(View.GONE);
        layoutGlobalUiBinding.layoutHowTo.tvHowToHeadTwo.setText(getResources().getString(R.string.how_to_download));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_snack));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.cop_link_from_snack));
        if (!MySharePrefs.getInstance(mySnackVideoActivity).getBoolean(MySharePrefs.ISSHOWHOWTOSNACK)) {
            MySharePrefs.getInstance(mySnackVideoActivity).putBoolean(MySharePrefs.ISSHOWHOWTOSNACK, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(mySnackVideoActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(mySnackVideoActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                GetsnackvideoData();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            pasteText();
        });

        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(mySnackVideoActivity, "com.kwai.bulldog");
        });
    }

    private void GetsnackvideoData() {
        try {
            createFileFolder();
            URL url = new URL(layoutGlobalUiBinding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains("snackvideo")) {
                Utils.showProgressDialog(mySnackVideoActivity);
                new callGetsnackvideoData().execute(layoutGlobalUiBinding.etText.getText().toString());


            } else if (host.contains("sck.io")) {
                getUrlData(layoutGlobalUiBinding.etText.getText().toString());
            } else {
                Utils.setToast(mySnackVideoActivity, getResources().getString(R.string.enter_valid_url));
            }

            showInterstitial();

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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("snackvideo") || clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("sck.io")) {
                        layoutGlobalUiBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("snackvideo") || item.getText().toString().contains("sck.io")) {
                        layoutGlobalUiBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("snackvideo") || CopyIntent.contains("sck.io")) {
                    layoutGlobalUiBinding.etText.setText(CopyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUrlData(String str) {
        URI uri;
        try {
            uri = new URI(str);
        } catch (Exception e) {
            e.printStackTrace();
            uri = null;
        }
        String[] split = uri.getPath().split("/");
        String str2 = split[split.length - 1];
        String str3 = "android";
        String str4 = "8c46a905";
        StringBuilder sb = new StringBuilder("ANDROID_");
        sb.append(Settings.Secure.getString(getContentResolver(), "android_id"));
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        arrayList.add("mod=OnePlus(ONEPLUS A5000)");
        arrayList.add("lon=0");
        arrayList.add("country_code=in");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("did=");
        sb2.append(sb);
        arrayList.add(sb2.toString());
        arrayList.add("app=1");
        arrayList.add("oc=UNKNOWN");
        arrayList.add("egid=");
        arrayList.add("ud=0");
        arrayList.add("c=GOOGLE_PLAY");
        arrayList.add("sys=KWAI_BULLDOG_ANDROID_9");
        arrayList.add("appver=2.7.1.153");
        arrayList.add("mcc=0");
        arrayList.add("language=en-in");
        arrayList.add("lat=0");
        arrayList.add("ver=2.7");
        arrayList2.addAll(arrayList);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("shortKey=");
        sb3.append(str2);
        arrayList2.add(sb3.toString());
        StringBuilder sb4 = new StringBuilder();
        sb4.append("os=");
        sb4.append(str3);
        arrayList2.add(sb4.toString());
        StringBuilder sb5 = new StringBuilder();
        sb5.append("client_key=");
        sb5.append(str4);
        arrayList2.add(sb5.toString());
        try {
            Collections.sort(arrayList2);
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        String clockKey = yxcorp.gifs.utils.CPU.getClockData(this, TextUtils.join("", arrayList2).getBytes(StandardCharsets.UTF_8), 0);

        StringBuilder sb6 = new StringBuilder();
        sb6.append("https://g-api.snackvideo.com/rest/bulldog/share/get?");
        sb6.append(TextUtils.join("&", arrayList));
        try {
            Utils utils = new Utils(mySnackVideoActivity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    Utils.showProgressDialog(mySnackVideoActivity);
                    commonClassForAPI.callSnackVideoData(observer, sb6.toString(), str2, str3, clockKey, str4);
                }
            } else {
                Utils.setToast(mySnackVideoActivity, getResources().getString(R.string.no_net_conn));
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


    class callGetsnackvideoData extends AsyncTask<String, Void, Document> {
        Document snackvideoDoc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... urls) {
            try {
                snackvideoDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            return snackvideoDoc;
        }

        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(mySnackVideoActivity);
            try {
                Elements element = result.select("script");
                String URL = "";
                for (Element script : element) {
                    String a = script.data();
                    if (a.contains("window.__INITIAL_STATE__")) {
                        a = a.substring(a.indexOf("{"), a.indexOf("};")) + "}";
                        URL = a;
                        break;
                    }
                }
                if (!URL.equals("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(URL);
                        VideoUrl = jsonObject.getJSONObject("sharePhoto").getString("mp4Url");
                        String Url = jsonObject.getString("shortUrl");
                        getUrlData(Url);
                        VideoUrl = "";
                        layoutGlobalUiBinding.etText.setText("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                showInterstitial();


            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }


    }


}