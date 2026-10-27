package com.skoolage.multimedia;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class VideoPlayerActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private Button btnPlayPause;
    private Button btnSpeed;
    private SeekBar seekBar;
    private boolean isPlaying = false;
    private boolean isSurfaceReady = false;
    private boolean isPrepared = false;
    private final float[] speeds = {0.5f, 1.0f, 1.5f, 2.0f};
    private int speedIndex = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPrepared) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        surfaceView = findViewById(R.id.surfaceView);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnSpeed = findViewById(R.id.btnSpeed);
        seekBar = findViewById(R.id.seekBar);

        setupSurfaceHolder();
        setupControls();
    }

    private void setupSurfaceHolder() {
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                isSurfaceReady = true;
                initMediaPlayer(holder);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                // Não é necessário tratamento adicional aqui
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                isSurfaceReady = false;
            }
        });
    }

    private void initMediaPlayer(SurfaceHolder holder) {
        mediaPlayer = new MediaPlayer();
        try {
            // Substitua pela URI real do vídeo (ex.: vindo de um arquivo, res/raw ou URL remota)
            //Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample_video);
            Uri videoUri = Uri.parse("https://www.youtube.com/watch?v=CD-E-LDc384");
            mediaPlayer.setDataSource(this, videoUri);
            mediaPlayer.setDisplay(holder);

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                seekBar.setMax(mp.getDuration());
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlayPause.setText("Play");
                handler.removeCallbacks(progressUpdater);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Erro ao reproduzir vídeo", Toast.LENGTH_SHORT).show();
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao carregar vídeo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupControls() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnSpeed.setOnClickListener(v -> cycleSpeed());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && isPrepared) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(progressUpdater);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) {
                    handler.post(progressUpdater);
                }
            }
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null || !isPrepared) return;

        if (isPlaying) {
            mediaPlayer.pause();
            btnPlayPause.setText("Play");
            handler.removeCallbacks(progressUpdater);
        } else {
            mediaPlayer.start();
            btnPlayPause.setText("Pause");
            handler.post(progressUpdater);
        }
        isPlaying = !isPlaying;
    }

    private void cycleSpeed() {
        if (mediaPlayer == null || !isPrepared) return;

        // PlaybackParams requer API 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            speedIndex = (speedIndex + 1) % speeds.length;
            float newSpeed = speeds[speedIndex];
            boolean wasPlaying = mediaPlayer.isPlaying();
            PlaybackParams params = mediaPlayer.getPlaybackParams();
            params.setSpeed(newSpeed);
            mediaPlayer.setPlaybackParams(params);
            if (wasPlaying && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            btnSpeed.setText(newSpeed + "x");
        } else {
            Toast.makeText(this, "Controle de velocidade requer API 23+", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            handler.removeCallbacks(progressUpdater);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressUpdater);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
