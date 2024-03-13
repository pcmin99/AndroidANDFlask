package com.example.hello;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDialog extends Dialog {
    private TextView reviewdate;
    private TextView shopname;

    private TextView shopaddr;
    private Button shutdownClick;

    private EditText txt_contents;
    // reviewList를 클래스 변수로 선언

    private TextView recText;

    // 작성 시간
    private TextView nowCo;


    private boolean isTaskRunning = false;

    public CustomDialog(@NonNull Context context, String contents, String dates_date, String companyName, String address) {
        super(context);
        setContentView(R.layout.activity_custom_dialog);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        reviewdate = findViewById(R.id.review);
        reviewdate.setText(dates_date);

        shopname = findViewById(R.id.shopname);
        shopname.setText(companyName);

        shopaddr = findViewById(R.id.shopaddr);
        shopaddr.setText(address);

        // 사용자가 작성할 리뷰 내용
        txt_contents = findViewById(R.id.txt_contents);

        // 영수증 날짜 라는 텍스트임
        recText = findViewById(R.id.recText);

        // 현재 시간
        nowCo = (TextView) findViewById(R.id.nowCo);
        // Text에 시간 세팅
        nowCo.setText(getTime());


        shutdownClick = findViewById(R.id.btn_shutdown);
        shutdownClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTaskRunning) {
                    isTaskRunning = true; // 작업이 실행 중임을 표시
                    new SendDataToServerTask().execute(shopname.getText().toString(), txt_contents.getText().toString(), reviewdate.getText().toString(),shopaddr.getText().toString());
                } else {
                    // 이미 작업이 실행 중일 때 사용자에게 알림을 표시할 수 있음
                }
            }
        });



    }

    private String getTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String getTime = dateFormat.format(date);

        return getTime;
    }

    // 사용자가 입력한 값들
    private class SendDataToServerTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            if (params.length < 4) {
                // 전달된 파라미터의 개수가 4보다 작으면 오류 처리
                return false;
            }

            String shopname = params[0];
            String txtContents = params[1];
            String reviewDate = params[2];
            String shopaddr = params[3];

            if(txtContents == null || txtContents.trim().isEmpty()){
                Toast.makeText(getContext(), "댓글을 작성 해주세요.", Toast.LENGTH_SHORT).show();
                return true;
            }
                try {
                    // 서버 URL 설정
                    URL url = new URL("http://175.114.130.21:5111/insert_review");

                    // HTTP 연결 설정
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                    httpURLConnection.setRequestProperty("Accept", "application/json");
                    httpURLConnection.setDoOutput(true);


                    // 데이터 전송
                    String jsonInputString = "{\"shopname\": \"" + shopname + "\", \"txtContents\": \"" + txtContents + "\", \"reviewDate\": \"" + reviewDate + "\" , \"shopaddr\": \""+shopaddr+ "\"}";
                    try (OutputStream os = httpURLConnection.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = httpURLConnection.getResponseCode();
                    return responseCode == HttpURLConnection.HTTP_OK;

                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }



        }

        protected void onPostExecute(Boolean success) {
            isTaskRunning = false; // 작업이 완료되었음을 표시
            if (success) {
                Toast.makeText(getContext(), "댓글이 저장 되었습니다.", Toast.LENGTH_SHORT).show();
                // 다이얼로그를 닫는 코드 추가
                dismiss();

            } else {
                Toast.makeText(getContext(), "댓글을 다시 작성 해주세요.", Toast.LENGTH_SHORT).show();
            }
        }



    }




}
