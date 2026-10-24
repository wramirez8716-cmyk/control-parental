<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="20dp"
    android:background="#F5F5F5"
    android:gravity="center_horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Panel del Padre"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="30dp" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="#E3F2FD"
        android:padding="20dp"
        android:gravity="center"
        android:layout_marginBottom="20dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Tu Código de Familia:"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/tv_codigo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="------"
            android:textSize="40sp"
            android:textStyle="bold"
            android:textColor="#1565C0"
            android:layout_margin="10dp" />

        <Button
            android:id="@+id/btn_generar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Generar Código"
            android:backgroundTint="#1565C0" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#FFFFFF"
        android:padding="20dp"
        android:layout_marginBottom="20dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="App activa en tablet: "
            android:textSize="16sp" />

        <TextView
            android:id="@+id/tv_app_activa"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Esperando..."
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="#4CAF50"
            android:layout_marginLeft="10dp" />
    </LinearLayout>

    <Button
        android:id="@+id/btn_salir"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Cerrar Sesión"
        android:backgroundTint="#9E9E9E" />

</LinearLayout>