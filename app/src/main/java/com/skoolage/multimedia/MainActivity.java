package com.skoolage.multimedia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnGravador = findViewById(R.id.btnGravador);
        Button btnPlayer = findViewById(R.id.btnPlayer);
        Button btnFala = findViewById(R.id.btnFala);

        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));
        }

        if (btnGravador != null) {
            btnGravador.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AudioActivity.class)));
        }

        if (btnPlayer != null) {
            btnPlayer.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VideoPlayerActivity.class)));
        }

        if (btnFala != null) {
            btnFala.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FalaActivity.class)));
        }
    }
}
