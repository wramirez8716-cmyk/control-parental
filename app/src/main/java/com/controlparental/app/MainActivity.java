package com.controlparental.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        
        // 1. Verificar si ya se eligió un rol antes
        String rolGuardado = prefs.getString("rol", "");
        
        if ("padre".equals(rolGuardado)) {
            // Si ya es padre, ir directo a la pantalla del padre
            Intent intent = new Intent(MainActivity.this, PadreActivity.class);
            startActivity(intent);
            finish(); // Cierra esta pantalla de selección
            return;
        } else if ("hijo".equals(rolGuardado)) {
            // Si ya es hijo (por ahora solo mostramos un mensaje)
            Toast.makeText(this, "Modo Hijo activo (En construcción)", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. Si no hay rol guardado, mostrar la pantalla de selección
        setContentView(R.layout.activity_main);

        Button btnPadre = findViewById(R.id.btn_padre);
        Button btnHijo = findViewById(R.id.btn_hijo);

        btnPadre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Guardar rol
                prefs.edit().putString("rol", "padre").apply();
                Toast.makeText(MainActivity.this, "Modo Padre activado", Toast.LENGTH_SHORT).show();
                
                // Ir a la pantalla del padre
                Intent intent = new Intent(MainActivity.this, PadreActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnHijo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Guardar rol
                prefs.edit().putString("rol", "hijo").apply();
                Toast.makeText(MainActivity.this, "Modo Hijo activado (En construcción)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}