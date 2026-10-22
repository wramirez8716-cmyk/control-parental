package com.controlparental.app;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

    // Este método se ejecuta cuando activas la app como administrador
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Control Parental activado correctamente", Toast.LENGTH_SHORT).show();
    }

    // Este método se ejecuta si desactivas la app
    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "Control Parental desactivado", Toast.LENGTH_SHORT).show();
    }
}