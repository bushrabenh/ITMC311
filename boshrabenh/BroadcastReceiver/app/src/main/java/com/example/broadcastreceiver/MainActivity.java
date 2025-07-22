package com.example.broadcastreceiver;



import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // استيراد ViewModelProvider

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager; // لاستخدام ثوابت BatteryManager
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView txtPercentage;
    private TextView txtChargingStatus; // TextView جديد لحالة الشحن
    private BatteryViewModel batteryViewModel; // كائن ViewModel

    // Broadcast Receiver Object
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            // الحصول على مستوى البطارية
            int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            // تحديث LiveData في ViewModel بمستوى البطارية
            batteryViewModel.updateBatteryLevel(level);

            // الحصول على حالة الشحن ونوع الشاحن
            int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int chargePlug = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            String statusText;
            if (isCharging) {
                if (usbCharge) {
                    statusText = "جاري الشحن (USB)";
                } else if (acCharge) {
                    statusText = "جاري الشحن (AC)";
                } else {
                    statusText = "جاري الشحن";
                }
            } else {
                statusText = "غير متصل بالشاحن";
            }
            if (status == BatteryManager.BATTERY_STATUS_FULL) {
                statusText = "البطارية ممتلئة";
            }
            // تحديث LiveData في ViewModel بحالة الشحن
            batteryViewModel.updateChargingStatus(statusText);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط العناصر من الواجهة الرسومية
        progressBar = findViewById(R.id.progressbar);
        txtPercentage = findViewById(R.id.txtpercentage);
        txtChargingStatus = findViewById(R.id.txtChargingStatus); // ربط TextView الجديد

        // تهيئة ViewModel
        batteryViewModel = new ViewModelProvider(this).get(BatteryViewModel.class);

        // مراقبة LiveData لتحديث الواجهة الرسومية
        // هذا يضمن أن الواجهة ستتحدث تلقائيًا عند تغير البيانات في ViewModel
        // وأيضًا ستستعيد حالتها الصحيحة عند تدوير الشاشة
        batteryViewModel.batteryLevel.observe(this, level -> {
            progressBar.setProgress(level);
            txtPercentage.setText("مستوى البطارية: " + level + "%");
        });

        batteryViewModel.chargingStatus.observe(this, status -> {
            txtChargingStatus.setText("حالة الشحن: " + status);
        });

        // تسجيل الـ BroadcastReceiver عند إنشاء الـ Activity
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // إلغاء تسجيل الـ BroadcastReceiver عند تدمير الـ Activity
        // هذا يمنع تسرب الذاكرة ويضمن عدم استقبال التحديثات بعد تدمير الـ Activity
        unregisterReceiver(mBatInfoReceiver);
    }
}