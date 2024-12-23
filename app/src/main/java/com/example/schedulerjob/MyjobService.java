package com.example.schedulerjob;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class MyjobService extends JobService {
    public static final String TAG = MyjobService.class.getName();
    private boolean jobCancelled;

    @Override
    public boolean onStartJob(JobParameters jobParameters){

        Log.e(TAG, "Job " + jobParameters.getJobId() + " started");
        doBackgroundWork(jobParameters);
        return true;
    }

    private void doBackgroundWork(JobParameters jobParameters){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String currentTime = sdf.format(new Date()); // Lấy thời gian hiện tại
                Log.e(TAG, "Job " + jobParameters.getJobId() + " - Current Time: " + currentTime);
                Log.e(TAG, "Job " + jobParameters.getJobId() + " finished");
                jobFinished(jobParameters, jobCancelled);
            }
        }).start();
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters){
        Log.e(TAG, "Job " + jobParameters.getJobId() + " cancelled");
        jobCancelled= true;
        return true;
    }


}
