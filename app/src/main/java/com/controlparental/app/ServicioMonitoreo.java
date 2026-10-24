package com.controlparental.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ServicioMonitoreo extends Service {
    private Handler handler = new Handler();
    private Runnable runnable;
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = getPackageManager();
        databaseReference = FirebaseDatabase.getInstance().getReference("hijos/tablet_prueba");

        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MONITOREO", "Monitoreo", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Notificación para que el sistema no mate el servicio
        Notification notification = new Notification.Builder(this, "MONITOREO")
                .setContentTitle("Control Parental Activo")
                .setContentText("Protegiendo el dispositivo")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build();
                
        try {
            startForeground(1, notification);
        } catch (Exception e) {
            Log.e("ServicioMonitoreo", "Error al iniciar foreground: " + e.getMessage());
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String appActiva = obtenerAppEnPrimerPlano();
                    if (appActiva != null) {
                        databaseReference.child("app_activa").setValue(appActiva);
                        Log.d("ServicioMonitoreo", "App activa: " + appActiva);
                    }
                } catch (Exception e) {
                    Log.e("ServicioMonitoreo", "Error obteniendo app: " + e.getMessage());
                }
                // Repetir cada 5 segundos
                handler.postDelayed(this, 5000);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String obtenerAppEnPrimerPlano() {
        long time = System.currentTimeMillis();
        UsageEvents usageEvents = usageStatsManager.queryEvents(time - 10000, time);
        UsageEvents.Event event = new UsageEvents.Event();
        String lastApp = "";
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastApp = event.getPackageName();
            }
        }
        
        if (!lastApp.isEmpty()) {
            try {
                return packageManager.getApplicationLabel(packageManager.getApplicationInfo(lastApp, 0)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                return lastApp;
            }
        }
        return "Pantalla de inicio";
    }
}