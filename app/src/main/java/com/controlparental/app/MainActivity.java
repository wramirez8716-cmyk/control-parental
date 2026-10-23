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
        String rolGuardado = prefs.getString("rol", "");

        // 1. Si ya eligió rol antes, ir directo a esa pantalla sin preguntar
        if ("padre".equals(rolGuardado)) {
            Intent intent = new Intent(MainActivity.this, PadreActivity.class);
            startActivity(intent);
            finish();
            return;
        } else if ("hijo".equals(rolGuardado)) {
            Intent intent = new Intent(MainActivity.this, HijoActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 2. Si no hay rol guardado, mostrar la pantalla de selección
        setContentView(R.layout.activity_main);

        Button btnPadre = findViewById(R.id.btn_padre);
        Button btnHijo = findViewById(R.id.btn_hijo);

        // Botón del PADRE
        btnPadre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("rol", "padre").apply();
                Toast.makeText(MainActivity.this, "Modo Padre activado", Toast.LENGTH_SHORT).show();
                
                // Aquí abrimos la pantalla del padre
                Intent intent = new Intent(MainActivity.this, PadreActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Botón del HIJO
        btnHijo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("rol", "hijo").apply();
                Toast.makeText(MainActivity.this, "Modo Hijo activado", Toast.LENGTH_SHORT).show();
                
                // AQUÍ ES DONDE HICIMOS EL CAMBIO: Abre la pantalla del hijo
                Intent intent = new Intent(MainActivity.this, HijoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}