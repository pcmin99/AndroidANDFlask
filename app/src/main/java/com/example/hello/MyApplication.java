// MyApplication.java
package com.example.hello;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;
    private String sessionValue;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 초기 세션 값을 설정하는 함수를 호출
        setInitialSessionValue();
    }

    public String getSessionValue() {
        return sessionValue;
    }

    public void setSessionValue(String sessionValue) {
        this.sessionValue = sessionValue;
    }

    // 초기 세션 값을 설정하는 함수
    private void setInitialSessionValue() {
        // 여기에서 JavaScript로부터 값을 가져오는 코드 작성
        // 예: 서버에서 세션 값을 가져와서 설정
        // 아래 코드는 임시로 "exampleSessionValue"로 설정하는 것입니다.
        String sessionValue = "exampleSessionValue";
        setSessionValue(sessionValue);
    }

    // JavaScript에서 세션 값을 가져와 설정합니다.
    public void setSessionValueFromJavaScript(String sessionValue) {
        setSessionValue(sessionValue);
    }
}
