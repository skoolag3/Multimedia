package com.skoolage.multimedia;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class FalaActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private EditText editTextToSpeak;
    private TextView textResult;
    private boolean isTtsReady = false;
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fala);

        editTextToSpeak = findViewById(R.id.editTextToSpeak);
        textResult = findViewById(R.id.textResult);
        Button btnSpeak = findViewById(R.id.btnSpeak);
        Button btnListen = findViewById(R.id.btnListen);

        textToSpeech = new TextToSpeech(this, this);

        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            textResult.setText(matches.get(0));
                        }
                    }
                });

        btnSpeak.setOnClickListener(v -> speakText());
        btnListen.setOnClickListener(v -> startSpeechRecognition());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("pt", "BR"));
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Idioma pt-BR não suportado pelo TTS", Toast.LENGTH_SHORT).show();
            } else {
                isTtsReady = true;
            }
        } else {
            Toast.makeText(this, "Falha ao inicializar o Text-to-Speech", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakText() {
        String text = editTextToSpeak.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Digite um texto primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isTtsReady) {
            Toast.makeText(this, "TTS ainda não está pronto", Toast.LENGTH_SHORT).show();
            return;
        }

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance");
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale algo...");

        try {
            speechRecognizerLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Reconhecimento de voz não disponível neste dispositivo",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
