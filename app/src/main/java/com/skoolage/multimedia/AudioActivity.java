package com.skoolage.multimedia;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class AudioActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 200;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private WaveformView waveformView;
    private String audioFilePath;
    private boolean isRecording = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable amplitudeUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaRecorder != null && isRecording) {
                int amplitude = mediaRecorder.getMaxAmplitude();
                waveformView.addAmplitude(amplitude);
                handler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        waveformView = findViewById(R.id.waveformView);
        Button btnRecord = findViewById(R.id.btnRecord);
        Button btnPlay = findViewById(R.id.btnPlay);

        audioFilePath = getExternalFilesDir(null) + "/recording.3gp";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }

        btnRecord.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
                btnRecord.setText("Gravar");
            } else {
                startRecording();
                btnRecord.setText("Parar");
            }
        });

        btnPlay.setOnClickListener(v -> playRecording());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO &&
                (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Permissão de áudio negada", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRecording() {
        waveformView.reset();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            handler.post(amplitudeUpdater);
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;
        handler.removeCallbacks(amplitudeUpdater);
    }

    private void playRecording() {
        File file = new File(audioFilePath);
        if (!file.exists()) {
            Toast.makeText(this, "Nenhuma gravação encontrada", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao reproduzir áudio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
