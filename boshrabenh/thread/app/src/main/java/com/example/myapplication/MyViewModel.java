package com.example.myapplication;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyViewModel extends ViewModel {

    private final MutableLiveData<String> _message = new MutableLiveData<>();
    public LiveData<String> message = _message;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    // لم نعد نحتاج لـ _isButtonEnabled هنا، سيظل الزر مفعلاً دائمًا في الواجهة

    private ExecutorService executorService;
    private Future<?> currentTask; // للاحتفاظ بمرجع للمهمة الجارية

    public MyViewModel() {
        _message.setValue("اضغط على الزر للبدء");
        _isLoading.setValue(false);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void startLongOperation() {
        // **التعديل هنا:** التحقق مما إذا كانت هناك عملية جارية بالفعل
        if (currentTask != null && !currentTask.isDone()) {
            _message.postValue("خطأ: العملية قيد التنفيذ بالفعل!");
            return; // الخروج من الدالة لمنع بدء عملية جديدة
        }

        // تحديث حالة الواجهة قبل بدء العملية
        _message.setValue("العملية بدأت. جارٍ العد التنازلي...");
        _isLoading.setValue(true);

        // إطلاق العملية الطويلة في سلسلة تعليمات الخلفية
        currentTask = executorService.submit(() -> {
            try {
                int totalDurationSeconds = 20;
                long startTime = System.currentTimeMillis();
                long endTime = startTime + totalDurationSeconds * 1000;

                for (int i = 0; i <= totalDurationSeconds; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    final int remainingTime = totalDurationSeconds - i;
                    _message.postValue("متبقي: " + remainingTime + " ثانية");

                    long currentTime = System.currentTimeMillis();
                    long targetTimeForNextUpdate = startTime + (long)(i + 1) * 1000;
                    long sleepDuration = targetTimeForNextUpdate - currentTime;

                    if (sleepDuration > 0) {
                        Thread.sleep(sleepDuration);
                    }
                }

                if (!Thread.currentThread().isInterrupted()) {
                    _message.postValue("العملية انتهت بنجاح!");
                } else {
                    _message.postValue("العملية تم إلغاؤها.");
                }

            } catch (InterruptedException e) {
                _message.postValue("العملية تم إلغاؤها.");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                _message.postValue("حدث خطأ: " + e.getMessage());
            } finally {
                // تحديث حالة الواجهة بعد انتهاء العملية
                _isLoading.postValue(false);
                // لم نعد نحتاج لتفعيل الزر هنا، لأنه لا يتم تعطيله
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        executorService.shutdownNow();
    }
}
