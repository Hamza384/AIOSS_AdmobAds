package com.socialmedia.status.story.video.downloder.MyActivity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.ContentValues.TAG;
import static com.socialmedia.status.story.video.downloder.MyActivity.MainActivity.extractLinks;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryLikeeShow;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;

public class MyLikeeActivity extends AppCompatActivity {
    LayoutGlobalUiBinding layoutGlobalUiBinding;
    MyLikeeActivity myLikeeActivity;
    CommonClassForAPI commonClassForAPI;
    Pattern pattern = Pattern.compile("window\\.data \\s*=\\s*(\\{.+?\\});");
    ProgressDialog mProgressDialog;
    AppLangSessionManager appLangSessionManager;
    AsyncTask downloadAsyncTask;
    InterstitialAd fbInterstitialAd;
    private String VideoUrl;
    private ClipboardManager clipboardManager;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutGlobalUiBinding = DataBindingUtil.setContentView(this, R.layout.layout_global_ui);
        myLikeeActivity = this;

        mContext = MyLikeeActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Likee Activity");

        appLangSessionManager = new AppLangSessionManager(myLikeeActivity);
        setLocale(appLangSessionManager.getLanguage());

        commonClassForAPI = CommonClassForAPI.getInstance(myLikeeActivity);
        createFileFolder();
        initViews();

        layoutGlobalUiBinding.imAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.likee_logo));
        layoutGlobalUiBinding.imAppIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.cld));
        layoutGlobalUiBinding.tvAppName.setText(getResources().getString(R.string.likee_app_name));


        /*AdsUtils.showFBBannerAd(myLikeeActivity, layoutGlobalUiBinding.bannerContainer);
        fBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,layoutGlobalUiBinding.adView);


        initiliazeDialog();

    }

    void initiliazeDialog() {
        mProgressDialog = new ProgressDialog(myLikeeActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(getResources().getString(R.string.downloadin_video));
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFD4D9D0")));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProgressDialog.dismiss();

                if (downloadAsyncTask != null) {
                    mProgressDialog.setProgress(0);
                    downloadAsyncTask.cancel(true);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        myLikeeActivity = this;
        assert myLikeeActivity != null;
        clipboardManager = (ClipboardManager) myLikeeActivity.getSystemService(CLIPBOARD_SERVICE);
        PasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myLikeeActivity.getSystemService(CLIPBOARD_SERVICE);
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


        Glide.with(myLikeeActivity)
                .load(R.drawable.likee1)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto1);

        Glide.with(myLikeeActivity)
                .load(R.drawable.likee2)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto2);

        Glide.with(myLikeeActivity)
                .load(R.drawable.likee3)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto3);

        Glide.with(myLikeeActivity)
                .load(R.drawable.likee4)
                .into(layoutGlobalUiBinding.layoutHowTo.imHowto4);


        layoutGlobalUiBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_likee));
        layoutGlobalUiBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.copy_video_link_from_likee));

        if (!MySharePrefs.getInstance(myLikeeActivity).getBoolean(MySharePrefs.ISSHOWHOWTOLIKEE)) {
            MySharePrefs.getInstance(myLikeeActivity).putBoolean(MySharePrefs.ISSHOWHOWTOLIKEE, true);
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            layoutGlobalUiBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }

        layoutGlobalUiBinding.loginBtn1.setOnClickListener(v -> {
            String LL = layoutGlobalUiBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myLikeeActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myLikeeActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                showInterstitial();
                GetLikeeData();
            }
        });

        layoutGlobalUiBinding.tvPaste.setOnClickListener(v -> {
            PasteText();
        });
        layoutGlobalUiBinding.LLOpenApp.setOnClickListener(v -> {
            Utils.OpenApp(myLikeeActivity, "video.like");
        });


    }

    private void GetLikeeData() {
        try {
            createFileFolder();
            String url = layoutGlobalUiBinding.etText.getText().toString();
            if (url.contains("likee")) {
                Utils.showProgressDialog(myLikeeActivity);
                new callGetLikeeData().execute(url);
            } else {
                Utils.setToast(myLikeeActivity, getResources().getString(R.string.enter_valid_url));
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
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("likee")) {
                        layoutGlobalUiBinding.etText.setText(extractLinks(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString()));
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("likee")) {
                        layoutGlobalUiBinding.etText.setText(extractLinks(item.getText().toString()));
                    }

                }
            } else {
                if (CopyIntent.contains("likee")) {
                    CopyIntent = extractLinks(CopyIntent);
                    layoutGlobalUiBinding.etText.setText(CopyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFilenameFromURL(String url) {
        try {
            return new File(new URL(url).getPath()).getName() + "";
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

    // fb interstitial ads
    public void fBInterstitialAdsINIT() {
        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    // fb interstitial ads



    class callGetLikeeData extends AsyncTask<String, Void, Document> {
        Document likeeDoc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... urls) {
            try {
                likeeDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            return likeeDoc;
        }

        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(myLikeeActivity);
            try {
                String JSONData = "";
                Matcher matcher = pattern.matcher(result.toString());
                while (matcher.find()) {
                    JSONData = matcher.group().replaceFirst("window.data = ", "").replace(";", "");
                }
                JSONObject jsonObject = new JSONObject(JSONData);
                VideoUrl = jsonObject.getString("video_url").replace("_4", "");
                //VideoUrl = VideoUrl.substring(VideoUrl.indexOf("http"),VideoUrl.indexOf("?"));
                Log.e("onPostExecute: ", VideoUrl);
                if (!VideoUrl.equals("")) {
                    try {
                        //startDownload(VideoUrl, RootDirectoryLikee, mainActivity, getFilenameFromURL(VideoUrl));
                        mProgressDialog.show();
                        downloadAsyncTask = new DownloadFileFromURL().execute(VideoUrl);

                        showInterstitial();
                        VideoUrl = "";
                        layoutGlobalUiBinding.etText.setText("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(
                        RootDirectoryLikeeShow + "/" + getFilenameFromURL(VideoUrl));
                byte[] data = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            mProgressDialog.dismiss();
            mProgressDialog.setProgress(0);

            Utils.setToast(myLikeeActivity, getResources().getString(R.string.download_complete));
            try {
                if (Build.VERSION.SDK_INT >= 19) {
                    MediaScannerConnection.scanFile(myLikeeActivity, new String[]
                                    {new File(RootDirectoryLikeeShow + "/" + getFilenameFromURL(VideoUrl)).getAbsolutePath()},
                            null, new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                }
                            });
                } else {
                    myLikeeActivity.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED",
                            Uri.fromFile(new File(RootDirectoryLikeeShow + "/" + getFilenameFromURL(VideoUrl)))));
                }

                showInterstitial();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressDialog.setProgress(0);
            Log.d(TAG, "onCancelled: AysncTask");
        }
    }


}