package com.example.hello;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.hello.MapReceiptActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    String imagePathGallery;  // 갤러리 이미지 경로
    String imagePathCamera;   // 카메라 이미지 경로
    final int CAMERA = 100;
    final int GALLERY = 101;
    Intent intent;
    ImageView imageView;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat imageDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.iv_main);


        // 카메라 및 외부 저장소 쓰기 권한이 있는지 확인
        boolean hasCamPerm = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasWritePerm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasCamPerm || !hasWritePerm)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        // 카메라 버튼 클릭 이벤트 처리
                findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("QueryPermissionsNeeded")
                    @Override
                    public void onClick(View view) {
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            File imageFile = null;
                            try {
                                imageFile = createImageFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (imageFile != null) {
                                Uri imageUri = FileProvider.getUriForFile(getApplicationContext(),
                                        "com.example.hello.fileprovider",
                                        imageFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                                // 이미지를 서버로 전송할 때 사용할 이름 설정
                                intent.putExtra("imagePath", imageFile.getAbsolutePath());

                                startActivityForResult(intent, CAMERA);
                            }
                        }
                    }
                });

        // 갤러리 버튼 클릭 이벤트 처리
        findViewById(R.id.btn_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY);
            }
        });

        // 검색 버튼
        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이미지를 서버로 전송하면서 애니메이션 시작
                startScanningAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // 애니메이션 시작 직전에 이미지를 서버로 전송
                        if (imagePathGallery != null && !imagePathGallery.isEmpty() ||
                                imagePathCamera != null && !imagePathCamera.isEmpty()) {
                            Bitmap galleryBitmap = BitmapFactory.decodeFile(imagePathGallery);
                            Bitmap cameraBitmap = BitmapFactory.decodeFile(imagePathCamera);
                            String sessionValue = MyApplication.getInstance().getSessionValue();

                            // 이미지를 비동기적으로 서버로 전송
                            new ImageUploaderTask(sessionValue, new ImageUploaderTask.UploadTaskListener() {
                                @Override
                                public void onUploadComplete() {
                                    // 이미지 전송이 완료되면 애니메이션 시작
                                    Log.d("ImageInfo", "Gallery Image Path: " + imagePathGallery);
                                    Log.d("ImageInfo", "Camera Image Path: " + imagePathCamera);
                                }

                                @Override
                                public void onUploadError() {
                                    // 이미지 전송 중 오류가 발생한 경우에 대한 처리
                                    Log.e("ImageUploaderTask", "Error: Image upload failed");
                                }
                            }).execute(galleryBitmap, cameraBitmap);
                        } else {
                            // 이미지 경로가 없는 경우에 대한 처리 (로그 또는 알림 등)
                            Log.e("ImageUploaderTask", "Error: Image path not provided");
                            Toast.makeText(CameraActivity.this, "이미지를 다시 선택 해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        navigateToNextScreen();
                    }
                });
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            switch (requestCode) {
                case GALLERY:
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        imagePathGallery = cursor.getString(index);
                        bitmap = BitmapFactory.decodeFile(imagePathGallery);
                        cursor.close();
                    }

                    // InputStream으로 이미지 세팅하기
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        imageView.setImageBitmap(rotateImageIfRequired(imagePathGallery, bitmap));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CAMERA:
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 10;

                    bitmap = BitmapFactory.decodeFile(imagePath, options);

                    // 카메라로 촬영한 이미지의 경로 저장
                    imagePathCamera = imagePath;
                    imageView.setImageBitmap(rotateImageIfRequired(imagePathCamera, bitmap));
                    break;
            }
        }
    }

    // 이미지 파일 생성
    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        String timeStamp = imageDate.format(new Date());
        String fileName = "IMAGE_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(fileName, ".jpg", storageDir);
        imagePath = file.getAbsolutePath();
        return file;
    }


    // 애니매이션 함수
    private void startScanningAnimation(final AnimatorListenerAdapter listener) {
        LottieAnimationView lottieAnimationView = findViewById(R.id.lotti);
        // 이미지를 360도 회전하면서 스캐닝 효과를 주는 애니메이션
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 0f);


        rotationAnimator.setDuration(12000); // 애니메이션 진행 시간 (12초)
        rotationAnimator.addListener(listener);

        lottieAnimationView.playAnimation();

        // 이미지 회전 애니메이션 시작
        rotationAnimator.start();
    }


    private void navigateToNextScreen() {

        // 여기에서 MainActivity로 복귀하는 코드를 추가
        Intent intent = new Intent(CameraActivity.this, MapReceiptActivity.class);
        startActivity(intent);
    }

    
    
    

    
    // 이미지 회전때문에 선언한 함수
    private Bitmap rotateImageIfRequired(String imagePath, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int rotateAngle = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateAngle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateAngle = 270;
                    break;
            }

            if (rotateAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotateAngle);

                // 회전된 이미지 반환
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 회전이 필요 없는 경우 원본 이미지 반환
        return bitmap;
    }



}
