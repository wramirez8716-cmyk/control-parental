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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HijoActivity extends Activity {

    private EditText etCodigo;
    private Button btnConectar, btnPermiso;
    private TextView tvEstado;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hijo);

        etCodigo = findViewById(R.id.et_codigo);
        btnConectar = findViewById(R.id.btn_conectar);
        btnPermiso = findViewById(R.id.btn_permiso);
        tvEstado = findViewById(R.id.tv_estado);
        dbRef = FirebaseDatabase.getInstance().getReference();

        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        String codigoGuardado = prefs.getString("codigo_familia", "");

        if (!codigoGuardado.isEmpty()) {
            etCodigo.setText(codigoGuardado);
            etCodigo.setEnabled(false);
            btnConectar.setVisibility(View.GONE);
            verificarPermisos();
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
                        prefs.edit().putString("codigo_familia", codigo).apply();
                        Toast.makeText(HijoActivity.this, "¡Conectado!", Toast.LENGTH_LONG).show();
                        etCodigo.setEnabled(false);
                        btnConectar.setVisibility(View.GONE);
                        verificarPermisos();
                    } else {
                        Toast.makeText(HijoActivity.this, "Código inválido", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        });

        btnPermiso.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!etCodigo.getText().toString().isEmpty()) {
            verificarPermisos();
        }
    }

    private void verificarPermisos() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        
        if (mode == AppOpsManager.MODE_ALLOWED) {
            tvEstado.setText("✅ Monitoreo Activo\nEnviando datos a papá...");
            tvEstado.setTextColor(0xFF2E7D32);
            btnPermiso.setVisibility(View.GONE);
            startService(new Intent(this, ServicioMonitoreo.class));
        } else {
            tvEstado.setText("⚠️ Falta permiso de uso.\nToca el botón para activarlo.");
            tvEstado.setTextColor(0xFFFF9800);
            btnPermiso.setVisibility(View.VISIBLE);
        }
    }
}