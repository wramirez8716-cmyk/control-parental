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
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
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

        if (tieneUso && esAdmin) {
            tvEstado.setText("✅ PROTECCIÓN ACTIVA\nMonitoreo y Anti-desinstalación activados.");
            tvEstado.setTextColor(0xFF2E7D32); 
            btnPermiso.setText("Sistema Protegido");
            btnPermiso.setEnabled(false);
            btnPermiso.setBackgroundColor(0xFF9E9E9E); 
            iniciarServicio();
        } else if (tieneUso && !esAdmin) {
            tvEstado.setText("⚠️ Falta permiso de Administrador.\nToca el botón para activar la protección anti-desinstalación.");
            tvEstado.setTextColor(0xFFFF9800); 
            btnPermiso.setText("Activar Protección Admin");
            btnPermiso.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activarAdministrador();
                }
            });
        } else {
            tvEstado.setText("⚠️ Permiso requerido:\nToca el botón y activa 'Control Parental'");
            tvEstado.setTextColor(0xFFD32F2F);
            btnPermiso.setText("Activar Permiso de Uso");
            btnPermiso.setEnabled(true);
            btnPermiso.setBackgroundColor(0xFF1565C0);
        }
    }

    private boolean tienePermisoDeUso() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void activarAdministrador() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Esta protección evita que la app sea desinstalada.");
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    private void iniciarServicio() {
        Intent serviceIntent = new Intent(this, ServicioMonitoreo.class);
        startService(serviceIntent);
    }
}