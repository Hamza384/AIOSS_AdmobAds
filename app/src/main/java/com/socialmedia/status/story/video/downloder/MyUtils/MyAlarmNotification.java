package com.socialmedia.status.story.video.downloder.MyUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.socialmedia.status.story.video.downloder.MyActivity.MySplashScreen;


public class MyAlarmNotification extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        int wordNo = getIntent().getIntExtra("INDEX", 0);
        Intent intent = new Intent(MyAlarmNotification.this, MySplashScreen.class);
        intent.putExtra("INDEX", wordNo);
        intent.putExtra("FROM_NOTIF", true);
        startActivity(intent);
        finish();
    }
}