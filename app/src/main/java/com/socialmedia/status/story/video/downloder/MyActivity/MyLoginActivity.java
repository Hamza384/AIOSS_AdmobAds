package com.socialmedia.status.story.video.downloder.MyActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.socialmedia.status.story.video.downloder.MyUtils.Utils;
import com.socialmedia.status.story.video.downloder.R;
import com.socialmedia.status.story.video.downloder.databinding.ActivityLoginBinding;
import com.socialmedia.status.story.video.downloder.MyUtils.MySharePrefs;
import com.google.firebase.analytics.FirebaseAnalytics;


public class MyLoginActivity extends AppCompatActivity {
    ActivityLoginBinding activityLoginBinding;
    MyLoginActivity myLoginActivity;
    private String cookies;
    Context mContext;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        myLoginActivity = this;

        mContext = MyLoginActivity.this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Utils.sendAnalytics(mFirebaseAnalytics, "My Login Activity");


        loadPage();
        activityLoginBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPage();
            }
        });

    }

    public void loadPage() {
        activityLoginBinding.webView.getSettings().setJavaScriptEnabled(true);
        activityLoginBinding.webView.clearCache(true);
        activityLoginBinding.webView.setWebViewClient(new MyBrowser());
        CookieSyncManager.createInstance(myLoginActivity);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        activityLoginBinding.webView.loadUrl("https://www.instagram.com/accounts/login/");
        activityLoginBinding.webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activityLoginBinding.swipeRefreshLayout.setRefreshing(progress != 100);
            }
        });
    }

    public String getCookie(String siteName, String CookieName) {
        String CookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if (cookies != null && !cookies.isEmpty()) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                if (ar1.contains(CookieName)) {
                    String[] temp1 = ar1.split("=");
                    CookieValue = temp1[1];
                    break;
                }
            }
        }
        return CookieValue;
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView webView, String str) {
            super.onLoadResource(webView, str);
        }

        @Override
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            cookies = CookieManager.getInstance().getCookie(str);
            try {
                String session_id = getCookie(str, "sessionid");
                String csrftoken = getCookie(str, "csrftoken");
                String userid = getCookie(str, "ds_user_id");
                if (session_id != null && csrftoken != null && userid != null) {
                    MySharePrefs.getInstance(myLoginActivity).putString(MySharePrefs.COOKIES, cookies);
                    MySharePrefs.getInstance(myLoginActivity).putString(MySharePrefs.CSRF, csrftoken);
                    MySharePrefs.getInstance(myLoginActivity).putString(MySharePrefs.SESSIONID, session_id);
                    MySharePrefs.getInstance(myLoginActivity).putString(MySharePrefs.USERID, userid);
                    MySharePrefs.getInstance(myLoginActivity).putBoolean(MySharePrefs.ISINSTALOGIN, true);

                    activityLoginBinding.webView.destroy();
                    Intent intent = new Intent();
                    intent.putExtra("result", "result");
                    setResult(RESULT_OK, intent);
                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onReceivedError(WebView webView, int i, String str, String str2) {
            super.onReceivedError(webView, i, str, str2);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
            return super.shouldInterceptRequest(webView, webResourceRequest);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return super.shouldOverrideUrlLoading(webView, webResourceRequest);
        }
    }
}