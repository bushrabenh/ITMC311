package com.example.asynctask;



import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // كائنات الأزرار وشريط التقدم والنص
    Button btnProcess;
    Button btnCancel;
    ProgressBar progressBar;
    TextView txtPercentage;
    TextView appTitle; // إضافة TextView الجديد للعنوان

    // للاحتفاظ بمرجع لـ AsyncTask الحالي لمنع تشغيل مهام متعددة وإلغاء المهمة
    private DoingAsyncTask currentAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط الكائنات بالعناصر في الواجهة الرسومية (XML)
        appTitle = findViewById(R.id.appTitle); // ربط العنوان الجديد
        btnProcess = findViewById(R.id.buttonProcess);
        btnCancel = findViewById(R.id.buttonCancel);
        progressBar = findViewById(R.id.progressbar);
        txtPercentage = findViewById(R.id.txtpercentage);

        // إضافة مستمع للنقرات لزر بدء العملية
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // منع تشغيل مهمة جديدة إذا كانت هناك مهمة قيد التشغيل بالفعل
                if (currentAsyncTask == null || currentAsyncTask.getStatus() == AsyncTask.Status.FINISHED) {
                    currentAsyncTask = new DoingAsyncTask();
                    currentAsyncTask.execute();
                } else {
                    Toast.makeText(MainActivity.this, "العملية قيد التنفيذ بالفعل!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // إضافة مستمع للنقرات لزر إلغاء العملية
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentAsyncTask != null && currentAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                    currentAsyncTask.cancel(true); // إلغاء المهمة
                } else {
                    Toast.makeText(MainActivity.this, "لا توجد عملية جارية لإلغائها.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // فئة AsyncTask الداخلية
    private class DoingAsyncTask extends AsyncTask<Void, Integer, Void> {

        int progress_status;

        // تُستدعى قبل بدء doInBackground()، على الـ UI Thread
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "بدء العملية...", Toast.LENGTH_SHORT).show();
            progress_status = 0;
            txtPercentage.setText("جارٍ المعالجة 0%");
            progressBar.setProgress(0); // إعادة تعيين شريط التقدم للصفر
            progressBar.setVisibility(View.VISIBLE); // إظهار شريط التقدم
            btnProcess.setEnabled(false); // تعطيل زر البدء
            btnCancel.setVisibility(View.VISIBLE); // إظهار زر الإلغاء
        }

        // تُنفذ في سلسلة تعليمات الخلفية (Background Thread)
        @Override
        protected Void doInBackground(Void... params) {
            while (progress_status < 100) {
                // التحقق مما إذا كانت المهمة قد تم إلغاؤها
                if (isCancelled()) {
                    break; // الخروج من الحلقة إذا تم الإلغاء
                }
                progress_status += 5;
                publishProgress(progress_status); // نشر التقدم لتحديث الواجهة
                SystemClock.sleep(200); // إيقاف مؤقت لمدة 200 ميلي ثانية (لإظهار التقدم بوضوح)
            }
            return null;
        }

        // تُستدعى لتحديث التقدم، على الـ UI Thread
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
            txtPercentage.setText("جارٍ المعالجة " + values[0] + "%");
        }

        // تُستدعى بعد انتهاء doInBackground() بنجاح (لم يتم إلغاؤها)، على الـ UI Thread
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, "العملية اكتملت بنجاح!", Toast.LENGTH_SHORT).show();
            txtPercentage.setText("العملية اكتملت");
            btnProcess.setEnabled(true); // إعادة تفعيل زر البدء
            progressBar.setVisibility(View.GONE); // إخفاء شريط التقدم
            btnCancel.setVisibility(View.GONE); // إخفاء زر الإلغاء
        }

        // تُستدعى إذا تم إلغاء المهمة، على الـ UI Thread
        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(MainActivity.this, "العملية تم إلغاؤها!", Toast.LENGTH_SHORT).show();
            txtPercentage.setText("العملية تم إلغاؤها");
            progressBar.setProgress(0); // إعادة تعيين شريط التقدم
            progressBar.setVisibility(View.GONE); // إخفاء شريط التقدم
            btnProcess.setEnabled(true); // إعادة تفعيل زر البدء
            btnCancel.setVisibility(View.GONE); // إخفاء زر الإلغاء
        }
    }

    // عند تدمير الـ Activity، يجب إلغاء أي مهام AsyncTask جارية لتجنب تسرب الذاكرة
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentAsyncTask != null && currentAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentAsyncTask.cancel(true);
        }
    }
}