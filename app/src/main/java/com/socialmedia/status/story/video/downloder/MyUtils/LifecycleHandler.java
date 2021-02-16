package com.socialmedia.status.story.video.downloder.MyUtils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {
    static String TAG = "LifecycleHandler";
    private static int resumed;
    private static int stopped;

    private static int created;
    private static int destroyed;

    private static Activity pausedActivity = null;

    public LifecycleHandler() {
        resumed = 0;
        stopped = 0;
        created = 0;
        destroyed = 0;
        pausedActivity = null;
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ++created;
    }

    public void onActivityDestroyed(Activity activity) {
        ++destroyed;

        if (created == destroyed)
            pausedActivity = null;
    }

    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    public void onActivityPaused(Activity activity) {
        pausedActivity = activity;
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        ++stopped;
    }

    public static boolean isApplicationInForeground() {
        return resumed > stopped;
    }

    public static boolean isAppRunning() {
        return created > destroyed;
    }

    public static boolean isAppRunningNotif(Activity activity) {
        if (activity.getLocalClassName().equals("AlarmNotification") && pausedActivity != null)
            return false;
        else if (created >= destroyed && activity.getLocalClassName().equals("AlarmNotification") && pausedActivity == null)
            return true;
        else
            return false;
    }
}
