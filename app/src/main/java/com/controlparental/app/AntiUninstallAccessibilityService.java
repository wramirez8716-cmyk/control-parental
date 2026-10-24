package com.controlparental.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AntiUninstallAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            String className = event.getClassName() != null ? event.getClassName().toString() : "";
            
            // Lista de patrones que indican intento de desinstalación
            boolean esIntentoDesinstalacion = 
                packageName.contains("packageinstaller") ||
                packageName.contains("com.android.settings") ||
                className.contains("UninstallerActivity") ||
                className.contains("PackageInstallerActivity") ||
                className.contains("ManageApplications") ||
                className.contains("InstalledAppDetails") ||
                className.contains("DeviceAdminSettings") ||
                className.contains("AccessibilitySettings") ||
                (packageName.equals("com.android.settings") && className.contains("Activity"));

            if (esIntentoDesinstalacion) {
                // Verificar si es nuestra propia app la que está siendo atacada
                if (esNuestraApp(packageName, className)) {
                    bloquearDispositivo();
                }
            }
        }
    }

    private boolean esNuestraApp(String packageName, String className) {
        // Si está en la pantalla de detalles de nuestra app
        return packageName.equals("com.android.settings") && 
               (className.contains("InstalledAppDetails") || className.contains("ManageApplications"));
    }

    private void bloquearDispositivo() {
        DevicePolicyManager devicePolicyManager = 
            (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        
        if (devicePolicyManager != null && devicePolicyManager.isAdminActive(componentName)) {
            // Bloquear inmediatamente
            devicePolicyManager.lockNow();
            
            // También cerrar la app de ajustes
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }
    }

    @Override
    public void onInterrupt() {
        // No hacer nada
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }
}