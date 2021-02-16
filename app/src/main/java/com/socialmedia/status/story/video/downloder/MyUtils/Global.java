package com.socialmedia.status.story.video.downloder.MyUtils;


import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.socialmedia.status.story.video.downloder.R;

import java.util.Locale;

public class Global extends MultiDexApplication {
    public static Global appInstance;
    public FirebaseAnalytics mFirebaseAnalytics;
    public PublisherInterstitialAd mInterstitialAd;
    public PublisherAdRequest ins_adRequest;
    AppLangSessionManager appLangSessionManager;

    public static synchronized Global getInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        appInstance = this;
        LoadAds();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        registerActivityLifecycleCallbacks(new LifecycleHandler());
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_interstitial_ad));
        appLangSessionManager = new AppLangSessionManager(getApplicationContext());
        setLocale(appLangSessionManager.getLanguage());

    }

    public void LoadAds() {

        try {

            mInterstitialAd = new PublisherInterstitialAd(this);

            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_ad));

            ins_adRequest = new PublisherAdRequest.Builder()
                    .build();

            mInterstitialAd.loadAd(ins_adRequest);
        } catch (Exception e) {
        }
    }

    public boolean requestNewInterstitial() {

        try {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLoaded() {

        try {
            if (mInterstitialAd.isLoaded() && mInterstitialAd != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setLocale(String lang) {
        if (lang.equals("")) {
            lang = "en";
        }
        Log.d("Support", lang + "");
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);


    }
}
