package com.socialmedia.status.story.video.downloder.MyActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.multidex.MultiDex;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.socialmedia.status.story.video.downloder.MyListners.InterstitialAdListener;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.ClipboardListener;
import com.socialmedia.status.story.video.downloder.MyUtils.Global;
import com.socialmedia.status.story.video.downloder.MyUtils.MySharePrefs;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;

import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, InterstitialAdListener {
    MainActivity mainActivity;
    ActivityMainBinding activityMainBinding;
    boolean doubleBackToExitPressedOnce = false;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    String copyKey = "";
    String copyValue = "";
    AppLangSessionManager sessionManager;
    FirebaseAnalytics mFirebaseAnalytics;
    Context mContext;
    //alert dialog
    ViewDialog alertDialog = new ViewDialog();
    //in app review
    ReviewManager manager;
    ReviewInfo reviewInfo;
    //admob intrestial
    InterstitialAd interstitialAd;
    private ClipboardManager clipboardManager;

    public static String extractLinks(String text) {
        Matcher m = Patterns.WEB_URL.matcher(text);
        String url = "";
        while (m.find()) {
            url = m.group();
            Log.d("New URL", "URL extracted: " + url);
            break;
        }
        return url;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainActivity = this;
        mContext = MainActivity.this;
        sessionManager = new AppLangSessionManager(mainActivity);
        mContext = MainActivity.this;

        MultiDex.install(mContext);
        MobileAds.initialize(mContext, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //in app review
        manager = ReviewManagerFactory.create(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "MainActivity");
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainActivity = this;
        clipboardManager = (ClipboardManager) mainActivity.getSystemService(CLIPBOARD_SERVICE);
    }

    public void initViews() {
        clipboardManager = (ClipboardManager) mainActivity.getSystemService(CLIPBOARD_SERVICE);
        if (mainActivity.getIntent().getExtras() != null) {
            for (String key : mainActivity.getIntent().getExtras().keySet()) {
                copyKey = key;
                String value = mainActivity.getIntent().getExtras().getString(copyKey);
                if (copyKey.equals("android.intent.extra.TEXT")) {
                    copyValue = mainActivity.getIntent().getExtras().getString(copyKey);
                    copyValue = extractLinks(copyValue);
                    copyText(value);
                } else {
                    copyValue = "";
                    copyText(value);
                }
            }
        }
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(new ClipboardListener() {
                @Override
                public void onPrimaryClipChanged() {
                    try {
                        showSocialNotifications(Objects.requireNonNull(clipboardManager.getPrimaryClip().getItemAt(0).getText()).toString());
                    } catch (
                            Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions(0);
        }

        activityMainBinding.rvLikee.setOnClickListener(this);
        activityMainBinding.rvInsta.setOnClickListener(this);
        activityMainBinding.rvWhatsApp.setOnClickListener(this);
        activityMainBinding.rvTikTok.setOnClickListener(this);
        activityMainBinding.rvFB.setOnClickListener(this);
        activityMainBinding.rvTwitter.setOnClickListener(this);
        activityMainBinding.rvGallery.setOnClickListener(this);
        activityMainBinding.rvAbout.setOnClickListener(this);
        activityMainBinding.rvShareApp.setOnClickListener(this);
        activityMainBinding.rvRateApp.setOnClickListener(this);
        activityMainBinding.rvMoreApp.setOnClickListener(this);
        activityMainBinding.rvSnack.setOnClickListener(this);
        activityMainBinding.rvShareChat.setOnClickListener(this);
        activityMainBinding.rvRoposo.setOnClickListener(this);
        activityMainBinding.rvJosh.setOnClickListener(this);
        activityMainBinding.rvChingari.setOnClickListener(this);
        activityMainBinding.rvMitron.setOnClickListener(this);
        activityMainBinding.rvMoj.setOnClickListener(this);
        activityMainBinding.rvMX.setOnClickListener(this);

        //TODO :  Change Language Dialog Open
        activityMainBinding.rvChangeLang.setOnClickListener(v -> {
            final BottomSheetDialog dialogSortBy = new BottomSheetDialog(MainActivity.this, R.style.SheetDialog);
            dialogSortBy.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogSortBy.setContentView(R.layout.dialog_language);
            final TextView tv_english = dialogSortBy.findViewById(R.id.tv_english);
            final TextView tv_hindi = dialogSortBy.findViewById(R.id.tv_hindi);
            final TextView tv_cancel = dialogSortBy.findViewById(R.id.tv_cancel);
            final TextView tvArabic = dialogSortBy.findViewById(R.id.tvArabic);
            dialogSortBy.show();
            tv_english.setOnClickListener(view -> {
                setLocale("en");
                sessionManager.setLanguage("en");
            });
            tv_hindi.setOnClickListener(view -> {
                setLocale("hi");
                sessionManager.setLanguage("hi");
            });
            tvArabic.setOnClickListener(view -> {
                setLocale("ar");
                sessionManager.setLanguage("ar");
            });
            tv_cancel.setOnClickListener(view -> dialogSortBy.dismiss());

        });

        createFileFolder();

    }

    private void copyText(String CopiedText) {
        try {
            if (CopiedText.contains("likee")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1000);
                } else {
                    callLikeeActivity();
                }
            } else if (CopiedText.contains("instagram.com")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1010);
                } else {
                    callInstaActivity();
                }
            } else if (CopiedText.contains("facebook.com") || CopiedText.contains("fb")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1040);
                } else {
                    callFacebookActivity();
                }
            } else if (CopiedText.contains("tiktok.com")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1030);
                } else {
                    callTikTokActivity();
                }
            } else if (CopiedText.contains("twitter.com")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1060);
                } else {
                    callTwitterActivity();
                }
            } else if (CopiedText.contains("sharechat")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1070);
                } else {
                    callShareChatActivity();
                }
            } else if (CopiedText.contains("roposo")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1080);
                } else {
                    callRoposoActivity();
                }
            } else if (CopiedText.contains("snackvideo") || CopiedText.contains("sck.io")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1090);
                } else {
                    callSnackVideoActivity();
                }
            } else if (CopiedText.contains("josh")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1100);
                } else {
                    callJoshActivity();
                }
            } else if (CopiedText.contains("chingari")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1110);
                } else {
                    callChingariActivity();
                }
            } else if (CopiedText.contains("mitron")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1120);
                } else {
                    callMitronActivity();
                }
            } else if (CopiedText.contains("mxtakatak")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1130);
                } else {
                    callMXActivity();
                }
            } else if (CopiedText.contains("moj")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(1140);
                } else {
                    callMojActivity();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        Intent i = null;

        switch (view.getId()) {
            case R.id.rvLikee:
                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1000);
                            } else {
                                callLikeeActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1000);
                                    } else {
                                        callLikeeActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1000);
                        } else {
                            callLikeeActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }
                break;
            case R.id.rvInsta:

                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1010);
                            } else {
                                callInstaActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1010);
                                    } else {
                                        callInstaActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1010);
                        } else {
                            callInstaActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;

            case R.id.rvWhatsApp:
                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1020);
                            } else {
                                callWhatsappActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1020);
                                    } else {
                                        callWhatsappActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1020);
                        } else {
                            callWhatsappActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }

                break;
            case R.id.rvTikTok:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1030);
                            } else {
                                callTikTokActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1030);
                                    } else {
                                        callTikTokActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1030);
                        } else {
                            callTikTokActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvFB:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1040);
                            } else {
                                callFacebookActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1040);
                                    } else {
                                        callFacebookActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1040);
                        } else {
                            callFacebookActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvGallery:


                try {

                    if (!Global.getInstance().requestNewInterstitial()) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1050);
                        } else {
                            callGalleryActivity();
                        }
                    } else {

                        Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();

                                Global.getInstance().mInterstitialAd.setAdListener(null);
                                Global.getInstance().mInterstitialAd = null;
                                Global.getInstance().ins_adRequest = null;
                                Global.getInstance().LoadAds();
                                if (Build.VERSION.SDK_INT >= 23) {
                                    checkPermissions(1050);
                                } else {
                                    callGalleryActivity();
                                }


                            }

                            @Override
                            public void onAdFailedToLoad(int i) {
                                super.onAdFailedToLoad(i);
                            }

                            @Override
                            public void onAdLoaded() {
                                super.onAdLoaded();
                            }
                        });
                    }


                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvTwitter:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1060);
                            } else {
                                callTwitterActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1060);
                                    } else {
                                        callTwitterActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1060);
                        } else {
                            callTwitterActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvAbout:


                i = new Intent(mainActivity, MyAboutUsActivity.class);
                startActivity(i);
                break;
            case R.id.rvShareApp:


                Utils.shareApp(mainActivity);
                break;

            case R.id.rvRateApp:
                Utils.rateApp(mainActivity);
                break;
            case R.id.rvMoreApp:
                Utils.moreApp(mainActivity);
                break;
            case R.id.rvShareChat:

                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1070);
                            } else {
                                callShareChatActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1070);
                                    } else {
                                        callShareChatActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1070);
                        } else {
                            callShareChatActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvRoposo:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1080);
                            } else {
                                callRoposoActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1080);
                                    } else {
                                        callRoposoActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1080);
                        } else {
                            callRoposoActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvSnack:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1090);
                            } else {
                                callSnackVideoActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1090);
                                    } else {
                                        callSnackVideoActivity();
                                    }

                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1090);
                        } else {
                            callSnackVideoActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvJosh:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1100);
                            } else {
                                callJoshActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1100);
                                    } else {
                                        callJoshActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1100);
                        } else {
                            callJoshActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvChingari:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1110);
                            } else {
                                callChingariActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1110);
                                    } else {
                                        callChingariActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1110);
                        } else {
                            callChingariActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvMitron:

                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1120);
                            } else {
                                callMitronActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1120);
                                    } else {
                                        callMitronActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1120);
                        } else {
                            callMitronActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
            case R.id.rvMX:

                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1130);
                            } else {
                                callMXActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1130);
                                    } else {
                                        callMXActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1130);
                        } else {
                            callMXActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;

            case R.id.rvMoj:


                try {
                    if (showAdOnCount(MySharePrefs.getInstance(mContext).getIntPref(Utils.KEY_AD_COUNT, 0))) {
                        if (!Global.getInstance().requestNewInterstitial()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                checkPermissions(1140);
                            } else {
                                callMojActivity();
                            }
                        } else {

                            Global.getInstance().mInterstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();

                                    Global.getInstance().mInterstitialAd.setAdListener(null);
                                    Global.getInstance().mInterstitialAd = null;
                                    Global.getInstance().ins_adRequest = null;
                                    Global.getInstance().LoadAds();
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        checkPermissions(1140);
                                    } else {
                                        callMojActivity();
                                    }


                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            });
                        }

                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions(1140);
                        } else {
                            callMojActivity();
                        }
                    }
                } catch (NullPointerException e) {
                    e.getStackTrace();
                }


                break;
        }
    }

    public void callJoshActivity() {
        Intent i = new Intent(mainActivity, MyJoshActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callChingariActivity() {
        Intent i = new Intent(mainActivity, MyChingariActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callMitronActivity() {
        Intent i = new Intent(mainActivity, MyMitronActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callMXActivity() {
        Intent i = new Intent(mainActivity, MyMXTakaTakActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callMojActivity() {
        Intent i = new Intent(mainActivity, MyMojActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callLikeeActivity() {
        Intent i = new Intent(mainActivity, MyLikeeActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callInstaActivity() {
        Intent i = new Intent(mainActivity, MyInstagramActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callWhatsappActivity() {
        Intent i = new Intent(mainActivity, MyWhatsappActivity.class);
        startActivity(i);
    }

    public void callTikTokActivity() {
        Intent i = new Intent(mainActivity, MyTikTokActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callFacebookActivity() {
        Intent i = new Intent(mainActivity, MyFacebookActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);

    }

    public void callTwitterActivity() {
        Intent i = new Intent(mainActivity, MyTwitterActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callGalleryActivity() {
        Intent i = new Intent(mainActivity, MyGalleryActivity.class);
        startActivity(i);
    }

    public void callRoposoActivity() {
        Intent i = new Intent(mainActivity, MyRoposoActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callShareChatActivity() {
        Intent i = new Intent(mainActivity, MyShareChatActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void callSnackVideoActivity() {
        Intent i = new Intent(mainActivity, MySnackVideoActivity.class);
        i.putExtra("CopyIntent", copyValue);
        startActivity(i);
    }

    public void showSocialNotifications(String Text) {
        if (Text.contains("instagram.com") || Text.contains("facebook.com") || Text.contains("fb") || Text.contains("tiktok.com")
                || Text.contains("twitter.com") || Text.contains("likee")
                || Text.contains("sharechat") || Text.contains("roposo") || Text.contains("snackvideo") || Text.contains("sck.io")
                || Text.contains("chingari") || Text.contains("myjosh") || Text.contains("mitron")) {
            Intent intent = new Intent(mainActivity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Notification", Text);
            PendingIntent pendingIntent = PendingIntent.getActivity(mainActivity, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationManager notificationManager = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(getResources().getString(R.string.app_name),
                        getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                mChannel.enableLights(true);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(mChannel);
            }
            NotificationCompat.Builder notificationBuilder;
            notificationBuilder = new NotificationCompat.Builder(mainActivity, getResources().getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(getResources().getColor(R.color.black))
                    .setLargeIcon(BitmapFactory.decodeResource(mainActivity.getResources(),
                            R.mipmap.ic_launcher))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle("Copied text")
                    .setContentText(Text)
                    .setChannelId(getResources().getString(R.string.app_name))
                    .setFullScreenIntent(pendingIntent, true);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    private boolean checkPermissions(int type) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(mainActivity, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mainActivity,
                    listPermissionsNeeded.toArray(new
                            String[listPermissionsNeeded.size()]), type);
            return false;
        } else {
            if (type == 1000) {
                callLikeeActivity();
            } else if (type == 1010) {
                callInstaActivity();
            } else if (type == 1020) {
                callWhatsappActivity();
            } else if (type == 1030) {
                callTikTokActivity();
            } else if (type == 1040) {
                callFacebookActivity();
            } else if (type == 1050) {
                callGalleryActivity();
            } else if (type == 1060) {
                callTwitterActivity();
            } else if (type == 1070) {
                callShareChatActivity();
            } else if (type == 1080) {
                callRoposoActivity();
            } else if (type == 1090) {
                callSnackVideoActivity();
            } else if (type == 1100) {
                callJoshActivity();
            } else if (type == 1110) {
                callChingariActivity();
            } else if (type == 1120) {
                callMitronActivity();
            } else if (type == 1130) {
                callMXActivity();
            } else if (type == 1140) {
                callMojActivity();
            }

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callLikeeActivity();
            }
        } else if (requestCode == 1010) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callInstaActivity();
            }
        } else if (requestCode == 1020) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callWhatsappActivity();
            }
        } else if (requestCode == 1030) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callTikTokActivity();
            }
        } else if (requestCode == 1040) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callFacebookActivity();
            }
        } else if (requestCode == 1050) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callGalleryActivity();
            }
        } else if (requestCode == 1060) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callTwitterActivity();
            }
        } else if (requestCode == 1070) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callShareChatActivity();
            }
        } else if (requestCode == 1080) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callRoposoActivity();
            }
        } else if (requestCode == 1090) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callSnackVideoActivity();
            }
        } else if (requestCode == 1100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callJoshActivity();
            }
        } else if (requestCode == 1110) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callChingariActivity();
            }
        } else if (requestCode == 1120) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callMitronActivity();
            }
        } else if (requestCode == 1130) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callMXActivity();
            }
        } else if (requestCode == 1140) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callMojActivity();
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        this.doubleBackToExitPressedOnce = true;
        // dialog
        alertDialog.showDialog(MainActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    public void setLocale(String lang) {

        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        Intent refresh = new Intent(MainActivity.this, MainActivity.class);
        startActivity(refresh);
        finish();
    }

    private void Review(boolean isBackPressed) {
        manager.requestReviewFlow().addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(MainActivity.this, reviewInfo);
                    flow.addOnFailureListener(new com.google.android.play.core.tasks.OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {

                        }
                    });
                    flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (isBackPressed) {
                                finishAffinity();
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean showAdOnCount(int count) {

        if (count >= 3) {
            showInterstitial();
            MySharePrefs.getInstance(mContext).savePref(Utils.KEY_AD_COUNT, 0);
            return true;
        } else {
            MySharePrefs.getInstance(mContext).savePref(Utils.KEY_AD_COUNT, count+1);
            return false;
        }

    }

    private void showInterstitial() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    @Override
    public void adLoaded() {

    }

    @Override
    public void adClosed() {

    }

    @Override
    public void AdFailed() {

    }

    public class ViewDialog {

        public void showDialog(Activity activity) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_alertdialog_layout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            FrameLayout mDialogNo = dialog.findViewById(R.id.frmNo);
            mDialogNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            FrameLayout mDialogOk = dialog.findViewById(R.id.frmOk);
            mDialogOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Review(true);
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }


}
