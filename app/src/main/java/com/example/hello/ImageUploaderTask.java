package com.example.hello;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// ImageUploaderTask 클래스
public class ImageUploaderTask extends AsyncTask<Bitmap, Void, String> {

    private String sessionValue;
    private UploadTaskListener uploadTaskListener;

    // UploadTaskListener 인터페이스 정의
    public interface UploadTaskListener {
        void onUploadComplete();
        void onUploadError();
    }

    // 생성자에 UploadTaskListener 추가
    public ImageUploaderTask(String sessionValue, UploadTaskListener uploadTaskListener) {
        this.sessionValue = sessionValue;
        this.uploadTaskListener = uploadTaskListener;
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        try {
            // 전달된 비트맵 이미지들을 하나씩 처리
            for (Bitmap imageBitmap : bitmaps) {
                // 서버에 이미지 업로드
                URL url = new URL("http://175.114.130.21:5111/img");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/octet-stream");

                // 세션 값 설정
                connection.setRequestProperty("Session", sessionValue);

                connection.setDoOutput(true);

                byte[] imageBytes = getBytesFromBitmap(imageBitmap);

                // 서버로 이미지 바이트 전송
                OutputStream os = connection.getOutputStream();
                os.write(imageBytes);
                os.flush();
                os.close();

                // 서버 응답 코드 확인
                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return "Failed to upload image. Response code: " + responseCode;
                }
            }

            return "All images uploaded successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception occurred: " + e.getMessage();
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return new byte[0];
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    protected void onPostExecute(String result) {
        // 서버 응답 결과(result)를 처리하거나 로그로 출력할 수 있습니다.
        Log.d("ImageUploaderTask", "Result: " + result);


        // 이미지 업로드 완료 시 리스너 호출
        if (result.startsWith("All images uploaded")) {
            uploadTaskListener.onUploadComplete();
        } else {
            uploadTaskListener.onUploadError();
        }
    }
}
