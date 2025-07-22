package com.example.myapplication;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    private TextView myTextView;
    private Button myButton;
    private ProgressBar progressBar;
    private MyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.myTextView);
        myButton = findViewById(R.id.myButton);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // مراقبة LiveData لتحديث الواجهة الرسومية
        viewModel.message.observe(this, s -> myTextView.setText(s));
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        // **التعديل هنا:** لم نعد نراقب viewModel.isButtonEnabled

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.startLongOperation();
            }
        });

        // إذا كنت تفضل استخدام android:onClick في XML
        // public void buttonClick(View view) {
        //     viewModel.startLongOperation();
        // }
    }
}