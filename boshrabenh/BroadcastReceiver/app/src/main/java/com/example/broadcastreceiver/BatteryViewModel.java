package com.example.broadcastreceiver;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BatteryViewModel extends ViewModel {

    // LiveData لمستوى البطارية
    private final MutableLiveData<Integer> _batteryLevel = new MutableLiveData<>();
    public LiveData<Integer> batteryLevel = _batteryLevel;

    // LiveData لحالة الشحن (نص وصفي)
    private final MutableLiveData<String> _chargingStatus = new MutableLiveData<>();
    public LiveData<String> chargingStatus = _chargingStatus;

    public BatteryViewModel() {
        // قيم أولية
        _batteryLevel.setValue(0);
        _chargingStatus.setValue("غير معروف");
    }

    // دالة لتحديث مستوى البطارية
    public void updateBatteryLevel(int level) {
        _batteryLevel.setValue(level);
    }

    // دالة لتحديث حالة الشحن
    public void updateChargingStatus(String status) {
        _chargingStatus.setValue(status);
    }
}
