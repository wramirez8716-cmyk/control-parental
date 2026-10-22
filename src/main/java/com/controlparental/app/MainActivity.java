package com.controlparental.app;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el administrador de dispositivos
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);

        // Configurar el botón de activar administrador
        Button btnActivar = findViewById(R.id.btn_activar);
        btnActivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activarAdministrador();
            }
        });

        // Configurar el botón de bloquear
        Button btnBloquear = findViewById(R.id.btn_bloquear);
        btnBloquear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bloquearPantalla();
            }
        });
    }

    // Método para activar la app como administrador del dispositivo
    private void activarAdministrador() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Necesito permisos de administrador para bloquear la pantalla remotamente.");
        startActivityForResult(intent, 1);
    }

    // Método para bloquear la pantalla
    private void bloquearPantalla() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow();
            Toast.makeText(this, "Pantalla bloqueada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Primero activa el administrador", Toast.LENGTH_SHORT).show();
        }
    }
}