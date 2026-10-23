package com.controlparental.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

        // 2. Si no hay rol guardado, mostrar la pantalla de selección
        setContentView(R.layout.activity_main);

        Button btnPadre = findViewById(R.id.btn_padre);
        Button btnHijo = findViewById(R.id.btn_hijo);

        btnPadre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("rol", "padre").apply();
                Toast.makeText(MainActivity.this, "Configurado como Padre", Toast.LENGTH_SHORT).show();
                iniciarPantallaPadre();
            }
        });

        btnHijo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("rol", "hijo").apply();
                Toast.makeText(MainActivity.this, "Configurado como Hijo", Toast.LENGTH_SHORT).show();
                iniciarPantallaHijo();
            }
        });
    }

    private void iniciarPantallaPadre() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👨 Modo Padre");
        builder.setMessage("Bienvenido. Aquí verás la actividad de tus hijos en tiempo real.\n\nLa app se reiniciará para cargar tu panel.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Cierra esta actividad para que al abrirla de nuevo cargue el panel
            }
        });
        builder.setCancelable(false); // Obliga a tocar OK
        builder.show();
    }

    private void iniciarPantallaHijo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👦 Modo Hijo");
        builder.setMessage("Tu actividad está siendo monitoreada por tu padre/madre.\n\nLa app se reiniciará para activar el monitoreo.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Cierra esta actividad para que al abrirla de nuevo cargue el monitoreo
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}