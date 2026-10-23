package com.controlparental.app;

import android.app.Activity;
import android.app.AlertDialog;
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
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);

        // Verificar si ya se eligió un rol antes
        String rol = prefs.getString("rol", "");
        if (!rol.isEmpty()) {
            // Ya tiene rol guardado, ir directo a esa pantalla
            if (rol.equals("padre")) {
                iniciarPantallaPadre();
            } else if (rol.equals("hijo")) {
                iniciarPantallaHijo();
            }
            return; // Salir del onCreate para no mostrar la pantalla de selección
        }

        // Configurar botones de selección
        Button btnPadre = findViewById(R.id.btn_padre);
        Button btnHijo = findViewById(R.id.btn_hijo);

        btnPadre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Guardar rol como padre
                prefs.edit().putString("rol", "padre").apply();
                Toast.makeText(MainActivity.this, "Configurado como Padre", Toast.LENGTH_SHORT).show();
                iniciarPantallaPadre();
            }
        });

        btnHijo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Guardar rol como hijo
                prefs.edit().putString("rol", "hijo").apply();
                Toast.makeText(MainActivity.this, "Configurado como Hijo", Toast.LENGTH_SHORT).show();
                iniciarPantallaHijo();
            }
        });
    }

    private void iniciarPantallaPadre() {
        // Por ahora solo mostramos un mensaje
        // Después aquí irá la pantalla del padre con lista de hijos
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👨 Modo Padre");
        builder.setMessage("Bienvenido. Aquí verás la actividad de tus hijos en tiempo real.\n\n(Próximamente: lista de hijos conectados)");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void iniciarPantallaHijo() {
        // Por ahora solo mostramos un mensaje
        // Después aquí irá la pantalla del hijo con monitoreo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👦 Modo Hijo");
        builder.setMessage("Tu actividad está siendo monitoreada por tu padre/madre.\n\n(Próximamente: envío de datos en tiempo real)");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}