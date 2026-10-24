package com.controlparental.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServicioMonitoreo extends Service {
    private Handler handler = new Handler();
    private Runnable runnable;
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private DatabaseReference databaseReference;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private String ultimaApp = "";

    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = getPackageManager();
        
        // Configurar bloqueo
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);

        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        String codigoFamilia = prefs.getString("codigo_familia", "");

        if (!codigoFamilia.isEmpty()) {
            databaseReference = FirebaseDatabase.getInstance().getReference("familias").child(codigoFamilia);
            escucharOrdenes();
        } else {
            stopSelf();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MONITOREO", "Monitoreo", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, "MONITOREO")
                .setContentTitle("Control Parental Activo")
                .setContentText("Protegiendo el dispositivo")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build();
                
        startForeground(1, notification);

        runnable = new Runnable() {
            @Override
            public void run() {
                String appActiva = obtenerAppEnPrimerPlano();
                if (appActiva != null && databaseReference != null) {
                    databaseReference.child("app_activa").setValue(appActiva);
                    
                    // NUEVO: Guardar en historial si la app cambió
                    if (!appActiva.equals(ultimaApp)) {
                        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        String registro = appActiva + " a las " + hora;
                        databaseReference.child("historial").push().setValue(registro);
                        ultimaApp = appActiva;
                    }
                }
                handler.postDelayed(this, 5000);
            }
        };
    }

    private void escucharOrdenes() {
        databaseReference.child("orden").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String orden = snapshot.getValue(String.class);
                if ("BLOQUEAR".equals(orden)) {
                    if (devicePolicyManager.isAdminActive(componentName)) {
                        devicePolicyManager.lockNow(); // ¡BLOQUEA LA PANTALLA!
                    }
                    // Limpiar la orden para que no se bloquee infinitamente
                    databaseReference.child("orden").setValue("OK");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
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
    public IBinder onBind(Intent intent) { return null; }

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