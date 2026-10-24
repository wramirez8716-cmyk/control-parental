package com.controlparental.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private Button btnConectar, btnPermisos;
    private TextView tvEstado;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hijo);

        etCodigo = findViewById(R.id.et_codigo_familia);
        btnConectar = findViewById(R.id.btn_conectar);
        btnPermisos = findViewById(R.id.btn_permisos);
        tvEstado = findViewById(R.id.tv_estado_permisos);

        dbRef = FirebaseDatabase.getInstance().getReference();

        // Verificar si ya hay código guardado
        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        String codigoGuardado = prefs.getString("codigo_familia", "");

        if (!codigoGuardado.isEmpty()) {
            etCodigo.setText(codigoGuardado);
            etCodigo.setEnabled(false);
            btnConectar.setText("Conectado");
            btnConectar.setEnabled(false);
            verificarYActivarPermisos();
        }

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codigo = etCodigo.getText().toString().trim().toUpperCase();
                
                if (codigo.length() != 6) {
                    Toast.makeText(HijoActivity.this, 
                        "El código debe tener 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si el código existe en Firebase
                dbRef.child("codigos").child(codigo).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Código válido, guardar y conectar
                                SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
                                prefs.edit().putString("codigo_familia", codigo).apply();
                                
                                etCodigo.setEnabled(false);
                                btnConectar.setText("Conectado");
                                btnConectar.setEnabled(false);
                                
                                Toast.makeText(HijoActivity.this, 
                                    "¡Conectado exitosamente!", Toast.LENGTH_LONG).show();
                                
                                verificarYActivarPermisos();
                            } else {
                                Toast.makeText(HijoActivity.this, 
                                    "Código inválido. Verifica con tus padres.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(HijoActivity.this, 
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        btnPermisos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí iría la lógica para pedir permisos de uso
                // Por ahora solo mostramos un mensaje
                Toast.makeText(HijoActivity.this, 
                    "Configurando permisos...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarYActivarPermisos() {
        tvEstado.setText("✅ Conectado a la familia\nActivando monitoreo...");
        tvEstado.setTextColor(0xFF2E7D32);
        
        // Iniciar el servicio de monitoreo
        Intent serviceIntent = new Intent(this, ServicioMonitoreo.class);
        startService(serviceIntent);
        
        btnPermisos.setVisibility(View.GONE);
    }
}