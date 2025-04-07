package vn.iotstar.socket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    private Button btnLed1On, btnLed7On, btnOff, btnNotification;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần giao diện
        btnLed1On = findViewById(R.id.btn_led1_on);
        btnLed7On = findViewById(R.id.btn_led7_on);
        btnOff = findViewById(R.id.btn_off);
        btnNotification = findViewById(R.id.btn_notification);
        tvStatus = findViewById(R.id.tv_status);

        // Khởi tạo Socket
        try {
            mSocket = IO.socket("http://192.168.112.1:3000"); // Thay bằng URL server của bạn
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Kết nối Socket
        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
            tvStatus.setText("Thiết bị đang kết nối");
            Toast.makeText(MainActivity.this, "Đã kết nối đến server", Toast.LENGTH_SHORT).show();
        }));
        mSocket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> {
            tvStatus.setText("Mất kết nối");
            Toast.makeText(MainActivity.this, "Mất kết nối đến server", Toast.LENGTH_SHORT).show();
        }));
        mSocket.connect();

        // Xử lý sự kiện nhấn nút
        btnLed1On.setOnClickListener(v -> {
            mSocket.emit("led_control", "led1_on");
            Toast.makeText(MainActivity.this, "Đèn 1: BẬT", Toast.LENGTH_SHORT).show();
        });

        btnLed7On.setOnClickListener(v -> {
            mSocket.emit("led_control", "led7_on");
            Toast.makeText(MainActivity.this, "Đèn 7: BẬT", Toast.LENGTH_SHORT).show();
        });

        btnOff.setOnClickListener(v -> {
            mSocket.emit("led_control", "all_off");
            Toast.makeText(MainActivity.this, "Tất cả đèn: TẮT", Toast.LENGTH_SHORT).show();
        });

        btnNotification.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Trạng thái: Đèn đang hoạt động", Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }
}