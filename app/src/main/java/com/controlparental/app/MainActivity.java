package com.controlparental.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("ControlParental", MODE_PRIVATE);
        String rol = prefs.getString("rol", "");

        if ("padre".equals(rol)) {
            startActivity(new Intent(this, PadreActivity.class));
            finish();
        } else if ("hijo".equals(rol)) {
            startActivity(new Intent(this, HijoActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            
            findViewById(R.id.btn_padre).setOnClickListener(v -> {
                prefs.edit().putString("rol", "padre").apply();
                startActivity(new Intent(this, PadreActivity.class));
                finish();
            });

            findViewById(R.id.btn_hijo).setOnClickListener(v -> {
                prefs.edit().putString("rol", "hijo").apply();
                startActivity(new Intent(this, HijoActivity.class));
                finish();
            });
        }
    }
}