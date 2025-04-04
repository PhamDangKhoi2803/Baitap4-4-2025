package vn.iotstar.uploadimages;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static vn.iotstar.uploadimages.utils.Const.MY_REQUEST_CODE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.iotstar.uploadimages.api.APIService;
import vn.iotstar.uploadimages.model.ImageUpload;
import vn.iotstar.uploadimages.utils.Const;
import vn.iotstar.uploadimages.utils.RealPathUtil;


public class MainActivity extends AppCompatActivity {
    private EditText editUserName;
    private ImageView imgChoose, imgMultipart;
    private Button btnChoose, btnUpload;
    private TextView tvUsername;
    private Uri mUri;
    private ProgressDialog mProgressDialog;
    private static final String TAG = "MainActivity";
    public static String[] storge_permissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };
    @RequiresApi(api = Build.VERSION_CODES. TIRAMISU)
    public static String[] storge_permissions_33 = {
            android. Manifest.permission.READ_MEDIA_IMAGES,
            android. Manifest.permission.READ_MEDIA_AUDIO,
            android. Manifest.permission.READ_MEDIA_VIDEO

    };

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imgChoose.setImageBitmap(bitmap); // Đảm bảo imgChoose đã được khai báo
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ các view
        AnhXa();

        // Khởi tạo ProgressDialog
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Please wait upload....");

        // Sự kiện nút chọn ảnh
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPermission();
                openGallery();
            }
        });

        // Sự kiện nút upload
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri != null) {
                    UploadImage();
                } else {
                    Toast.makeText(MainActivity.this, "Please choose an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void AnhXa() {
        editUserName = findViewById(R.id.editUserName);
        imgChoose = findViewById(R.id.imgChoose);
        imgMultipart = findViewById(R.id.imgMultipart);
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        tvUsername = findViewById(R.id.tvUsername);
    }

    private void UploadImage() {
        mProgressDialog.show();

        // Lấy username từ EditText
        String username = editUserName.getText().toString().trim();
        if (username.isEmpty()) {
            mProgressDialog.dismiss();
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://app.iotstar.vn/appfoods/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService serviceAPI = retrofit.create(APIService.class);

        // Chuẩn bị file ảnh
        File file = new File(RealPathUtil.getRealPath(this, mUri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);

        // Chuẩn bị username
        RequestBody usernameBody = RequestBody.create(MediaType.parse("multipart/form-data"), username);

        // Gọi API
        Call<ImageUpload> call = serviceAPI.upload(usernameBody, body);
        call.enqueue(new Callback<ImageUpload>() {
            @Override
            public void onResponse(Call<ImageUpload> call, Response<ImageUpload> response) {
                mProgressDialog.dismiss();
                if (response.isSuccessful()) {
                    ImageUpload imageUpload = response.body();
                    tvUsername.setText("Uploaded: " + imageUpload.getUsername());
                    Glide.with(MainActivity.this).load(imageUpload.getAvatar()).into(imgMultipart);
                    Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ImageUpload> call, Throwable t) {
                mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String[] permission(){
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            p = storge_permissions_33;
        }
        else {
            p = storge_permissions;
        }
        return  p;
    }

    private void CheckPermission(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            openGallery();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33 trở lên)
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(permission(), MY_REQUEST_CODE);
            }
        }
    }

    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

}