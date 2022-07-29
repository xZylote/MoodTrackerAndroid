package com.example.logintest;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Java class for visualization
 */

public class VisualizeDayActivity extends AppCompatActivity {

    /**
     * Create, setup the WebView to load the visualization site
     * The visualization process and options are handled by the hosted site
     * @param savedInstanceState
     */

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize_day);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        WebView webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setInitialScale(1);
        Intent intent = getIntent();
        String startDate = intent.getStringExtra("DATE");
        LocalDate endDateDate = LocalDate.parse(startDate.replace("/", "-")).plusDays(1);
        String endDate = endDateDate.format(DateTimeFormatter.ISO_LOCAL_DATE).replace("-", "/");
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                System.out.println("hello");
                return false;
            }
        });
        webView.loadUrl("https://m.hay.li/hidden.html?q=" + user.getUid() + "&s=" + startDate + "&e=" + endDate);
        webView.setInitialScale(90);
    }

}

