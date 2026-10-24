package com.controlparental.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class PadreActivity extends Activity {

    private TextView tvCodigo, tvAppActiva, tvHistorial;
    private Button btnGenerar, btnBloquear, btnSalir;
    private DatabaseReference dbRef;
    private String codigoActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padre);

        // Conectar los elementos del diseño
        tvCodigo = findViewById(R.id.tv_codigo);
        tvAppActiva = findViewById(R.id.tv_app_activa);
        tvHistorial = findViewById(R.id.tv_historial);
        btnGenerar = findViewById(R.id.btn_generar);
        btnBloquear = findViewById(R.id.btn_bloquear);
        btnSalir = findViewById(R.id.btn_salir);

        // Conectar con Firebase
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Verificar si ya hay un código guardado
        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        codigoActual = prefs.getString("codigo_familia", "");

        if (!codigoActual.isEmpty()) {
            tvCodigo.setText(codigoActual);
            btnGenerar.setText("Código Activo");
            btnGenerar.setEnabled(false);
            escucharDatos();
        }

        // Botón para generar nuevo código
        btnGenerar.setOnClickListener(v -> {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            
            codigoActual = sb.toString();
            
            // Guardar en Firebase para que el hijo pueda validarlo
            dbRef.child("codigos").child(codigoActual).setValue("activo");
            
            // Guardar localmente
            prefs.edit().putString("codigo_familia", codigoActual).apply();

            tvCodigo.setText(codigoActual);
            btnGenerar.setText("Código Activo");
            btnGenerar.setEnabled(false);
            Toast.makeText(this, "Código generado: " + codigoActual, Toast.LENGTH_LONG).show();
            escucharDatos();
        });

        // Botón de bloqueo remoto
        btnBloquear.setOnClickListener(v -> {
            if (!codigoActual.isEmpty()) {
                dbRef.child("familias").child(codigoActual).child("orden").setValue("BLOQUEAR");
                Toast.makeText(this, "¡Orden de bloqueo enviada!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Primero genera un código", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para cerrar sesión
        btnSalir.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            finish();
        });
    }

    private void escucharDatos() {
        DatabaseReference familiaRef = dbRef.child("familias").child(codigoActual);
        
        // 1. Escuchar app activa en tiempo real
        familiaRef.child("app_activa").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String app = snapshot.getValue(String.class);
                tvAppActiva.setText(app != null ? app : "En espera...");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PadreActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Escuchar historial de uso (últimos 10 registros)
        familiaRef.child("historial").limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder historialTexto = new StringBuilder("📋 Historial reciente:\n");
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        historialTexto.append("• ").append(child.getValue(String.class)).append("\n");
                    }
                } else {
                    historialTexto.append("(Sin datos aún)");
                }
                tvHistorial.setText(historialTexto.toString());
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // 3. Escuchar alertas de instalación de apps
        familiaRef.child("alerta_instalacion").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String alerta = snapshot.getValue(String.class);
                if (alerta != null && !alerta.equals("Sin alertas") && !alerta.isEmpty()) {
                    Toast.makeText(PadreActivity.this, alerta, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}