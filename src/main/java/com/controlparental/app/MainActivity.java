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

// Importamos las librerías de Firebase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class MainActivity extends Activity {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Configurar el bloqueo de pantalla
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);

        // 2. Configurar Firebase (El "oído" de la app)
        // Esto se conecta a la base de datos en la nube
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("orden_de_bloqueo");

        // 3. Escuchar cambios en la base de datos
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Aquí recibimos el mensaje desde tu celular
                String orden = dataSnapshot.getValue(String.class);
                
                // Si la orden es "BLOQUEAR", ejecutamos el bloqueo
                if ("BLOQUEAR".equals(orden)) {
                    bloquearPantalla();
                    // Reiniciamos la orden para que no se bloquee infinitamente
                    databaseReference.setValue("ESPERANDO"); 
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Si hay error de conexión
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para activar permisos (solo se usa la primera vez)
        Button btnActivar = findViewById(R.id.btn_activar);
        btnActivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activarAdministrador();
            }
        });
    }

    private void activarAdministrador() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        startActivityForResult(intent, 1);
    }

    private void bloquearPantalla() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow(); // ¡Aquí ocurre la magia del bloqueo!
        }
    }
}