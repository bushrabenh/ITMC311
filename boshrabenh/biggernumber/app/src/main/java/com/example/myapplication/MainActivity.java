package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.os.Handler;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
import android.graphics.Color;
import android.os.Handler;


import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button a1,a2;
    TextView t1;                                                                                                                                                                                                
    int num1,num2,p=0;

    KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        a1=findViewById(R.id.a1);
        a2=findViewById(R.id.a2);
        t1=findViewById(R.id.p);
        konfettiView = findViewById(R.id.konfettiView);

        roll();


        a1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                fun(num1,num2);
            }
        });

        a2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fun(num2,num1);

            }
        });




    }
    void fun(int q1,int q2){
        if(q1>q2){

            p++;
            Toast.makeText(MainActivity.this, " correct", Toast.LENGTH_SHORT).show();
            final MediaPlayer mpClap = MediaPlayer.create(this, R.raw.clap);
            mpClap.start();

            // إيقاف الصوت بعد 10 ثواني (10000 مللي ثانية)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mpClap.isPlaying()) {
                        mpClap.pause();
                        mpClap.seekTo(0);
                        mpClap.release();
                    }
                }
            }, 10000);

            // تحرير مورد MediaPlayer عند انتهاء الصوت لو قبل 10 ثواني
            mpClap.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mpClap.release();
                }
            });
            konfettiView.setVisibility(View.VISIBLE);
            konfettiView.build()
                    .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                    .setDirection(0.0, 359.0)
                    .setSpeed(4f, 7f)
                    .setFadeOutEnabled(true)
                    .setTimeToLive(2000L)
                    .addShapes(Shape.RECT, Shape.CIRCLE)
                    .addSizes(new Size(12, 5f))
                    .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                    .streamFor(300, 2000L); // عدد الجزيئات والمدة

            // إخفاء الكونفيتي بعد انتهاء العرض (مثلاً بعد ثانيتين)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    konfettiView.setVisibility(View.GONE);
                }
            }, 2000);
        }
        else{
            p--;
            Toast.makeText(MainActivity.this, " wrong", Toast.LENGTH_SHORT).show();
            final MediaPlayer mpWrong = MediaPlayer.create(this, R.raw.wrong);
            mpWrong.start();

            // إيقاف الصوت بعد 10 ثواني (10000 مللي ثانية)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mpWrong.isPlaying()) {
                        mpWrong.pause();
                        mpWrong.seekTo(0);
                        mpWrong.release();
                    }
                }
            }, 10000);
            // تحرير مورد MediaPlayer عند انتهاء الصوت لو قبل 10 ثواني
            mpWrong.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mpWrong.release();
                }
            });
        }
        t1.setText("Points: "+p);
        roll();
    }
    void roll(){
        Random random=new Random();
        num1= random.nextInt(9);
        do{
            num2=random.nextInt(9);
        }while(num1==num2);
        a1.setText(num1+"");
        a2.setText(num2+"");
    }


}