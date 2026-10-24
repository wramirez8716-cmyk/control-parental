package com.controlparental.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

public class AntiUninstallAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            String className = event.getClassName() != null ? event.getClassName().toString() : "";

            // Detectar si se abre la pantalla de desinstalación o de desactivar admin
            if (packageName.contains("packageinstaller") || 
                className.contains("UninstallerActivity") || 
                className.contains("DeviceAdminSettings") ||
                className.contains("ManageApplications")) {
                
                // ¡BLOQUEAR LA PANTALLA INMEDIATAMENTE!
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
                
                if (devicePolicyManager != null && devicePolicyManager.isAdminActive(componentName)) {
                    devicePolicyManager.lockNow(); // Bloquea la tablet al instante
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // No necesitamos hacer nada aquí
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = getServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }
}