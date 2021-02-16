package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.databinding.ActivityAboutUsBinding;
import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;

import java.util.Locale;

public class MyAboutUsActivity extends AppCompatActivity {

    ActivityAboutUsBinding activityAboutUsBinding;
    AppLangSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAboutUsBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_us);

        sessionManager = new AppLangSessionManager(MyAboutUsActivity.this);
        setLocale(sessionManager.getLanguage());

        AdsUtils.showFBBannerAd(MyAboutUsActivity.this, activityAboutUsBinding.bannerContainer);


        activityAboutUsBinding.RLPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MyAboutUsActivity.this, MyPrivacyActivity.class);
                startActivity(i);
            }
        });
        activityAboutUsBinding.imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}
