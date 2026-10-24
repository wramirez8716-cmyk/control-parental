package com.controlparental.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InstalacionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()) || 
            Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            
            // Obtener el nombre del paquete instalado (ej: com.whatsapp)
            String packageName = intent.getData().getSchemeSpecificPart();
            
            // Obtener el código de familia para saber a quién avisar
            SharedPreferences prefs = context.getSharedPreferences("ControlParental", Context.MODE_PRIVATE);
            String codigo = prefs.getString("codigo_familia", "");
            
            if (!codigo.isEmpty() && !packageName.equals("com.controlparental.app")) {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("familias").child(codigo);
                String mensaje = "⚠️ Nueva app instalada/actualizada: " + packageName;
                db.child("alerta_instalacion").setValue(mensaje);
                
                // Limpiar la alerta después de 10 segundos para no saturar
                db.child("alerta_instalacion").setValue("Sin alertas").addOnCompleteListener(task -> {
                    // Se limpia automáticamente
                });
            }
       
        }
    }
}