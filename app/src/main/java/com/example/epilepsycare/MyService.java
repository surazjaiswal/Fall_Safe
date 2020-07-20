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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.epilepsycare.Constants.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Locale;

public class MyService extends Service implements SensorEventListener {

    public MyService() {
    }

    private static final String TAG = "foregroundService";
    private static final String TAG_ACC = "acceleration";
    private static final String TAG_TASK = "fallTask";
    private static final String TAG_FALL = "Fall Detected";

//    Creating static variables
//    public static boolean isServiceStopped = false;
    public static boolean isFallTaskCancelled = false;
    public static boolean notificationAlert,sendSMS,sendLocation,voiceAlert;
    public static String location_lat,location_log,location_address,location_link;

    String  xValue, yValue, zValue;
    String netValue = "";
    double netAccel;

    // Creating objects of classes needed

    SensorManager sensorManager;
    Sensor accelerometer;
    Notification notification;
    NotificationCompat.Builder builder;
    FallNotificationService fallTask;

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

        // Creating Notification Channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel foregroundChannel = new NotificationChannel(CHANNEL_ID1,CHANNEL_NAME1, NotificationManager.IMPORTANCE_LOW);
            NotificationChannel fallEventChannel = new NotificationChannel(CHANNEL_ID2,CHANNEL_NAME2,NotificationManager.IMPORTANCE_HIGH);
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
        sensorManager.registerListener(MyService.this,accelerometer,100);
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
                while (true){
                    if(MainActivity.isServiceStopped){
                        stopForeground(true);
                        stopSelf();
                        MainActivity.isServiceStopped = false;
                        sensorManager.unregisterListener(MyService.this);
                        Log.d(TAG, "run: Service Stopped");
                        return;
                    }else {
                        try {
                            if(Double.parseDouble(netValue) < 0.1){
                                Log.d(TAG_ACC, " x: "+ xValue +  " y: " + yValue + " z: " + zValue + " netValue: " + netValue);
                                netValue = "!!! FALL !!!";
                                Log.d(TAG_FALL, "run: fall Detected");
                                if(fallTask.getStatus() == AsyncTask.Status.RUNNING){
                                    Log.d(TAG_TASK, "run: taskRunning");
                                }else {
                                    fallTask = new FallNotificationService(MyService.this);
                                    fallTask.execute();
                                }
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "run: Service IS Running");
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

        xValue = String.format("%.2f",sensorEvent.values[0]);
        yValue = String.format("%.2f",sensorEvent.values[1]);
        zValue = String.format("%.2f",sensorEvent.values[2]);
        netAccel = Math.sqrt(Math.pow((sensorEvent.values[0]),2) + Math.pow((sensorEvent.values[1]),2) + Math.pow((sensorEvent.values[2]),2));
        netValue = String.format("%.2f",netAccel/9);
//        Log.d(TAG_ACC, " x: "+ xValue +  " y: " + yValue + " z: " + zValue + " netValue: " + netValue);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void fgServiceNotification(){

        Intent intent = new Intent(MyService.this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this,101,intent,0);

        builder = new NotificationCompat.Builder(this,CHANNEL_ID1);
        builder.setSmallIcon(R.drawable.ic_fall)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setColor(Color.GREEN)
                .setOngoing(true)
                .setContentTitle("Epilepsy Care")
                .setContentText("Be aware. Take care.");
        notification = builder.build();
        startForeground(NOTIFICATION_ID1,notification);

    }

    public void updateForegroundNotification(){
        Log.d(TAG, "run: acceleration Service :: "+netValue);
        builder.setOnlyAlertOnce(true).setContentText("Running Foreground Service  " + netValue).build();
        notification = builder.build();
        startForeground(NOTIFICATION_ID1,notification);
    }

}

class FallNotificationService extends AsyncTask<Integer,Integer,String>{

    private static final String TAG = "Fall Event";
    private static final String TAG_LOK = "Fall Location";
    @SuppressLint("StaticFieldLeak")
    Context serviceContext;
    public FallNotificationService(Context context) {
        this.serviceContext = context;
    }

    FusedLocationProviderClient fusedLocationProviderClient;
    MyReceiver myReceiver;


    @Override
    protected void onPreExecute() {

        // Registering LocationClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(serviceContext);
        getLocation(serviceContext);
        myReceiver = new MyReceiver();

        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Integer... integers) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.FALL_CONFIRMATION_ACTION_OK);
        filter.addAction(Constants.FALL_CONFIRMATION_ACTION_CANCEL);
        serviceContext.registerReceiver(myReceiver,filter);

        Intent mainActivityIntent = new Intent(serviceContext,MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(serviceContext,
                111,mainActivityIntent,0);

        Intent actionIntentCancel = new Intent(serviceContext,MyReceiver.class);
        actionIntentCancel.setAction(Constants.FALL_CONFIRMATION_ACTION_CANCEL);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(serviceContext,
                112,actionIntentCancel,0);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(serviceContext,MyService.CHANNEL_ID2)
                .setSmallIcon(R.drawable.ic_fall)
                .setContentTitle("Help Message")
                .setContentText("Sending help message in ....")
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setColor(Color.RED)
                .setContentIntent(mainActivityPendingIntent)
                .setVibrate(new long[] { 1000, 1000, 1000 })
                .addAction(0,"Cancel",actionPendingIntent);
        NotificationManagerCompat NotificationMgr = NotificationManagerCompat.from(serviceContext);

        for (int i = 1; i <= 30; i++) {
            Log.d(TAG, "doInBackground: task running " + i);
            if(MyService.isFallTaskCancelled){
                /*NotificationMgr.notify(MyService.NOTIFICATION_ID2,notificationBuilder.setContentText("False alarm recorded")
                                .setOnlyAlertOnce(false)
                                .setOngoing(false)
                                .setColor(Color.WHITE)
                                .build());*/
                SystemClock.sleep(1000);
                cancelNotification(serviceContext,MyService.NOTIFICATION_ID2);
                SystemClock.sleep(1000);
                resetNotify(serviceContext,MyService.NOTIFICATION_ID2,MyService.CHANNEL_ID2);
//                MyService.isFallTaskCancelled=false;
                return "Cancelled";
            }
            NotificationMgr.notify(MyService.NOTIFICATION_ID2,notificationBuilder
                    .setContentText("Sending in " + (30-i) + " seconds")
                    .setProgress(30,i,false)
                    .build());
            SystemClock.sleep(1000);
        }
        getLocation(serviceContext);
        if(MyService.sendSMS){
            if(MyService.sendLocation){
                sendSMS(MyService.location_link,MyService.location_address);
            }else {
                sendSMS();
            }
            confirmationNotify(serviceContext,MyService.NOTIFICATION_ID2,MyService.CHANNEL_ID2);
        }else {
            cancelNotification(serviceContext,MyService.NOTIFICATION_ID2);
            failNotify(serviceContext,MyService.NOTIFICATION_ID2,MyService.CHANNEL_ID2);
        }

        /*NotificationMgr.notify(MyService.NOTIFICATION_ID2,notificationBuilder.setContentText("Help message send")
                .setColor(Color.GREEN)
                .setOngoing(false)
                .build());*/

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        MyService.isFallTaskCancelled=false;
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

    public void confirmationNotify(Context context, int notificationID,String channelID) {
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

    public static void resetNotify(Context context, int notificationID,String channelID) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID)
                .setAutoCancel(true)
                .setContentTitle("Fall Event")
                .setContentText("FALSE Fall Alarm Recorded")
                .setColor(Color.WHITE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setTimeoutAfter(30 * 1000)
                .setSmallIcon(R.drawable.ic_fall);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(notificationID, mBuilder.build());
    }

    public static void failNotify(Context context, int notificationID,String channelID) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID)
                .setAutoCancel(true)
                .setContentTitle("Fall Event")
                .setContentText("Help message not send")
                .setColor(Color.YELLOW)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setTimeoutAfter(60 * 1000)
                .setSmallIcon(R.drawable.ic_fall);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(notificationID, mBuilder.build());
    }

    @SuppressLint("UnlocalizedSms")
    public void sendSMS(String location_link,String location_address) {
        SmsManager myManager = SmsManager.getDefault();
        myManager.sendTextMessage("8770016236", null, "Hello! I need Help" +"\n" +location_address+"\n"+ location_link , null, null);
    }
    @SuppressLint("UnlocalizedSms")
    public void sendSMS() {
        SmsManager myManager = SmsManager.getDefault();
        myManager.sendTextMessage("8770016236", null, "Hello! I need Help", null, null);
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
                if(location != null){
                    Log.d(TAG, "Location :: " + location);
                    // Initialize geoCoder
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    // Initialize Address
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

                        /*Log.d(TAG, "onComplete: Location :: " + addresses);
                        Log.d(TAG, "onComplete: Location :: " + addresses.get(0).getLatitude());
                        Log.d(TAG, "onComplete: Location :: " + addresses.get(0).getLongitude());*/

                        MyService.location_lat = Double.toString(addresses.get(0).getLatitude());
                        MyService.location_log = Double.toString(addresses.get(0).getLongitude());
                        MyService.location_address = addresses.get(0).getAddressLine(0);
                        MyService.location_link = "http://www.google.com/maps/place/"+MyService.location_lat+","+MyService.location_log;

                        Log.d(TAG_LOK, "onComplete: Location :: " + MyService.location_link);

                    }catch (Exception e){
                        Log.d(TAG_LOK, "onComplete: " + e);
                        e.printStackTrace();
                    }
                }
            }
        });
    }



}
