package com.example.schedulerjob;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("ALARM_ID", -1);

        int alarmIndex = getAlarmIndexById(alarmId);
        if (alarmIndex == -1) {
            Log.e("AlarmReceiver", "Alarm ID not found");
            return;
        }
        long intervalMillis = MainActivity.INTERVALS[alarmIndex];

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        Log.e(TAG, "Alarm " + alarmId + " - Current Time: " + currentTime);
        Log.e(TAG, "Alarm " + alarmId + " finished");

        AlarmSchedule.scheduleExactAlarm(context, alarmId, intervalMillis);
    }

    private int getAlarmIndexById(int alarmId) {
        for (int i = 0; i < MainActivity.ALARM_IDS.length; i++) {
            if (MainActivity.ALARM_IDS[i] == alarmId) {
                return i;
            }
        }
        return -1;
    }


}
