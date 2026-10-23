package com.controlparental.app;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HijoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hijo);

        TextView tvEstado = findViewById(R.id.tv_estado_monitoreo);
        Button btnPermiso = findViewById(R.id.btn_permiso_uso);
        Button btnCambiarRol = findViewById(R.id.btn_cambiar_rol);

        // Verificar si ya tenemos el permiso
        if (tienePermisoDeUso()) {
            tvEstado.setText("✅ Monitoreo Activo\nEnviando datos a papá/mamá...");
            tvEstado.setTextColor(0xFF2E7D32); // Verde
            iniciarServicio();
        }

        btnPermiso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir la pantalla de ajustes de Android para dar permiso
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });

        btnCambiarRol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
                prefs.edit().clear().apply();
                finish(); // Vuelve a la pantalla de selección
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Revisar el permiso cada vez que la pantalla vuelve a estar visible
        if (tienePermisoDeUso()) {
            TextView tvEstado = findViewById(R.id.tv_estado_monitoreo);
            tvEstado.setText("✅ Monitoreo Activo\nEnviando datos a papá/mamá...");
            tvEstado.setTextColor(0xFF2E7D32);
            iniciarServicio();
        }
    }

    private boolean tienePermisoDeUso() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void iniciarServicio() {
        Intent serviceIntent = new Intent(this, ServicioMonitoreo.class);
        startService(serviceIntent);
    }
}