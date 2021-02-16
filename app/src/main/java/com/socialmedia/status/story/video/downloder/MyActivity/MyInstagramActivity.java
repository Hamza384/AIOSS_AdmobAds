package com.socialmedia.status.story.video.downloder.MyActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.adapter.MyStoriesListAdapter;
import com.socialmedia.status.story.video.downloder.adapter.MyUserListAdapter;
import com.socialmedia.status.story.video.downloder.MyApi.CommonClassForAPI;
import com.socialmedia.status.story.video.downloder.databinding.ActivityInstagramBinding;
import com.socialmedia.status.story.video.downloder.MyInterfaces.UserListInterface;
import com.socialmedia.status.story.video.downloder.MyModel.Edge;
import com.socialmedia.status.story.video.downloder.MyModel.EdgeSidecarToChildren;
import com.socialmedia.status.story.video.downloder.MyModel.MyResponseModel;
import com.socialmedia.status.story.video.downloder.MyModel.story.MyFullDetailModel;
import com.socialmedia.status.story.video.downloder.MyModel.story.MyStoryModel;
import com.socialmedia.status.story.video.downloder.MyModel.story.MyTrayModel;
import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.MySharePrefs;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.facebook.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import io.reactivex.observers.DisposableObserver;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.RootDirectoryInsta;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.startDownload;

public class MyInstagramActivity extends AppCompatActivity implements UserListInterface {
    Context mContext;
    CommonClassForAPI commonClassForAPI;
    AppLangSessionManager appLangSessionManager;
    MyUserListAdapter myUserListAdapter;
    MyStoriesListAdapter myStoriesListAdapter;
    private ActivityInstagramBinding activityInstagramBinding;
    private MyInstagramActivity myInstagramActivity;
    private ClipboardManager clipboardManager;
    private String PhotoUrl;
    private String VideoUrl;
    private InterstitialAd fbInterstitialAd;
    FirebaseAnalytics mFirebaseAnalytics;
    private final DisposableObserver<JsonObject> instaObserver = new DisposableObserver<JsonObject>() {
        @Override
        public void onNext(JsonObject versionList) {
            Utils.hideProgressDialog(myInstagramActivity);
            try {
                Log.e("onNext: ", versionList.toString());
                Type listType = new TypeToken<MyResponseModel>() {
                }.getType();
                MyResponseModel myResponseModel = new Gson().fromJson(versionList.toString(), listType);
                EdgeSidecarToChildren edgeSidecarToChildren = myResponseModel.getGraphql().getShortcode_media().getEdge_sidecar_to_children();
                if (edgeSidecarToChildren != null) {
                    List<Edge> edgeArrayList = edgeSidecarToChildren.getEdges();
                    for (int i = 0; i < edgeArrayList.size(); i++) {
                        if (edgeArrayList.get(i).getNode().isIs_video()) {
                            VideoUrl = edgeArrayList.get(i).getNode().getVideo_url();
                            startDownload(VideoUrl, RootDirectoryInsta, myInstagramActivity, getVideoFilenameFromURL(VideoUrl));
                            activityInstagramBinding.etText.setText("");
                            VideoUrl = "";

                        } else {
                            PhotoUrl = edgeArrayList.get(i).getNode().getDisplay_resources().get(edgeArrayList.get(i).getNode().getDisplay_resources().size() - 1).getSrc();
                            startDownload(PhotoUrl, RootDirectoryInsta, myInstagramActivity, getImageFilenameFromURL(PhotoUrl));
                            PhotoUrl = "";
                            activityInstagramBinding.etText.setText("");
                        }
                    }
                } else {
                    boolean isVideo = myResponseModel.getGraphql().getShortcode_media().isIs_video();
                    if (isVideo) {
                        VideoUrl = myResponseModel.getGraphql().getShortcode_media().getVideo_url();
                        //new DownloadFileFromURL().execute(VideoUrl,getFilenameFromURL(VideoUrl));
                        startDownload(VideoUrl, RootDirectoryInsta, myInstagramActivity, getVideoFilenameFromURL(VideoUrl));
                        VideoUrl = "";
                        activityInstagramBinding.etText.setText("");
                    } else {
                        PhotoUrl = myResponseModel.getGraphql().getShortcode_media().getDisplay_resources()
                                .get(myResponseModel.getGraphql().getShortcode_media().getDisplay_resources().size() - 1).getSrc();

                        startDownload(PhotoUrl, RootDirectoryInsta, myInstagramActivity, getImageFilenameFromURL(PhotoUrl));
                        PhotoUrl = "";
                        activityInstagramBinding.etText.setText("");
                        // new DownloadFileFromURL().execute(PhotoUrl,getFilenameFromURL(PhotoUrl));
                    }
                }

                showInterstitial();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable e) {
            Utils.hideProgressDialog(myInstagramActivity);
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            Utils.hideProgressDialog(myInstagramActivity);
        }
    };
    private final DisposableObserver<MyStoryModel> storyObserver = new DisposableObserver<MyStoryModel>() {
        @Override
        public void onNext(MyStoryModel response) {
            activityInstagramBinding.RVUserList.setVisibility(View.VISIBLE);
            activityInstagramBinding.prLoadingBar.setVisibility(View.GONE);
            try {
                myUserListAdapter = new MyUserListAdapter(myInstagramActivity, response.getTray(), myInstagramActivity);
                activityInstagramBinding.RVUserList.setAdapter(myUserListAdapter);
                myUserListAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable e) {
            activityInstagramBinding.prLoadingBar.setVisibility(View.GONE);
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            activityInstagramBinding.prLoadingBar.setVisibility(View.GONE);
        }
    };
    private final DisposableObserver<MyFullDetailModel> storyDetailObserver = new DisposableObserver<MyFullDetailModel>() {
        @Override
        public void onNext(MyFullDetailModel response) {
            activityInstagramBinding.RVUserList.setVisibility(View.VISIBLE);
            activityInstagramBinding.prLoadingBar.setVisibility(View.GONE);
            try {
                myStoriesListAdapter = new MyStoriesListAdapter(myInstagramActivity, response.getReel_feed().getItems());
                activityInstagramBinding.RVStories.setAdapter(myStoriesListAdapter);
                myStoriesListAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable e) {
            activityInstagramBinding.prLoadingBar.setVisibility(View.GONE);
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            activityInstagramBinding.prLoadingBar.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityInstagramBinding = DataBindingUtil.setContentView(this, R.layout.activity_instagram);

        mContext = myInstagramActivity = this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Instagram Activity");

        appLangSessionManager = new AppLangSessionManager(myInstagramActivity);
        setLocale(appLangSessionManager.getLanguage());


        commonClassForAPI = CommonClassForAPI.getInstance(myInstagramActivity);
        createFileFolder();

        /*AdsUtils.showFBBannerAd(MyInstagramActivity.this, activityInstagramBinding.bannerContainer);
        FBInterstitialAdsINIT();*/
        AdsUtils.showGoogleBannerAd(mContext,activityInstagramBinding.adView);

        initViews();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mContext = myInstagramActivity = this;
        assert myInstagramActivity != null;
        clipboardManager = (ClipboardManager) myInstagramActivity.getSystemService(CLIPBOARD_SERVICE);
        PasteText();
    }

    private void initViews() {
        clipboardManager = (ClipboardManager) myInstagramActivity.getSystemService(CLIPBOARD_SERVICE);

        activityInstagramBinding.imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        /*activityInstagramBinding.imInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityInstagramBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
            }
        });


        Glide.with(myInstagramActivity)
                .load(R.drawable.insta1)
                .into(activityInstagramBinding.layoutHowTo.imHowto1);

        Glide.with(myInstagramActivity)
                .load(R.drawable.insta2)
                .into(activityInstagramBinding.layoutHowTo.imHowto2);

        Glide.with(myInstagramActivity)
                .load(R.drawable.insta3)
                .into(activityInstagramBinding.layoutHowTo.imHowto3);

        Glide.with(myInstagramActivity)
                .load(R.drawable.insta4)
                .into(activityInstagramBinding.layoutHowTo.imHowto4);


        activityInstagramBinding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.opn_insta));
        activityInstagramBinding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.opn_insta));
        if (!MySharePrefs.getInstance(myInstagramActivity).getBoolean(MySharePrefs.ISSHOWHOWTOINSTA)) {
            MySharePrefs.getInstance(myInstagramActivity).putBoolean(MySharePrefs.ISSHOWHOWTOINSTA, true);
            activityInstagramBinding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        } else {
            activityInstagramBinding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }*/


        activityInstagramBinding.loginBtn1.setOnClickListener(v -> {

            String LL = activityInstagramBinding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(myInstagramActivity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(myInstagramActivity, getResources().getString(R.string.enter_valid_url));
            } else {
                GetInstagramData();
                showInterstitial();
            }


        });

        activityInstagramBinding.tvPaste.setOnClickListener(v -> {
            PasteText();
        });
        activityInstagramBinding.LLOpenInstagram.setOnClickListener(v -> {
            Utils.OpenApp(myInstagramActivity, "com.instagram.android");
        });

        GridLayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        activityInstagramBinding.RVUserList.setLayoutManager(mLayoutManager);
        activityInstagramBinding.RVUserList.setNestedScrollingEnabled(false);
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);


        if (MySharePrefs.getInstance(myInstagramActivity).getBoolean(MySharePrefs.ISINSTALOGIN)) {
            layoutCondition();
            callStoriesApi();
            activityInstagramBinding.SwitchLogin.setChecked(true);
        } else {
            activityInstagramBinding.SwitchLogin.setChecked(false);
        }

        activityInstagramBinding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(myInstagramActivity,
                        MyLoginActivity.class);
                startActivityForResult(intent, 100);
            }
        });

        activityInstagramBinding.RLLoginInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MySharePrefs.getInstance(myInstagramActivity).getBoolean(MySharePrefs.ISINSTALOGIN)) {
                    Intent intent = new Intent(myInstagramActivity,
                            MyLoginActivity.class);
                    startActivityForResult(intent, 100);
                } else {
                    AlertDialog.Builder ab = new AlertDialog.Builder(myInstagramActivity);
                    ab.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MySharePrefs.getInstance(myInstagramActivity).putBoolean(MySharePrefs.ISINSTALOGIN, false);
                            MySharePrefs.getInstance(myInstagramActivity).putString(MySharePrefs.COOKIES, "");
                            MySharePrefs.getInstance(myInstagramActivity).putString(MySharePrefs.CSRF, "");
                            MySharePrefs.getInstance(myInstagramActivity).putString(MySharePrefs.SESSIONID, "");
                            MySharePrefs.getInstance(myInstagramActivity).putString(MySharePrefs.USERID, "");

                            if (MySharePrefs.getInstance(myInstagramActivity).getBoolean(MySharePrefs.ISINSTALOGIN)) {
                                activityInstagramBinding.SwitchLogin.setChecked(true);
                            } else {
                                activityInstagramBinding.SwitchLogin.setChecked(false);
                                activityInstagramBinding.RVUserList.setVisibility(View.GONE);
                                activityInstagramBinding.RVStories.setVisibility(View.GONE);
                                activityInstagramBinding.tvViewStories.setText(myInstagramActivity.getResources().getText(R.string.view_stories));
                                activityInstagramBinding.tvLogin.setVisibility(View.VISIBLE);
                            }
                            dialog.cancel();

                        }
                    });
                    ab.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = ab.create();
                    alert.setTitle(getResources().getString(R.string.do_u_want_to_download_media_from_pvt));
                    alert.show();
                }

            }
        });

        GridLayoutManager mLayoutManager1 = new GridLayoutManager(getApplicationContext(), 3);
        activityInstagramBinding.RVStories.setLayoutManager(mLayoutManager1);
        activityInstagramBinding.RVStories.setNestedScrollingEnabled(false);
        mLayoutManager1.setOrientation(RecyclerView.VERTICAL);

    }

    public void layoutCondition() {
        activityInstagramBinding.tvViewStories.setText(myInstagramActivity.getResources().getString(R.string.stories));
        activityInstagramBinding.tvLogin.setVisibility(View.GONE);

    }

    private void GetInstagramData() {
        try {
            createFileFolder();
            URL url = new URL(activityInstagramBinding.etText.getText().toString());
            String host = url.getHost();
            Log.e("initViews: ", host);
            if (host.equals("www.instagram.com")) {
                callDownload(activityInstagramBinding.etText.getText().toString());
            } else {
                Utils.setToast(myInstagramActivity, getResources().getString(R.string.enter_valid_url));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void PasteText() {
        try {
            activityInstagramBinding.etText.setText("");
            String CopyIntent = getIntent().getStringExtra("CopyIntent");
            if (CopyIntent.equals("")) {
                if (!(clipboardManager.hasPrimaryClip())) {

                } else if (!(clipboardManager.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    if (clipboardManager.getPrimaryClip().getItemAt(0).getText().toString().contains("instagram.com")) {
                        activityInstagramBinding.etText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("instagram.com")) {
                        activityInstagramBinding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("instagram.com")) {
                    activityInstagramBinding.etText.setText(CopyIntent);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getUrlWithoutParameters(String url) {
        try {
            URI uri = new URI(url);
            return new URI(uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    null, // Ignore the query part of the input url
                    uri.getFragment()).toString();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.setToast(myInstagramActivity, getResources().getString(R.string.enter_valid_url));
            return "";
        }
    }

    private void callDownload(String Url) {
        String UrlWithoutQP = getUrlWithoutParameters(Url);
        UrlWithoutQP = UrlWithoutQP + "?__a=1";
        try {
            Utils utils = new Utils(myInstagramActivity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    Utils.showProgressDialog(myInstagramActivity);
                    commonClassForAPI.callResult(instaObserver, UrlWithoutQP,
                            "ds_user_id=" + MySharePrefs.getInstance(myInstagramActivity).getString(MySharePrefs.USERID)
                                    + "; sessionid=" + MySharePrefs.getInstance(myInstagramActivity).getString(MySharePrefs.SESSIONID));
                }
            } else {
                Utils.setToast(myInstagramActivity, getResources().getString(R.string.no_net_conn));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String getImageFilenameFromURL(String url) {
        try {
            return new File(new URL(url).getPath()).getName();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return System.currentTimeMillis() + ".png";
        }
    }

    public String getVideoFilenameFromURL(String url) {
        try {
            return new File(new URL(url).getPath()).getName();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return System.currentTimeMillis() + ".mp4";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instaObserver.dispose();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 100 && resultCode == RESULT_OK) {
                String requiredValue = data.getStringExtra("key");
                if (MySharePrefs.getInstance(myInstagramActivity).getBoolean(MySharePrefs.ISINSTALOGIN)) {
                    activityInstagramBinding.SwitchLogin.setChecked(true);
                    layoutCondition();
                    callStoriesApi();
                } else {
                    activityInstagramBinding.SwitchLogin.setChecked(false);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
    public void FBInterstitialAdsINIT() {
        fbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getResources().getString(R.string.fb_placement_interstitial_id));
        fbInterstitialAd.loadAd();
    }

    private void showInterstitial() {
        if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
            fbInterstitialAd.show();
        }
    }
    // fb interstitial ads till here

    private void callStoriesApi() {
        try {
            Utils utils = new Utils(myInstagramActivity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    activityInstagramBinding.prLoadingBar.setVisibility(View.VISIBLE);
                    commonClassForAPI.getStories(storyObserver, "ds_user_id=" + MySharePrefs.getInstance(myInstagramActivity).getString(MySharePrefs.USERID)
                            + "; sessionid=" + MySharePrefs.getInstance(myInstagramActivity).getString(MySharePrefs.SESSIONID));
                }
            } else {
                Utils.setToast(myInstagramActivity, myInstagramActivity
                        .getResources().getString(R.string.no_net_conn));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }


    }

    @Override
    public void userListClick(int position, MyTrayModel myTrayModel) {
        callStoriesDetailApi(String.valueOf(myTrayModel.getUser().getPk()));
    }

    private void callStoriesDetailApi(String UserId) {
        try {
            Utils utils = new Utils(myInstagramActivity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    activityInstagramBinding.prLoadingBar.setVisibility(View.VISIBLE);
                    commonClassForAPI.getFullDetailFeed(storyDetailObserver, UserId, "ds_user_id=" + MySharePrefs.getInstance(myInstagramActivity).getString(MySharePrefs.USERID)
                            + "; sessionid=" + MySharePrefs.getInstance(myInstagramActivity).getString(MySharePrefs.SESSIONID));
                }
            } else {
                Utils.setToast(myInstagramActivity, myInstagramActivity
                        .getResources().getString(R.string.no_net_conn));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
