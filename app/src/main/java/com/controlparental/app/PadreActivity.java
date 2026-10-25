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

    private TextView tvCodigo, tvAppActiva, tvHistorial, tvPendiente;
    private Button btnGenerar, btnBloquear, btnSalir, btnAprobar, btnBloquearApp;
    private DatabaseReference dbRef;
    private String codigoActual = "";
    private String appPendienteActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padre);

        tvCodigo = findViewById(R.id.tv_codigo);
        tvAppActiva = findViewById(R.id.tv_app_activa);
        tvHistorial = findViewById(R.id.tv_historial);
        tvPendiente = findViewById(R.id.tv_pendiente);
        btnGenerar = findViewById(R.id.btn_generar);
        btnBloquear = findViewById(R.id.btn_bloquear);
        btnSalir = findViewById(R.id.btn_salir);
        btnAprobar = findViewById(R.id.btn_aprobar);
        btnBloquearApp = findViewById(R.id.btn_bloquear_app);

        dbRef = FirebaseDatabase.getInstance().getReference();
        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        codigoActual = prefs.getString("codigo_familia", "");

        if (!codigoActual.isEmpty()) {
            tvCodigo.setText(codigoActual);
            btnGenerar.setText("Código Activo");
            btnGenerar.setEnabled(false);
            escucharDatos();
        }

        btnGenerar.setOnClickListener(v -> {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
            codigoActual = sb.toString();
            dbRef.child("codigos").child(codigoActual).setValue("activo");
            prefs.edit().putString("codigo_familia", codigoActual).apply();
            tvCodigo.setText(codigoActual);
            btnGenerar.setText("Código Activo");
            btnGenerar.setEnabled(false);
            Toast.makeText(this, "Código: " + codigoActual, Toast.LENGTH_LONG).show();
            escucharDatos();
        });

        btnBloquear.setOnClickListener(v -> {
            if (!codigoActual.isEmpty()) {
                dbRef.child("familias").child(codigoActual).child("orden").setValue("BLOQUEAR");
                Toast.makeText(this, "¡Tablet bloqueada!", Toast.LENGTH_SHORT).show();
            }
        });

        // NUEVO: Botón Aprobar
        btnAprobar.setOnClickListener(v -> {
            if (!appPendienteActual.isEmpty()) {
                dbRef.child("familias").child(codigoActual).child("pendientes").child(appPendienteActual).removeValue();
                dbRef.child("familias").child(codigoActual).child("aprobadas").child(appPendienteActual).setValue("si");
                Toast.makeText(this, "App aprobada", Toast.LENGTH_SHORT).show();
                appPendienteActual = "";
                tvPendiente.setText("Ninguna app pendiente.");
            }
        });

        // NUEVO: Botón Bloquear App
        btnBloquearApp.setOnClickListener(v -> {
            if (!appPendienteActual.isEmpty()) {
                dbRef.child("familias").child(codigoActual).child("pendientes").child(appPendienteActual).removeValue();
                dbRef.child("familias").child(codigoActual).child("bloqueadas").child(appPendienteActual).setValue("si");
                Toast.makeText(this, "App bloqueada. Se cerrará si el niño la abre.", Toast.LENGTH_LONG).show();
                appPendienteActual = "";
                tvPendiente.setText("Ninguna app pendiente.");
            }
        });

        btnSalir.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            finish();
        });
    }

    private void escucharDatos() {
        DatabaseReference familiaRef = dbRef.child("familias").child(codigoActual);
        
        familiaRef.child("app_activa").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String app = snapshot.getValue(String.class);
                tvAppActiva.setText(app != null ? app : "En espera...");
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        familiaRef.child("historial").limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder historialTexto = new StringBuilder("📋 Historial:\n");
                for (DataSnapshot child : snapshot.getChildren()) {
                    historialTexto.append("• ").append(child.getValue(String.class)).append("\n");
                }
                tvHistorial.setText(historialTexto.toString());
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // NUEVO: Escuchar apps pendientes
        familiaRef.child("pendientes").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Tomar la última app pendiente
                    DataSnapshot lastChild = snapshot.getChildren().iterator().next();
                    appPendienteActual = lastChild.getKey();
                    tvPendiente.setText("⚠️ Nueva app: " + appPendienteActual);
                } else {
                    appPendienteActual = "";
                    tvPendiente.setText("Ninguna app pendiente.");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}