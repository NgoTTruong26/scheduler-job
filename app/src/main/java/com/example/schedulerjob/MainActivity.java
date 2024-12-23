package com.example.schedulerjob;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int[] JOB_IDS = {123, 234, 345};
    protected static final int[] ALARM_IDS = {101, 102, 103};
    protected static final long[] INTERVALS = {15 * 60 * 1000, 20 * 60 * 1000, 25 * 60 * 1000}; // 15, 20, 25 minutes

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

        Button startButtonAlarm = findViewById(R.id.startSchedulerAlarm);
        Button stopButtonAlarm = findViewById(R.id.stopSchedulerAlarm);

        // Gắn sự kiện cho các nút
        startButton.setOnClickListener(view -> {
            for (int i = 0; i < JOB_IDS.length; i++) {
                onClickStartScheduler(JOB_IDS[i], INTERVALS[i]);
            }
        });

        stopButton.setOnClickListener(view -> {
            for (int jobId : JOB_IDS) {
                onClickCancelScheduler(jobId);
            }
        });

        startButtonAlarm.setOnClickListener(view -> {
            for (int i = 0; i < ALARM_IDS.length; i++) {
                onClickStartSchedulerAlarm(ALARM_IDS[i], INTERVALS[i]);
            }
        });

        stopButtonAlarm.setOnClickListener(view -> {
            for (int jobId : ALARM_IDS) {
                onClickCancelSchedulerAlarm(jobId);
            }
        });

    }


    private void onClickStartScheduler(int jobId, long intervalMillis) {
        try {
            ComponentName componentName = new ComponentName(this, MyjobService.class);
            JobInfo jobInfo = new JobInfo.Builder(jobId, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // Yêu cầu mạng Wi-Fi
                    .setPersisted(true) // Công việc tồn tại qua reboot
                    .setPeriodic(intervalMillis, intervalMillis/3) // Chạy định kỳ mỗi 15 phút
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

    private void onClickStartSchedulerAlarm(int alarmId, long intervalMillis) {
        AlarmSchedule.scheduleExactAlarm(this, alarmId, intervalMillis);
    }



    private void onClickCancelSchedulerAlarm(int alarmId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Alarm " + alarmId + " cancelled", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Alarm " + alarmId + " cancelled");
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