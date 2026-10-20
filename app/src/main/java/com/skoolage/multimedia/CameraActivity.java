
package com.skoolage.multimedia;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    PreviewView campoImagem;
    Button btnFoto, btnVideo;

    ImageCapture imageCapture;
    VideoCapture<Recorder> videoCapture;

    Recording actionRecording;
    ExecutorService cameraExecutor;
    boolean isRecording;

    private static final int REQUEST_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        campoImagem = findViewById(R.id.campoImagem);
        btnFoto = findViewById(R.id.btnFoto);
        btnVideo = findViewById(R.id.btnVideo);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (verificarPermissoes()) {
            iniciarCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_PERMISSIONS
            );
        }
    }

    private boolean verificarPermissoes() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_PERMISSIONS
                && verificarPermissoes()) {
            iniciarCamera();
        } else if (requestCode == REQUEST_PERMISSIONS) {
            Toast.makeText(
                    this,
                    "Permissões de acesso não concedidas",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    private void iniciarCamera() {
        ListenableFuture<ProcessCameraProvider> cameraFuture =
                ProcessCameraProvider.getInstance(this);

        cameraFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider =
                        cameraFuture.get();

                Preview preview = new Preview.Builder()
                        .build();

                preview.setSurfaceProvider(
                        campoImagem.getSurfaceProvider()
                );

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(
                                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                        )
                        .build();

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(
                                QualitySelector.from(Quality.HD)
                        )
                        .build();

                videoCapture = VideoCapture.withOutput(recorder);

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture,
                        videoCapture
                );

            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Erro ao iniciar câmera: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
