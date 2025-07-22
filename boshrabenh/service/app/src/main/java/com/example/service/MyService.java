package com.example.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat; // لتوافقية الإشعارات

public class MyService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer player;
    private AudioManager audioManager;
    private static final String CHANNEL_ID = "MediaPlaybackChannel";
    private static final int NOTIFICATION_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createNotificationChannel(); // إنشاء قناة الإشعارات
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // طلب تركيز الصوت
        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // لم يتم منح تركيز الصوت، لا يمكن تشغيل الموسيقى
            stopSelf(); // إيقاف الخدمة
            return START_NOT_STICKY;
        }

        if (player == null) { // تأكد من إنشاء الـ player مرة واحدة فقط
            player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
            player.setLooping(true); // تشغيل مستمر
            player.setVolume(1.0f, 1.0f); // ضبط مستوى الصوت
        }

        if (!player.isPlaying()) {
            player.start(); // بدء التشغيل فقط إذا لم يكن يعمل بالفعل
        }

        // تحويل الخدمة إلى Foreground Service
        startForeground(NOTIFICATION_ID, createNotification());

        return START_STICKY; // الخدمة ستظل تعمل حتى يتم إيقافها صراحة
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // استعادة تركيز الصوت: استئناف التشغيل أو زيادة مستوى الصوت
                if (player != null && !player.isPlaying()) {
                    player.start();
                } else if (player != null) {
                    player.setVolume(1.0f, 1.0f);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // فقدان تركيز الصوت بشكل دائم: إيقاف التشغيل
                if (player != null) {
                    player.stop();
                    player.release(); // تحرير الموارد
                    player = null;
                }
                stopSelf(); // إيقاف الخدمة بالكامل
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // فقدان تركيز الصوت مؤقتًا (مثلاً لمكالمة): إيقاف مؤقت
                if (player != null && player.isPlaying()) {
                    player.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // فقدان تركيز الصوت مؤقتًا ويمكن تقليل الصوت: تقليل مستوى الصوت
                if (player != null && player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f); // تقليل الصوت
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop(); // إيقاف التشغيل
            player.release(); // تحرير موارد الـ MediaPlayer
            player = null; // تعيين الكائن لـ null
        }
        // التخلي عن تركيز الصوت
        audioManager.abandonAudioFocus(this);
        // إيقاف الخدمة من Foreground
        stopForeground(true);
    }

    // --- وظائف مساعدة لإنشاء الإشعارات ---

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // مطلوب لـ Android 8.0 (Oreo) والإصدارات الأحدث
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "قناة تشغيل الموسيقى",
                    NotificationManager.IMPORTANCE_LOW // أهمية منخفضة لعدم الإزعاج الدائم
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        // Intent لفتح الـ Activity عند النقر على الإشعار
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE); // FLAG_IMMUTABLE مطلوب لـ Android S+

        // Intent لإيقاف الخدمة من زر في الإشعار
        Intent stopIntent = new Intent(this, MyService.class);
        stopIntent.setAction("ACTION_STOP_SERVICE"); // تعريف إجراء مخصص للإيقاف
        PendingIntent stopPendingIntent = PendingIntent.getService(this,
                0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        // بناء الإشعار
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("مشغل النغمة")
                .setContentText("النغمة تعمل في الخلفية...")
                .setSmallIcon(android.R.drawable.ic_media_play) // أيقونة صغيرة
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "إيقاف", stopPendingIntent) // زر إيقاف في الإشعار
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true); // لعدم إزعاج المستخدم بالإشعار المتكرر

        return builder.build();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // هذه الدالة تُستدعى عندما يقوم المستخدم بإزالة التطبيق من قائمة التطبيقات الحديثة
        // يمكننا استخدامها لإيقاف الخدمة إذا أردنا
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    // يمكننا معالجة الإجراءات المرسلة من الإشعار هنا
    @Override
    public ComponentName startService(Intent service) {
        if (service != null && "ACTION_STOP_SERVICE".equals(service.getAction())) {
            stopSelf(); // إذا كان الإجراء هو إيقاف، نوقف الخدمة
            return null;
        }
        return super.startService(service);
    }
}
