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
        Button btnAccion = findViewById(R.id.btn_permiso_uso);

        // Configurar el botón UNA SOLA VEZ con lógica inteligente
        btnAccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manejarSiguientePaso();
            }
        });

        // Verificar estado inicial
        actualizarEstado(tvEstado, btnAccion);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tvEstado = findViewById(R.id.tv_estado_monitoreo);
        Button btnAccion = findViewById(R.id.btn_permiso_uso);
        actualizarEstado(tvEstado, btnAccion);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            TextView tvEstado = findViewById(R.id.tv_estado_monitoreo);
            Button btnAccion = findViewById(R.id.btn_permiso_uso);
            actualizarEstado(tvEstado, btnAccion);
        }
    }

    private void manejarSiguientePaso() {
        boolean tieneUso = tienePermisoDeUso();
        boolean esAdmin = devicePolicyManager.isAdminActive(componentName);
        boolean esAccesibilidad = esServicioAccesibilidadActivo();

        if (!tieneUso) {
            // Paso 1: Pedir permiso de uso
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else if (!esAdmin) {
            // Paso 2: Activar administrador
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
                "Necesario para bloquear la pantalla si intentan desinstalar la app.");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        } else if (!esAccesibilidad) {
            // Paso 3: Activar accesibilidad
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    private void actualizarEstado(TextView tvEstado, Button btnAccion) {
        boolean tieneUso = tienePermisoDeUso();
        boolean esAdmin = devicePolicyManager.isAdminActive(componentName);
        boolean esAccesibilidad = esServicioAccesibilidadActivo();

        if (tieneUso && esAdmin && esAccesibilidad) {
            // TODO COMPLETO
            tvEstado.setText("✅ PROTECCIÓN TOTAL ACTIVADA\nMonitoreo, Anti-desinstalación y Bloqueo de Ajustes activos.");
            tvEstado.setTextColor(0xFF2E7D32); // Verde
            btnAccion.setText("Sistema 100% Protegido");
            btnAccion.setEnabled(false);
            btnAccion.setBackgroundColor(0xFF9E9E9E);
            iniciarServicio();
        } else if (!tieneUso) {
            // Falta permiso de uso
            tvEstado.setText("⚠️ Paso 1 de 3:\nActiva el permiso de 'Acceso a datos de uso'");
            tvEstado.setTextColor(0xFFFF9800); // Naranja
            btnAccion.setText("ACTIVAR PERMISO DE USO");
            btnAccion.setEnabled(true);
            btnAccion.setBackgroundColor(0xFF1565C0);
        } else if (!esAdmin) {
            // Falta admin
            tvEstado.setText("⚠️ Paso 2 de 3:\nActiva el permiso de Administrador");
            tvEstado.setTextColor(0xFFFF9800);
            btnAccion.setText("ACTIVAR ADMINISTRADOR");
            btnAccion.setEnabled(true);
            btnAccion.setBackgroundColor(0xFF1565C0);
        } else if (!esAccesibilidad) {
            // Falta accesibilidad
            tvEstado.setText("⚠️ Paso 3 de 3:\nActiva 'Control Parental' en Accesibilidad");
            tvEstado.setTextColor(0xFFFF9800);
            btnAccion.setText("ACTIVAR ACCESIBILIDAD");
            btnAccion.setEnabled(true);
            btnAccion.setBackgroundColor(0xFF1565C0);
        }
    }

    private boolean tienePermisoDeUso() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
            android.os.Process.myUid(), getPackageName());
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

    private void iniciarServicio() {
        Intent serviceIntent = new Intent(this, ServicioMonitoreo.class);
        startService(serviceIntent);
    }
}