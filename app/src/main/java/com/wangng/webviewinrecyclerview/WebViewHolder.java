package com.wangng.webviewinrecyclerview;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by 小爱 on 2017/7/20.
 */

public class WebViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "WebViewHolder";

    private boolean mIsLoading;
    String url = "https://m.baidu.com/from=1012852s/s?word=%E5%B7%B4%E5%8E%98%E5%B2%9B&ts=0307421&t_kt=0&ie=utf-8&fm_kl=b26a42b666&" +
            "rsv_iqid=1090308116&rsv_t=c2dbEJhJPefAJ0vOMwixV" +
            "WqB6iOhhVxxQLyQquz%252F5XUgPAfwO9liAVJ%252BXki86eQ&sa=is_4&rsv_pq=1090308116&rsv_sug4=10218&inputT=3904&ss=100&rq=%E5%B7%B4";
    private final WebView webView;

    public WebViewHolder(View itemView) {
        super(itemView);
        webView = (WebView) itemView.findViewById(R.id.web_view);

        //当页面正在加载时，禁止链接的点击事件
        webView.setOnTouchListener(new WebViewTouchListener());

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(url);

    }

    private class WebViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return !mIsLoading;
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //判断重定向的方式一
//            WebView.HitTestResult hitTestResult = view.getHitTestResult();
//            if(hitTestResult == null) {
//                return false;
//            }
//            if(hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
//                return false;
//            }

            //判断重定向的方式二
            if(mIsLoading) {
                return false;
            }

            if(url != null && url.startsWith("http")) {
                webView.loadUrl(url);
                return true;
            } else {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    view.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mIsLoading = true;
            Log.d(TAG, "onPageStarted");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mIsLoading = false;
            Log.d(TAG, "onPageFinished");
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
    }
}
