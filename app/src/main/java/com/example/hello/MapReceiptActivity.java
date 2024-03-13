package com.example.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapReceiptActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double latitude;
    private double longitude;
    private String companyName;

    private String address;

    private String dates_date;

    // 기본
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapreceipt);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 서버로 주소 정보 요청
        new RequestAddressTask().execute();

        // 아니요 클릭
        findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Use the context of the current activity
                Intent intent = new Intent(MapReceiptActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        // 해당 하는 주소가 맞을시
        findViewById(R.id.mapYes).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Use the context of the current activity
                Intent intent = new Intent(MapReceiptActivity.this, YsemapActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("companyName", companyName);
                intent.putExtra("address", address);
                intent.putExtra("dates_date", dates_date);
                onMapReady(mMap);
                startActivity(intent);
            }
        });
    }

    // 비동기 
    private class RequestAddressTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject  doInBackground(Void... voids) {
            // 서버 주소
            String serverUrl = "http://175.114.130.21:5111/get_data";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                // 서버 응답 읽기
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                // 서버 응답을 문자열로 반환
                return new JSONObject(result.toString());

            } catch (IOException | JSONException e) {
                //Toast.makeText(MapReceiptActivity.this, "해당 사진을 다시 검색 해주세요.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    latitude = jsonObject.getDouble("latitude");
                    longitude = jsonObject.getDouble("longitude");
                    companyName = jsonObject.getString("company_name");
                    address = jsonObject.getString("address");
                    dates_date = jsonObject.getString("dates_date");

                    // 백슬래시(\) 제거
                    dates_date = dates_date.replaceAll("\\\\", "");

                    // 대괄호 []와 큰 따옴표 "" 제거
                    dates_date = dates_date.replaceAll("[\\[\\]\"]", "");


                    // 받은 데이터를 로그로 출력
                    Log.d("DataFetcherTask", "위도: " + latitude);
                    Log.d("DataFetcherTask", "경도: " + longitude);
                    Log.d("DataFetcherTask", "Company Name: " + companyName);
                    Log.d("DataFetcherTask", "dates_date: " + dates_date);

                    // 지도 준비가 완료되면 onMapReady 호출
                    onMapReady(mMap);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때 호출
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        LatLng location = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location);
        markerOptions.title(companyName);
        markerOptions.snippet(address);


        mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(location).zoom(17).tilt(48).build()));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 지도 유형 설정
        googleMap.setBuildingsEnabled(true);
    }



}
