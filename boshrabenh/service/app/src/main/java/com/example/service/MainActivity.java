package com.example.service;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // استيراد Toast

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonStart;
    private Button buttonStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent serviceIntent = new Intent(this, MyService.class);

        if (view == buttonStart) {
            // بدء الخدمة كـ foreground service (مهم لـ Android 8.0+)
            // يجب أن تطلب إذن POST_NOTIFICATIONS في Android 13+
            startService(serviceIntent);
            Toast.makeText(this, "بدء تشغيل النغمة", Toast.LENGTH_SHORT).show();
        } else if (view == buttonStop) {
            // إيقاف الخدمة
            stopService(serviceIntent);
            Toast.makeText(this, "إيقاف تشغيل النغمة", Toast.LENGTH_SHORT).show();
        }
    }
}