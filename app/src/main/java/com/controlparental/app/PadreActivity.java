package com.controlparental.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PadreActivity extends Activity {

    private TextView tvAppActiva, tvTiempoUso;
    private Button btnBloquear, btnCambiarRol;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padre);

        tvAppActiva = findViewById(R.id.tv_app_activa);
        tvTiempoUso = findViewById(R.id.tv_tiempo_uso);
        btnBloquear = findViewById(R.id.btn_bloquear);
        btnCambiarRol = findViewById(R.id.btn_cambiar_rol);

        // Conectar a Firebase (usamos una ruta de prueba por ahora)
        databaseReference = FirebaseDatabase.getInstance().getReference("hijos/tablet_prueba");

        // Escuchar cambios en tiempo real
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Leer la app activa
                String app = dataSnapshot.child("app_activa").getValue(String.class);
                if (app != null) {
                    tvAppActiva.setText(app);
                } else {
                    tvAppActiva.setText("Ninguna (Pantalla de inicio)");
                }

                // Leer el tiempo de uso (por ahora un valor de prueba)
                Long tiempo = dataSnapshot.child("tiempo_minutos").getValue(Long.class);
                if (tiempo != null) {
                    tvTiempoUso.setText(tiempo + " minutos");
                } else {
                    tvTiempoUso.setText("0 minutos");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PadreActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción del botón bloquear
        btnBloquear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Escribimos la orden en Firebase
                FirebaseDatabase.getInstance().getReference("ordenes/tablet_prueba")
                        .setValue("BLOQUEAR");
                Toast.makeText(PadreActivity.this, "Orden de bloqueo enviada", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción para cambiar de rol (borra los datos guardados)
        btnCambiarRol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
                prefs.edit().clear().apply();
                finish(); // Cierra y vuelve a la selección
            }
        });
    }
}