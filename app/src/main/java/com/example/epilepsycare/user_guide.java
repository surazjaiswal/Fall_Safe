package com.example.epilepsycare;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

public class user_guide extends AppCompatActivity {

    private WebView webView;
    private PDFView userGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        userGuide = (PDFView) findViewById(R.id.pdfUserGuide);
        userGuide.fromAsset("user_guide_fall_safe.pdf").load();

//        final String userGuideURL = "https://drive.google.com/file/d/1rBYlSlhiWGJFjIV-C8jsMJG4RcSIWoX1/view?usp=sharing";
//        webView = (WebView) findViewById(R.id.webViewUserGuide);
//        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl(userGuideURL);
//
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
    }

//    @Override
//    public void onBackPressed() {
//        if (webView.canGoBack()) {
//            finish();
//            webView.goBack();
//
//        } else {
//            super.onBackPressed();
//        }
//    }
}