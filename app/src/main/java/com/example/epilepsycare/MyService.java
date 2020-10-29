package com.example.epilepsycare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.epilepsycare.Constants.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class MyService extends Service implements SensorEventListener {

    public MyService() {
    }

    // Creating TAG's
    private static final String TAG = "foregroundService";
    private static final String TAG_ACC = "acceleration";
    private static final String TAG_TASK = "fallTask";
    private static final String TAG_FALL = "Fall Detected";

    // Creating static variables
//    public static boolean isServiceStopped = false;
    public static boolean isFallTaskCancelled = false;
    public static boolean vibrationAlert, sendSMS, sendLocation, voiceAlert;
    public static String location_lat, location_log, location_address, location_link;
    public static String emgName, emgNumber, emgMessage;

    String xValue, yValue, zValue;
    String netValue = "";
    double netAccel;


    // Creating objects of classes needed

    SensorManager sensorManager;
    Sensor accelerometer;
    Notification notification;
    NotificationCompat.Builder builder;
    FallNotificationService fallTask;
    SharedPreferences preferences;
    static TextToSpeech textToSpeech;

    // Notification channel for foreground service

    public static final String CHANNEL_ID1 = "Foreground Service";
    private static final String CHANNEL_NAME1 = "Foreground Service";
    private static final String CHANNEL_DESC1 = "Services Running in background";
    public static final int NOTIFICATION_ID1 = 101;

    // Notification channel for fall event service

    public static final String CHANNEL_ID2 = "Fall Event Notification";
    public static final String CHANNEL_NAME2 = "Fall Event Notification";
    public static final String CHANNEL_DESC2 = "Sending help message";
    public static final int NOTIFICATION_ID2 = 102;


    @Override
    public void onCreate() {

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "onInit: Language not supported ");
                    } else {
                        Log.e("TTS", "onInit: Initialization Successful");
                    }
                } else {
                    Log.e("TTS", "onInit: Initialization Failed");
                }
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        FallEventActivity.preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        FallEventActivity.loadData();

        // Creating Notification Channel for android version greater than Oero(8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel foregroundChannel = new NotificationChannel(CHANNEL_ID1, CHANNEL_NAME1, NotificationManager.IMPORTANCE_LOW);
            NotificationChannel fallEventChannel = new NotificationChannel(CHANNEL_ID2, CHANNEL_NAME2, NotificationManager.IMPORTANCE_HIGH);
            foregroundChannel.setDescription(CHANNEL_DESC1);
            fallEventChannel.setDescription(CHANNEL_DESC2);
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(foregroundChannel);
            manager.createNotificationChannel(fallEventChannel);
        }

        // Registering Accelerometer
        Log.d(TAG, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MyService.this, accelerometer, 100);
//        sensorManager.registerListener(MyService.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Registered Accelerometer Listener");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: myServiceStarted");

        fgServiceNotification();
        fallTask = new FallNotificationService(MyService.this);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (MainActivity.isServiceStopped) {
                        stopForeground(true);
                        stopSelf();
                        MainActivity.isServiceStopped = false;
                        sensorManager.unregisterListener(MyService.this);
                        if (textToSpeech != null) {
                            textToSpeech.stop();
                            textToSpeech.shutdown();
                        }
                        Log.d(TAG, "run: Service Stopped");
                        return;
                    } else {
                        try {
                            if (Double.parseDouble(netValue) < 0.1) {
                                Log.d(TAG_FALL, " x: " + xValue + " y: " + yValue + " z: " + zValue + " netValue: " + netValue);
                                Log.d(TAG_FALL, "run: fall Detected");
                                MainActivity.hasFallen = true;
//                                MainActivity.tv_safetySTS_safe.setVisibility(View.INVISIBLE);
//                                MainActivity.tv_safetySTS_unsafe.setVisibility(View.VISIBLE);
                                String time = preferences.getString("time_limit", String.valueOf(30));
                                if (fallTask.getStatus() == AsyncTask.Status.RUNNING) {
                                    Log.d(TAG_TASK, "run: taskRunning");
                                } else {
                                    fallTask = new FallNotificationService(MyService.this);
                                    if (!time.isEmpty()) {
                                        fallTask.execute(Integer.parseInt(time));
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
//                        updateForegroundNotification();
                        SystemClock.sleep(100);
                    }
                }
            }
        });
        thread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        xValue = String.format("%.2f", sensorEvent.values[0]);
        yValue = String.format("%.2f", sensorEvent.values[1]);
        zValue = String.format("%.2f", sensorEvent.values[2]);
        netAccel = Math.sqrt(Math.pow((sensorEvent.values[0]), 2) + Math.pow((sensorEvent.values[1]), 2) + Math.pow((sensorEvent.values[2]), 2));
        netValue = String.format("%.2f", netAccel / 9);
//        Log.d(TAG_ACC, " x: "+ xValue +  " y: " + yValue + " z: " + zValue + " netValue: " + netValue);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getDateTime() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        return format.format(date);
    }

    public void fgServiceNotification() {

        Intent intent = new Intent(MyService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 101, intent, 0);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID1);
        builder.setSmallIcon(R.drawable.ic_fall)
                .setContentIntent(pendingIntent)
                .setColor(Color.GREEN)
                .setOngoing(true)
                .setContentTitle("Epilepsy Care")
                .setContentText("Be aware. Take care.");
        notification = builder.build();
        startForeground(NOTIFICATION_ID1, notification);

    }

    public void updateForegroundNotification() {
        Log.d(TAG, "run: acceleration Service :: " + netValue);
        builder.setOnlyAlertOnce(true).setContentText("Running Foreground Service  " + netValue).build();
        notification = builder.build();
        startForeground(NOTIFICATION_ID1, notification);
    }

}

class FallNotificationService extends AsyncTask<Integer, Integer, String> {

    private static final String TAG = "Fall Event";
    private static final String TAG_LOK = "Fall Location";
    @SuppressLint("StaticFieldLeak")
    Context serviceContext;

    FallNotificationService(Context context) {
        this.serviceContext = context;
    }

    FusedLocationProviderClient fusedLocationProviderClient;
    MyReceiver myReceiver;
    Vibrator vibrator;
    AudioManager audioManager;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    int initialVolume;

    @Override
    protected void onPreExecute() {

        // Registering objects
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(serviceContext);
        getLocation(serviceContext);
        myReceiver = new MyReceiver();
        vibrator = (Vibrator) serviceContext.getSystemService(Context.VIBRATOR_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(serviceContext);
        audioManager = (AudioManager) serviceContext.getSystemService(Context.AUDIO_SERVICE);

        // checking fallEvent arrayList
        loadData();
        // checking for any audio mute conditions
        initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Integer... integers) {

        if (preferences.getBoolean("pref_setting_check_voiceAlert", false)) {
            Log.d(TAG, "doInBackground: speaker active");
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))/4,0);
            speak(String.valueOf(integers[0]));
        }
        if(MainActivity.fallEvents == null){
            MainActivity.fallEvents = new ArrayList<>();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.FALL_CONFIRMATION_ACTION_OK);
        filter.addAction(Constants.FALL_CONFIRMATION_ACTION_CANCEL);
        serviceContext.registerReceiver(myReceiver, filter);

        Intent mainActivityIntent = new Intent(serviceContext, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(serviceContext,
                111, mainActivityIntent, 0);

        Intent actionIntentCancel = new Intent(serviceContext, MyReceiver.class);
        actionIntentCancel.setAction(Constants.FALL_CONFIRMATION_ACTION_CANCEL);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(serviceContext,
                112, actionIntentCancel, 0);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(serviceContext, MyService.CHANNEL_ID2)
                .setSmallIcon(R.drawable.ic_fall)
                .setContentTitle("Help Message")
                .setContentText("Sending help message in ....")
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setColor(Color.RED)
                .setContentIntent(mainActivityPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
                .addAction(0, "Cancel", actionPendingIntent);
        NotificationManagerCompat NotificationMgr = NotificationManagerCompat.from(serviceContext);

        while (MyService.textToSpeech.isSpeaking()){
            SystemClock.sleep(100);
        }
        if(!MyService.textToSpeech.isSpeaking()){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,initialVolume,0);
        }
        for (int i = 0; i <= integers[0]; i++) {
            Log.d(TAG, "doInBackground: task running " + i);
            if (MyService.isFallTaskCancelled) {
                MainActivity.fallEvents.add(0,new FallEvents(MyService.location_link,getDateTime(),false,"False Fall Alarm"));
                saveData();
                cancelNotification(serviceContext, MyService.NOTIFICATION_ID2);
                SystemClock.sleep(500);
                resetNotify(serviceContext, MyService.NOTIFICATION_ID2, MyService.CHANNEL_ID2);
                MainActivity.hasFallen = false;
//                MyService.isFallTaskCancelled=false;
                return "Cancelled";
            }
            NotificationMgr.notify(MyService.NOTIFICATION_ID2, notificationBuilder
                    .setContentText("Sending in " + (integers[0] - i) + " seconds")
                    .setProgress(integers[0], i, false)
                    .build());
            SystemClock.sleep(1000);
            if (preferences.getBoolean("pref_setting_check_vibration", false)) {
                vibrator.vibrate(500);
            }
        }
        MainActivity.hasFallen = false;
        getLocation(serviceContext);
        cancelNotification(serviceContext, MyService.NOTIFICATION_ID2);
        MainActivity.fallEvents.add(0,new FallEvents(MyService.location_link,getDateTime(),true,"Successful Fall Detection"));
        SystemClock.sleep(500);
        if (preferences.getBoolean("pref_setting_check_voiceAlert", false)) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                    for (int i = 1; i < 30; i = i + 4) {
                        if (preferences.getBoolean("pref_setting_check_voiceAlert",false)){
                            speakHelp();
                            SystemClock.sleep(3000);
                        }
                    }
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,initialVolume,0);
                }
            });
            thread.start();
        }
        if (preferences.getBoolean("pref_setting_check_sendSMS", false)) {
            if (preferences.getBoolean("pref_setting_check_sendLocation", false)) {
                sendSMS(MyService.location_link, MyService.location_address);
            } else {
                sendSMS();
            }
            confirmationNotify(serviceContext, MyService.NOTIFICATION_ID2, MyService.CHANNEL_ID2);
        } else {
            cancelNotification(serviceContext, MyService.NOTIFICATION_ID2);
            failNotify(serviceContext, MyService.NOTIFICATION_ID2, MyService.CHANNEL_ID2);
        }
        saveData();

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        MyService.isFallTaskCancelled = false;
        serviceContext.unregisterReceiver(myReceiver);
        super.onPostExecute(s);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    public static void cancelNotification(Context context, int notificationID) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(notificationID);
    }

    public void confirmationNotify(Context context, int notificationID, String channelID) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID)
                .setAutoCancel(true)
                .setContentTitle("Send SMS")
                .setContentText("Help Message Send Successfully")
                .setColor(Color.GREEN)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_fall);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(notificationID, mBuilder.build());
    }

    public static void resetNotify(Context context, int notificationID, String channelID) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID)
                .setAutoCancel(true)
                .setContentTitle("Fall Event")
                .setContentText("False Alarm Recorded")
                .setColor(Color.WHITE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setTimeoutAfter(30 * 1000)
                .setSmallIcon(R.drawable.ic_fall);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(notificationID, mBuilder.build());
    }

    public static void failNotify(Context context, int notificationID, String channelID) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID)
                .setAutoCancel(true)
                .setContentTitle("Emergency")
                .setContentText("Help message not send")
                .setColor(Color.YELLOW)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setTimeoutAfter(60 * 1000)
                .setSmallIcon(R.drawable.ic_fall);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(notificationID, mBuilder.build());
    }

    @SuppressLint("UnlocalizedSms")
    public void sendSMS(String location_link, String location_address) {
        SmsManager myManager = SmsManager.getDefault();
//        myManager.sendTextMessage("8770016236", null, "Hello, I need help." +"\n" +location_address+".\n"+ location_link , null, null);
        myManager.sendTextMessage(MainActivity.emgNumber, null, MainActivity.emgMessage + "\n" + location_address + "\n" + location_link, null, null);
    }

    @SuppressLint("UnlocalizedSms")
    public void sendSMS() {
        SmsManager myManager = SmsManager.getDefault();
//        myManager.sendTextMessage("8770016236", null, "Hello, I need help.", null, null);
        myManager.sendTextMessage(MainActivity.emgNumber, null, MainActivity.emgMessage, null, null);
    }

    public void speak(String timeLimit) {
        String text = "Seizure has been detected. Help message will be send in " + timeLimit + " seconds";
        MyService.textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void speakHelp() {
        String text = " Please Help Me ";
        MyService.textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void getLocation(final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                // Initialize Location
                Location location = task.getResult();
                if (location != null) {
                    Log.d(TAG, "Location :: " + location);
                    // Initialize geoCoder
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    // Initialize Address
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        /*Log.d(TAG, "onComplete: Location :: " + addresses);
                        Log.d(TAG, "onComplete: Location :: " + addresses.get(0).getLatitude());
                        Log.d(TAG, "onComplete: Location :: " + addresses.get(0).getLongitude());*/

                        MyService.location_lat = Double.toString(addresses.get(0).getLatitude());
                        MyService.location_log = Double.toString(addresses.get(0).getLongitude());
                        MyService.location_address = addresses.get(0).getAddressLine(0);
                        MyService.location_link = "http://www.google.com/maps/place/" + MyService.location_lat + "," + MyService.location_log;

                        Log.d(TAG_LOK, "onComplete: Location :: " + MyService.location_link);

                    } catch (Exception e) {
                        Log.d(TAG_LOK, "onComplete: " + e);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public String getDateTime() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        String DateToStr = format.format(date);
//        System.out.println(DateToStr);
        return DateToStr;
    }


    public void saveData() {
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.fallEvents);
        editor = preferences.edit();
        editor.putString("com.example.epilepsycare.fallevent", json);
        editor.apply();
    }

    public void loadData() {
        if (MainActivity.fallEvents == null) {
            MainActivity.fallEvents = new ArrayList<>();
        }
        Gson gson = new Gson();
        String json = preferences.getString("com.example.epilepsycare.fallevent", null);
        Type type = new TypeToken<ArrayList<FallEvents>>() {
        }.getType();
        MainActivity.fallEvents = gson.fromJson(json, type);

    }


}
