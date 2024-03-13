    package com.example.hello;

    import android.content.DialogInterface;
    import android.content.Intent;
    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.view.WindowManager;
    import android.widget.ArrayAdapter;
    import android.widget.ListView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.CameraPosition;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.MarkerOptions;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.BufferedInputStream;
    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.OutputStream;
    import java.io.OutputStreamWriter;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;  // ArrayList import 추가
    import java.util.Date;
    import java.util.Locale;

    public class YsemapActivity extends AppCompatActivity  implements OnMapReadyCallback {

        private GoogleMap mMap;



        // 받아오는거 따로 가져 오는거 따로 이기에
        private double latitude;
        private double longitude;

        // 영수증 상호명
        private String companyName;

        // 해당 상호명 주소
        private String address;

        // 해당 상호명 리뷰 내용들
        private String receipt_review_content;

        // 해당 상호명에 리뷰들에 날짜
        private String receipt_date;

        // 해당 상호명 리뷰 적은 유저 아이디
        private String user_id;

        private ArrayList<SampleData> reviewList;

        private Adapt adapt;


        private CustomDialog  customDialog;

        // 해당 영수증 날짜
        private String dates_date;

        private TextView shopname;







        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ysemap);

            // reviewList 초기화
            reviewList = new ArrayList<>();

            ListView listView = findViewById(R.id.list);

            // adapt 객체 생성 시 reviewList 전달
            adapt = new Adapt(this, reviewList);
            listView.setAdapter(adapt);

            new SelectReceiptTask().execute();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1);
            mapFragment.getMapAsync(this);


            Intent intent = getIntent();
            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);
            companyName = intent.getStringExtra("companyName");
            address = intent.getStringExtra("address");
            dates_date = intent.getStringExtra("dates_date");

            shopname = findViewById(R.id.shopname);
            shopname.setText(companyName);




            //다이얼로그 밖의 화면은 흐리게 만들어줌
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            layoutParams.dimAmount = 0.8f;
            getWindow().setAttributes(layoutParams);


            // 홈으로 버튼
            findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(YsemapActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });


            // 팝업
            findViewById(R.id.popupbtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 팝업 창 띄우기
                    showPopupDialog();
                }
            });

        }




        // 비동기 함수 ~Post
        public class SelectReceiptTask extends AsyncTask<Void, Void, ArrayList<JSONObject>> {

            @Override
            protected ArrayList<JSONObject> doInBackground(Void... voids) {
                String serverUrl = "http://175.114.130.21:5111/select_data";
                ArrayList<JSONObject> dataList = new ArrayList<>();

                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    // 서버 응답 읽기
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // 서버 응답을 문자열로 반환
                    JSONObject jsonObject = new JSONObject(result.toString());
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        dataList.add(dataArray.getJSONObject(i));
                    }

                } catch (IOException | JSONException e) {
                    Toast.makeText(YsemapActivity.this, "리뷰를 가져오는데 실패 했습니다.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                return dataList;
            }

            protected void onPostExecute(ArrayList<JSONObject> dataList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reviewList.clear(); // 리스트 비우기

                        for (JSONObject item : dataList) {
                            try {
                                receipt_review_content = item.getString("receipt_review_content");
                                String dateString = item.getString("receipt_date");
                                SimpleDateFormat serverFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                                Date date = serverFormat.parse(dateString);
                                SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
                                receipt_date = displayFormat.format(date);
                                user_id = item.getString("user_id");

                                Log.d("DataFetcherTask", "user_id: " + user_id);
                                Log.d("DataFetcherTask", "receipt_review_content: " + receipt_review_content);
                                Log.d("DataFetcherTask", "receipt_date: " + receipt_date);
                                Log.d("DataFetcherTask", "reviewList: " + reviewList);

                                reviewList.add(new SampleData(user_id, receipt_review_content, receipt_date));
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        // 어댑터에게 알림을 보냄
                        adapt.notifyDataSetChanged();
                        onMapReady(mMap);
                    }
                });
            }



        }



        private void showPopupDialog() {
            customDialog = new CustomDialog(this, "", dates_date, companyName,address);
            customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // 팝업이 닫힌 후에 데이터를 다시 가져오기
                    new SelectReceiptTask().execute();

                }
            });
            customDialog.show();
        }

//        private void updateUI() {
//
//            if (reviewList != null) {
//                reviewList.clear();
//
//                // 데이터를 가져온 후에 adapt 객체를 생성하고 데이터를 설정합니다.
//                adapt = new Adapt(YsemapActivity.this, reviewList);
//                adapt.notifyDataSetChanged();
//            }
//
//        }


        // 구글 map api 열기위해
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
