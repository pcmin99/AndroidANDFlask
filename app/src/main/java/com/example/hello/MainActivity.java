package com.example.hello;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hello.CameraActivity;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private WebView webView;
    private WebSettings webSettings;

    private long backBtnTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 네이티브 UI 요소 참조
        webView = findViewById(R.id.webvw11);

        // 웹뷰 설정
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 자바스크립트 인터페이스 추가
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // 기본 페이지 로딩( 스프링 부트 url )
        webView.loadUrl("http://175.114.130.19:8080/touro");
    }

    // 이미지를 Base64로 인코딩
    private String encodeBitmapToBase64(Bitmap imageBitmap) {


        return ""; // 인코딩 결과를 반환
    }

    // 커스텀 WebViewClient 정의
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 특정 페이지의 URL을 확인하여 동작을 수행( 스프링 부트 url )
            if (url.equals("http://175.114.130.19:8080/user/user/appReview")) {
                openAppGetImage();
                return true; // true를 반환하면 해당 URL에 대한 페이지 로딩을 WebView가 처리하지 않습니다.
            }

            // 특정 페이지가 아닌 경우에는 일반적인 처리를 진행
            return super.shouldOverrideUrlLoading(view, url);
        }

        // JavaScript를 사용하여 디바이스 유형 감지
        private boolean isMobileDevice(Context context) {
            return (context.getResources().getConfiguration().smallestScreenWidthDp < 600);
        }
    }

    // 자바스크립트 인터페이스 클래스 정의
    private class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void setSessionValue(String sessionValue) {
            // 여기에서 세션 값을 MyApplication에 설정
            MyApplication.getInstance().setSessionValue(sessionValue);
        }
    }

    // 웹뷰에서 그 전 페이지로 이동하며 더 이상 페이지가 없을 경우 웹을 종료
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;
        if (webView.canGoBack()) {
            webView.goBack();
        } else if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }


    }

    // 특정 페이지에서 다른 앱의 카메라 액티비티 열기
    private void openAppGetImage() {
        // 세션 값 가져오기
        String sessionValue = MyApplication.getInstance().getSessionValue();

        Log.d("MainActivity", "세션 값: " + sessionValue);

        // "CameraActivity" 앱을 열기 위한 인텐트 설정
        Intent intent = new Intent(this, CameraActivity.class);

        // 세션 값을 인텐트에 추가
        intent.putExtra("SESSION_VALUE", sessionValue);

        // 다른 앱으로부터 결과를 받기 위한 코드
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }




}
