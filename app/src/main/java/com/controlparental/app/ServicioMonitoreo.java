package com.controlparental.app;

import android.app.ActivityManager;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ServicioMonitoreo extends Service {
    
    private Handler handler = new Handler();
    private Runnable runnable;
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private DatabaseReference databaseReference;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private String ultimaApp = "";
    
    // Lista para guardar las apps que el padre bloqueó
    private Set<String> appsBloqueadas = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = getPackageManager();
        
        // Configurar el bloqueo de pantalla
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);

        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        String codigoFamilia = prefs.getString("codigo_familia", "");

        if (!codigoFamilia.isEmpty()) {
            databaseReference = FirebaseDatabase.getInstance().getReference("familias").child(codigoFamilia);
            escucharAppsBloqueadas();
        } else {
            stopSelf();
            return;
        }

        // Configurar notificación para Android 8+
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

        // Lógica que se repite cada 3 segundos
        runnable = new Runnable() {
            @Override
            public void run() {
                String appLabel = obtenerAppEnPrimerPlano(); // Nombre bonito (ej: "YouTube")
                String appPackage = obtenerPaqueteEnPrimerPlano(); // Nombre técnico (ej: "com.google.android.youtube")

                if (appLabel != null && databaseReference != null) {
                    // 1. Actualizar app activa en Firebase
                    databaseReference.child("app_activa").setValue(appLabel);
                    
                    // 2. Guardar en historial si cambió la app
                    if (!appLabel.equals(ultimaApp)) {
                        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        databaseReference.child("historial").push().setValue(appLabel + " a las " + hora);
                        ultimaApp = appLabel;
                    }

                    // 3. VERIFICAR SI LA APP ESTÁ BLOQUEADA
                    if (appPackage != null && appsBloqueadas.contains(appPackage)) {
                        try {
                            // Matar el proceso de la app
                            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                            am.killBackgroundProcesses(appPackage);
                            
                            // Bloquear la tablet inmediatamente
                            if (devicePolicyManager.isAdminActive(componentName)) {
                                devicePolicyManager.lockNow();
                            }
                        } catch (Exception e) {
                            Log.e("ServicioMonitoreo", "Error al bloquear app: " + e.getMessage());
                        }
                    }
                }
                
                // Repetir cada 3 segundos (3000 ms)
                handler.postDelayed(this, 3000);
            }
        };
    }

    // Escucha en tiempo real las apps que el padre bloquea
    private void escucharAppsBloqueadas() {
        databaseReference.child("bloqueadas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                appsBloqueadas.clear(); // Limpiar lista anterior
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        appsBloqueadas.add(child.getKey()); // Agregar paquete a la lista
                    }
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
    public IBinder onBind(Intent intent) { 
        return null; 
    }

    // Obtiene el nombre bonito de la app (ej: "WhatsApp")
    private String obtenerAppEnPrimerPlano() {
        long time = System.currentTimeMillis();
        UsageEvents usageEvents = usageStatsManager.queryEvents(time - 10000, time);
        UsageEvents.Event event = new UsageEvents.Event();
        String lastPackage = "";
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.getPackageName();
            }
        }
        
        if (!lastPackage.isEmpty()) {
            try {
                return packageManager.getApplicationLabel(packageManager.getApplicationInfo(lastPackage, 0)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                return lastPackage;
            }
        }
        return "Pantalla de inicio";
    }

    // Obtiene el nombre técnico del paquete (ej: "com.whatsapp") para poder bloquearlo
    private String obtenerPaqueteEnPrimerPlano() {
        long time = System.currentTimeMillis();
        UsageEvents usageEvents = usageStatsManager.queryEvents(time - 10000, time);
        UsageEvents.Event event = new UsageEvents.Event();
        String lastPackage = "";
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.getPackageName();
            }
        }
        return lastPackage.isEmpty() ? null : lastPackage;
    }
}