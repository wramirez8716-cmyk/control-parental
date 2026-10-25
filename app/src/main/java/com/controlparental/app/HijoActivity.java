package com.controlparental.app;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HijoActivity extends Activity {

    private EditText etCodigo;
    private Button btnConectar, btnActivarPermiso;
    private TextView tvEstadoPermisos;
    private LinearLayout layoutPermisos, layoutExito;
    private DatabaseReference dbRef;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hijo);

        etCodigo = findViewById(R.id.et_codigo);
        btnConectar = findViewById(R.id.btn_conectar);
        btnActivarPermiso = findViewById(R.id.btn_activar_permiso);
        tvEstadoPermisos = findViewById(R.id.tv_estado_permisos);
        layoutPermisos = findViewById(R.id.layout_permisos);
        layoutExito = findViewById(R.id.layout_exito);

        dbRef = FirebaseDatabase.getInstance().getReference();
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);

        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        String codigoGuardado = prefs.getString("codigo_familia", "");

        if (!codigoGuardado.isEmpty()) {
            etCodigo.setText(codigoGuardado);
            etCodigo.setEnabled(false);
            btnConectar.setVisibility(View.GONE);
            layoutPermisos.setVisibility(View.VISIBLE);
            verificarYGuiarPermisos();
        }

        btnConectar.setOnClickListener(v -> {
            String codigo = etCodigo.getText().toString().trim().toUpperCase();
            if (codigo.length() != 6) {
                Toast.makeText(this, "El código debe tener 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            dbRef.child("codigos").child(codigo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
                        prefs.edit().putString("codigo_familia", codigo).apply();
                        
                        Toast.makeText(HijoActivity.this, "¡Conectado!", Toast.LENGTH_LONG).show();
                        etCodigo.setEnabled(false);
                        btnConectar.setVisibility(View.GONE);
                        layoutPermisos.setVisibility(View.VISIBLE);
                        verificarYGuiarPermisos();
                    } else {
                        Toast.makeText(HijoActivity.this, "Código inválido", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        });

        btnActivarPermiso.setOnClickListener(v -> abrirSiguientePermisoFaltante());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!etCodigo.getText().toString().isEmpty()) {
            verificarYGuiarPermisos();
        }
    }

    private void verificarYGuiarPermisos() {
        boolean tieneUso = tienePermisoDeUso();
        boolean esAdmin = devicePolicyManager.isAdminActive(componentName);
        boolean sinBateria = esIgnoranteDeBateria();

        if (tieneUso && esAdmin && sinBateria) {
            // TODO PERFECTO
            layoutPermisos.setVisibility(View.GONE);
            layoutExito.setVisibility(View.VISIBLE);
            startService(new Intent(this, ServicioMonitoreo.class));
        } else {
            layoutPermisos.setVisibility(View.VISIBLE);
            layoutExito.setVisibility(View.GONE);
            
            if (!tieneUso) {
                tvEstadoPermisos.setText("1️⃣ Falta: Permiso de 'Acceso a datos de uso'");
                btnActivarPermiso.setText("ACTIVAR ACCESO DE USO");
                btnActivarPermiso.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_light));
            } else if (!esAdmin) {
                tvEstadoPermisos.setText("2️⃣ Falta: Permiso de 'Administrador del dispositivo'");
                btnActivarPermiso.setText("ACTIVAR ADMINISTRADOR");
                btnActivarPermiso.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
            } else if (!sinBateria) {
                tvEstadoPermisos.setText("3️⃣ Falta: Permiso de 'Sin restricciones de batería' (Clave para Xiaomi)");
                btnActivarPermiso.setText("ACTIVAR SIN RESTRICCIONES");
                btnActivarPermiso.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
            }
        }
    }

    private void abrirSiguientePermisoFaltante() {
        if (!tienePermisoDeUso()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else if (!devicePolicyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Necesario para bloquear la tablet remotamente.");
            startActivity(intent);
        } else if (!esIgnoranteDeBateria()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    private boolean tienePermisoDeUso() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean esIgnoranteDeBateria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(getPackageName());
        }
        return true;
    }
}