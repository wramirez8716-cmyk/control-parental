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
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String packageName = intent.getData().getSchemeSpecificPart();
            
            // Ignorar si es nuestra propia app o actualizaciones del sistema
            if (packageName.equals("com.controlparental.app") || packageName.startsWith("com.android")) {
                return;
            }

            SharedPreferences prefs = context.getSharedPreferences("ControlParental", Context.MODE_PRIVATE);
            String codigo = prefs.getString("codigo_familia", "");
            
            if (!codigo.isEmpty()) {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("familias").child(codigo);
                // Enviar a la lista de pendientes
                db.child("pendientes").child(packageName).setValue("pendiente");
            }
        }
    }
}