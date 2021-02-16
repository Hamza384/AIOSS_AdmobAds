package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.databinding.ActivityWhatsappBinding;
import com.socialmedia.status.story.video.downloder.MyFragment.MyWhatsappImageFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyWhatsappVideoFragment;
import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;

public class MyWhatsappActivity extends AppCompatActivity {
    AppLangSessionManager appLangSessionManager;
    private ActivityWhatsappBinding activityWhatsappBinding;
    private MyWhatsappActivity myWhatsappActivity;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWhatsappBinding = DataBindingUtil.setContentView(this, R.layout.activity_whatsapp);
        myWhatsappActivity = this;

        mContext = MyWhatsappActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Whatsapp Activity");

        createFileFolder();


        appLangSessionManager = new AppLangSessionManager(myWhatsappActivity);
        setLocale(appLangSessionManager.getLanguage());

        AdsUtils.showGoogleBannerAd(mContext,activityWhatsappBinding.adView);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myWhatsappActivity = this;
    }

    private void initViews() {
        setupViewPager(activityWhatsappBinding.viewpager);
        activityWhatsappBinding.tabs.setupWithViewPager(activityWhatsappBinding.viewpager);
        activityWhatsappBinding.imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        for (int i = 0; i < activityWhatsappBinding.tabs.getTabCount(); i++) {
            TextView tv = (TextView) LayoutInflater.from(myWhatsappActivity).inflate(R.layout.custom_tab, null);
            activityWhatsappBinding.tabs.getTabAt(i).setCustomView(tv);
        }

        activityWhatsappBinding.LLOpenWhatsapp.setOnClickListener(v -> {
            Utils.OpenApp(myWhatsappActivity, "com.whatsapp");
        });
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(myWhatsappActivity.getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new MyWhatsappImageFragment(), getResources().getString(R.string.images));
        adapter.addFragment(new MyWhatsappVideoFragment(), getResources().getString(R.string.videos));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);

    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);


    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}
