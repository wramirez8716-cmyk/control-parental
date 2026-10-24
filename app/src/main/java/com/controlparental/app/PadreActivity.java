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

    private TextView tvCodigo, tvAppActiva;
    private Button btnGenerar, btnSalir;
    private DatabaseReference dbRef;
    private String codigoActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padre);

        tvCodigo = findViewById(R.id.tv_codigo);
        tvAppActiva = findViewById(R.id.tv_app_activa);
        btnGenerar = findViewById(R.id.btn_generar);
        btnSalir = findViewById(R.id.btn_salir);
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
            
            // Guardar en Firebase y Local
            dbRef.child("codigos").child(codigoActual).setValue("activo");
            prefs.edit().putString("codigo_familia", codigoActual).apply();

            tvCodigo.setText(codigoActual);
            btnGenerar.setText("Código Activo");
            btnGenerar.setEnabled(false);
            Toast.makeText(this, "Código generado: " + codigoActual, Toast.LENGTH_LONG).show();
            escucharDatos();
        });

        btnSalir.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            finish();
        });
    }

    private void escucharDatos() {
        dbRef.child("familias").child(codigoActual).child("app_activa")
            .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String app = snapshot.getValue(String.class);
                tvAppActiva.setText(app != null ? app : "En espera...");
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}