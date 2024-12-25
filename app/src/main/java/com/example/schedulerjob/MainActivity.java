package com.example.schedulerjob;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import helpers.Weather;


public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int JOB_IDS = 123456;
    protected static final long INTERVALS = 15 * 60 * 1000; // 15, 20, 25 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các nút
        Button startButton = findViewById(R.id.StartSchedulerJob);
        Button stopButton = findViewById(R.id.StopSchedulerJob);


        // Gắn sự kiện cho các nút
        startButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                onClickStartScheduler(JOB_IDS, INTERVALS);

            }
        });

        stopButton.setOnClickListener(view -> {

                onClickCancelScheduler(JOB_IDS);

        });

        LocalBroadcastManager.getInstance(this).registerReceiver(weatherDataReceiver,
                new IntentFilter("WEATHER_DATA_ACTION"));
    }

    private final BroadcastReceiver weatherDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String weatherDescription = intent.getStringExtra("weather_description");
            double temperature = intent.getDoubleExtra("temperature", 0.0);
            String cityName = intent.getStringExtra("city_name");

            // Cập nhật UI
            updateWeatherUI(weatherDescription, temperature, cityName);
        }
    };

    private void updateWeatherUI(String weatherDescription, double temperature, String cityName) {
        TextView weatherInfoTextView = findViewById(R.id.weatherInfoTextView);

        String weatherInfo = "City: " + cityName + "\n"
                + "Temperature: " + temperature + "°C\n"
                + "Weather: " + weatherDescription;

        weatherInfoTextView.setText(weatherInfo);
    }


    private void onClickStartScheduler(int jobId, long intervalMillis) {
        try {
            ComponentName componentName = new ComponentName(this, MyjobService.class);
            JobInfo jobInfo = new JobInfo.Builder(jobId, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // Yêu cầu mạng Wi-Fi
                    .setPersisted(true) // Công việc tồn tại qua reboot
                    .setPeriodic(intervalMillis) // Chạy định kỳ mỗi 15 phút
                    .build();

            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                int result = jobScheduler.schedule(jobInfo);
                if (result == JobScheduler.RESULT_SUCCESS) {
                    Toast.makeText(MainActivity.this, "Job " + jobId + " scheduled successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Job " + jobId + " scheduled successfully");
                } else {
                    Toast.makeText(MainActivity.this, "Failed to schedule job " + jobId, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to schedule job " + jobId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling job: ", e);
        }
    }

    private void onClickCancelScheduler(int jobId) {
        Toast.makeText(MainActivity.this, "Job " + jobId + " cancelled", Toast.LENGTH_SHORT).show();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancel(jobId);
        }
    }

}