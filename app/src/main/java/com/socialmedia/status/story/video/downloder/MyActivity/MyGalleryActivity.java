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
import com.socialmedia.status.story.video.downloder.databinding.ActivityGalleryBinding;
import com.socialmedia.status.story.video.downloder.MyFragment.MyAllinOneGalleryFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyFBDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyInstaDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyLikeeDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyRoposoDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MySharechatDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MySnackVideoDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyTikTokDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyTwitterDownloadedFragment;
import com.socialmedia.status.story.video.downloder.MyFragment.MyWhatsAppDowndlededFragment;
import com.socialmedia.status.story.video.downloder.MyUtils.AdsUtils;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.createFileFolder;

public class MyGalleryActivity extends AppCompatActivity {
    MyGalleryActivity myGalleryActivity;
    ActivityGalleryBinding activityGalleryBinding;
    AppLangSessionManager appLangSessionManager;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGalleryBinding = DataBindingUtil.setContentView(this, R.layout.activity_gallery);
        myGalleryActivity = this;

        mContext = MyGalleryActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Gallery Activity");

        appLangSessionManager = new AppLangSessionManager(myGalleryActivity);
        setLocale(appLangSessionManager.getLanguage());
        AdsUtils.showGoogleBannerAd(myGalleryActivity, activityGalleryBinding.adView);

        initViews();
    }

    public void initViews() {
        setupViewPager(activityGalleryBinding.viewpager);
        activityGalleryBinding.tabs.setupWithViewPager(activityGalleryBinding.viewpager);
        activityGalleryBinding.imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        for (int i = 0; i < activityGalleryBinding.tabs.getTabCount(); i++) {
            TextView tv=(TextView) LayoutInflater.from(myGalleryActivity).inflate(R.layout.custom_tab,null);
            activityGalleryBinding.tabs.getTabAt(i).setCustomView(tv);
        }

        activityGalleryBinding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        createFileFolder();
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(myGalleryActivity.getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new MyAllinOneGalleryFragment(Utils.ROOTDIRECTORYJOSHSHOW), "Josh");
        adapter.addFragment(new MyAllinOneGalleryFragment(Utils.ROOTDIRECTORYCHINGARISHOW), "Chingari");
        adapter.addFragment(new MyAllinOneGalleryFragment(Utils.ROOTDIRECTORYMITRONSHOW), "Mitron");
        adapter.addFragment(new MySnackVideoDownloadedFragment(), "Snack Video");
        adapter.addFragment(new MySharechatDownloadedFragment(), "Sharechat");
        adapter.addFragment(new MyRoposoDownloadedFragment(), "Roposo");
        adapter.addFragment(new MyInstaDownloadedFragment(), "Instagram");
        adapter.addFragment(new MyWhatsAppDowndlededFragment(), "Whatsapp");
        adapter.addFragment(new MyTikTokDownloadedFragment(), "TikTok");
        adapter.addFragment(new MyFBDownloadedFragment(), "Facebook");
        adapter.addFragment(new MyTwitterDownloadedFragment(), "Twitter");
        adapter.addFragment(new MyLikeeDownloadedFragment(), "Likee");
        adapter.addFragment(new MyAllinOneGalleryFragment(Utils.ROOTDIRECTORYMXSHOW), "MXTakaTak");
        adapter.addFragment(new MyAllinOneGalleryFragment(Utils.ROOTDIRECTORYMOJSHOW), "Moj");

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

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

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }
}
