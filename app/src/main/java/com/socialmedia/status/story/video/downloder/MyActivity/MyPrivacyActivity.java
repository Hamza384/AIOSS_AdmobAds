package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.socialmedia.status.story.video.downloder.R;
import com.google.firebase.analytics.FirebaseAnalytics;


public class MyPrivacyActivity extends AppCompatActivity {


    Toolbar mToolbar;
    TextView privacyTxt;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        mContext = MyPrivacyActivity.this;

        mContext = MyPrivacyActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Privacy Activity");


        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(null);
            mToolbar.setTitle(R.string.privacy_policy);
            mToolbar.setNavigationIcon(R.drawable.ic_back);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }


        privacyTxt = findViewById(R.id.txtPrivacy);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            privacyTxt.setText(Html.fromHtml(getResources().getString(R.string.privacypolicy), Html.FROM_HTML_MODE_COMPACT));
        } else {
            privacyTxt.setText(Html.fromHtml(getResources().getString(R.string.privacypolicy)));
        }


    }

}