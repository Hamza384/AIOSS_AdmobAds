package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.adapter.MyShowImagesAdapter;
import com.socialmedia.status.story.video.downloder.databinding.ActivityFullViewBinding;
import com.socialmedia.status.story.video.downloder.MyUtils.AppLangSessionManager;
import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.shareImage;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.shareImageVideoOnWhatsapp;
import static com.socialmedia.status.story.video.downloder.MyUtils.Utils.shareVideo;

public class MyFullViewActivity extends AppCompatActivity {
    private ActivityFullViewBinding activityFullViewBinding;
    private MyFullViewActivity myFullViewActivity;
    private ArrayList<File> fileArrayList;
    private int Position = 0;
    MyShowImagesAdapter myShowImagesAdapter;
    AppLangSessionManager appLangSessionManager;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFullViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_full_view);
        myFullViewActivity = this;

        mContext = MyFullViewActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Full View Activity");

        appLangSessionManager = new AppLangSessionManager(myFullViewActivity);
        setLocale(appLangSessionManager.getLanguage());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fileArrayList= (ArrayList<File>) getIntent().getSerializableExtra("ImageDataFile");
            Position = getIntent().getIntExtra("Position",0);
        }
        initViews();

    }

    public void initViews(){
        myShowImagesAdapter =new MyShowImagesAdapter(this, fileArrayList, MyFullViewActivity.this);
        activityFullViewBinding.vpView.setAdapter(myShowImagesAdapter);
        activityFullViewBinding.vpView.setCurrentItem(Position);

        activityFullViewBinding.vpView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                Position=arg0;
                System.out.println("Current position=="+Position);
            }
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
            @Override
            public void onPageScrollStateChanged(int num) {
            }
        });

        activityFullViewBinding.imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder ab = new AlertDialog.Builder(myFullViewActivity);
                ab.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        boolean b=fileArrayList.get(Position).delete();
                        if (b){
                            deleteFileAA(Position);
                        }
                    }
                });
                ab.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = ab.create();
                alert.setTitle(getResources().getString(R.string.do_u_want_to_dlt));
                alert.show();
            }
        });
        activityFullViewBinding.imShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileArrayList.get(Position).getName().contains(".mp4")){
                    shareVideo(myFullViewActivity,fileArrayList.get(Position).getPath());
                }else {
                    shareImage(myFullViewActivity,fileArrayList.get(Position).getPath());
                }
            }
        });
        activityFullViewBinding.imWhatsappShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileArrayList.get(Position).getName().contains(".mp4")){
                    shareImageVideoOnWhatsapp(myFullViewActivity,fileArrayList.get(Position).getPath(),true);
                }else {
                    shareImageVideoOnWhatsapp(myFullViewActivity,fileArrayList.get(Position).getPath(),false);
                }
            }
        });
        activityFullViewBinding.imClose.setOnClickListener(v -> {
            onBackPressed();
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        myFullViewActivity = this;
    }

    public void deleteFileAA(int position){
        fileArrayList.remove(position);
        myShowImagesAdapter.notifyDataSetChanged();
        Utils.setToast(myFullViewActivity,getResources().getString(R.string.file_deleted));
        if (fileArrayList.size()==0){
            onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
