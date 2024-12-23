package com.example.schedulerjob;

import static android.content.ContentValues.TAG;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int JOB_ID = 123;

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
        startButton.setOnClickListener(view -> onClickStartScheduler());
        stopButton.setOnClickListener(view -> onClickCancelScheduler());

    }

    private void onClickStartScheduler() {
        try {
            ComponentName componentName = new ComponentName(this, MyjobService.class);
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // Yêu cầu mạng Wi-Fi
                    .setPersisted(true) // Công việc tồn tại qua reboot
                    .setPeriodic(15 * 60 * 1000) // Chạy định kỳ mỗi 15 phút
                    .build();

            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                int result = jobScheduler.schedule(jobInfo);
                if (result == JobScheduler.RESULT_SUCCESS) {
                    Log.d(TAG, "Job scheduled successfully");
                } else {
                    Log.e(TAG, "Failed to schedule job");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling job: ", e);
        }
    }
    private void onClickCancelScheduler(){
        Toast.makeText(MainActivity.this, "Job cancelled",Toast.LENGTH_SHORT).show();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

}