package com.controlparental.app;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HijoActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hijo);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);

        TextView tvEstado = findViewById(R.id.tv_estado_monitoreo);
        Button btnPermiso = findViewById(R.id.btn_permiso_uso);

        verificarPermisos(tvEstado, btnPermiso);

        btnPermiso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la pantalla de Accesibilidad para que el usuario active el servicio
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tvEstado = findViewById(R.id.tv_estado_monitoreo);
        Button btnPermiso = findViewById(R.id.btn_permiso_uso);
        verificarPermisos(tvEstado, btnPermiso);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            TextView tvEstado = findViewById(R.id.tv_estado_monitoreo);
            Button btnPermiso = findViewById(R.id.btn_permiso_uso);
            verificarPermisos(tvEstado, btnPermiso);
        }
    }

    private void verificarPermisos(TextView tvEstado, Button btnPermiso) {
        boolean tieneUso = tienePermisoDeUso();
        boolean esAdmin = devicePolicyManager.isAdminActive(componentName);
        boolean esAccesibilidad = esServicioAccesibilidadActivo();

        if (tieneUso && esAdmin && esAccesibilidad) {
            tvEstado.setText("✅ PROTECCIÓN TOTAL ACTIVADA\nMonitoreo, Anti-desinstalación y Bloqueo de Ajustes activos.");
            tvEstado.setTextColor(0xFF2E7D32); // Verde
            btnPermiso.setText("Sistema 100% Protegido");
            btnPermiso.setEnabled(false);
            btnPermiso.setBackgroundColor(0xFF9E9E9E); 
            iniciarServicio();
        } else {
            tvEstado.setText("⚠️ Protección incompleta.\nToca el botón y activa 'Control Parental' en Accesibilidad.");
            tvEstado.setTextColor(0xFFFF9800); // Naranja
            btnPermiso.setText("Activar Protección de Accesibilidad");
            btnPermiso.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Primero activamos admin si no lo está
                    if (!esAdmin) {
                        activarAdministrador();
                    } else {
                        // Luego pedimos accesibilidad
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private boolean tienePermisoDeUso() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean esServicioAccesibilidadActivo() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                return settingValue.contains(getPackageName() + "/com.controlparental.app.AntiUninstallAccessibilityService");
            }
        }
        return false;
    }

    private void activarAdministrador() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Necesario para bloquear la pantalla si intentan desinstalar la app.");
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    private void iniciarServicio() {
        Intent serviceIntent = new Intent(this, ServicioMonitoreo.class);
        startService(serviceIntent);
    }
}