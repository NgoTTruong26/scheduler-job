package com.example.schedulerjob;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import helpers.Weather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyjobService extends JobService {

    Weather weather = new Weather();

    public static final String TAG = MyjobService.class.getName();
    private boolean jobCancelled;
    private FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    public boolean onStartJob(JobParameters jobParameters){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
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
                getCurrentLocation();
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

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa được cấp quyền, thoát ra hoặc yêu cầu quyền
            Log.e(TAG, "Location permission not granted");
            return;
        }

        // Tạo yêu cầu vị trí
        com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create()
                .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(15*60*1000) // Cập nhật mỗi 10 giây
                .setFastestInterval(5000); // Tối thiểu 5 giây

        // Đăng ký cập nhật vị trí
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    fetchWeatherData(latitude, longitude);
                    Log.d(TAG, "Updated Latitude: " + latitude + ", Longitude: " + longitude);
                } else {
                    Log.d(TAG, "Location is null");
                }
            }
        }, this.getMainLooper());
    }

    private void sendWeatherDataToUI(Context context, String weatherDescription, double temperature, String cityName) {
        Intent localIntent = new Intent("WEATHER_DATA_ACTION");
        localIntent.putExtra("weather_description", weatherDescription);
        localIntent.putExtra("temperature", temperature);
        localIntent.putExtra("city_name", cityName);

        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    public void fetchWeatherData(double latitude, double longitude) {
        OkHttpClient client = new OkHttpClient();

        // Đảm bảo rằng `weather.fetchWeatherData()` trả về một Request hợp lệ
        Request weatherRequest = weather.fetchWeatherData(latitude, longitude);

        client.newCall(weatherRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject data = new JSONObject(responseBody); // Phân tích cú pháp JSON từ chuỗi
                        Log.d("WeatherAPI", "Response JSON: " + data.toString());

                        // Trích xuất thông tin từ JSON
                        String weatherDescription = data.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("description");
                        double temperature = data.getJSONObject("main").getDouble("temp");
                        String cityName = data.getString("name");

                        // Log thông tin
                        Log.d("WeatherAPI", "City: " + cityName);
                        Log.d("WeatherAPI", "Temperature: " + temperature + "°C");
                        Log.d("WeatherAPI", "Weather: " + weatherDescription);

                        sendWeatherDataToUI(MyjobService.this, weatherDescription, temperature, cityName);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Failed to fetch weather data.");

                }
            }
        });
    }


}
