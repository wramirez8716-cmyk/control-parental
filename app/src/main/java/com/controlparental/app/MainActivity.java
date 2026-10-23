package com.controlparental.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private static final String TAG = "ControlParental";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        
        // DEBUG: Ver qué rol está guardado
        String rolGuardado = prefs.getString("rol", "null");
        Log.d(TAG, "Rol guardado en SharedPreferences: " + rolGuardado);

        // 1. Verificar si ya se eligió un rol antes
        if (!"null".equals(rolGuardado) && !rolGuardado.isEmpty()) {
            Log.d(TAG, "Cargando pantalla para: " + rolGuardado);
            if (rolGuardado.equals("padre")) {
                mostrarPanelPadre();
            } else if (rolGuardado.equals("hijo")) {
                mostrarPanelHijo();
            }
            return; // Salir del onCreate
        }

        // 2. Si no hay rol guardado, mostrar la pantalla de selección
        Log.d(TAG, "Mostrando pantalla de selección de rol");
        setContentView(R.layout.activity_main);

        Button btnPadre = findViewById(R.id.btn_padre);
        Button btnHijo = findViewById(R.id.btn_hijo);

        btnPadre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Usuario seleccionó: PADRE");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("rol", "padre");
                boolean guardado = editor.commit(); // Usar commit() en lugar de apply()
                Log.d(TAG, "¿Se guardó correctamente? " + guardado);
                
                Toast.makeText(MainActivity.this, "Configurado como Padre", Toast.LENGTH_SHORT).show();
                mostrarDialogoYReiniciar("padre");
            }
        });

        btnHijo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Usuario seleccionó: HIJO");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("rol", "hijo");
                boolean guardado = editor.commit();
                Log.d(TAG, "¿Se guardó correctamente? " + guardado);
                
                Toast.makeText(MainActivity.this, "Configurado como Hijo", Toast.LENGTH_SHORT).show();
                mostrarDialogoYReiniciar("hijo");
            }
        });
    }

    private void mostrarDialogoYReiniciar(final String rol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (rol.equals("padre")) {
            builder.setTitle("👨 Modo Padre");
            builder.setMessage("Bienvenido. Aquí verás la actividad de tus hijos en tiempo real.\n\nLa app se reiniciará para cargar tu panel.");
        } else {
            builder.setTitle("👦 Modo Hijo");
            builder.setMessage("Tu actividad está siendo monitoreada por tu padre/madre.\n\nLa app se reiniciará para activar el monitoreo.");
        }
        
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Cierra esta actividad
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void mostrarPanelPadre() {
        // Por ahora mostramos un mensaje simple
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" Panel del Padre");
        builder.setMessage("¡Bienvenido Padre!\n\nAquí se mostrará:\n- Lista de hijos conectados\n- App que están usando ahora\n- Tiempo de uso\n- Controles de bloqueo\n\n(Próximamente)");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void mostrarPanelHijo() {
        // Por ahora mostramos un mensaje simple
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👦 Modo Hijo Activo");
        builder.setMessage("Tu actividad está siendo monitoreada.\n\nDetectando apps en uso...\n(Próximamente)");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}