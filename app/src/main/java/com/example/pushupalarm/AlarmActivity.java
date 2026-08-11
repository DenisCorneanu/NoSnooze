package com.example.pushupalarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 200;
    private static final long REQUIRED_TIME_MS = 15000;

    private PreviewView previewView;
    private TextView poseStatusText;
    private TextView progressText;
    private TextView remainingText;
    private TextView alarmTimeText;
    private ProgressBar progressBar;
    private Button stopAlarmButton;

    private PoseDetector poseDetector;
    private MediaPlayer mediaPlayer;

    private long validTimeMs = 0;
    private long lastFrameTime = 0;

    private int validFrames = 0;
    private int invalidFrames = 0;

    private boolean challengeCompleted = false;
    private int alarmId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarmId = getIntent().getIntExtra("alarm_id", -1);

        previewView = findViewById(R.id.previewView);
        poseStatusText = findViewById(R.id.poseStatusText);
        progressText = findViewById(R.id.progressText);
        remainingText = findViewById(R.id.remainingText);
        alarmTimeText = findViewById(R.id.alarmTimeText);
        progressBar = findViewById(R.id.progressBar);
        stopAlarmButton = findViewById(R.id.stopAlarmButton);

        stopAlarmButton.setEnabled(false);
        stopAlarmButton.setAlpha(0.55f);

        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());
        alarmTimeText.setText(currentTime);

        stopAlarmButton.setOnClickListener(v -> {
            if (challengeCompleted) {
                completeChallenge();
            } else {
                Toast.makeText(this, "Ține poziția 15 secunde mai întâi.", Toast.LENGTH_SHORT).show();
            }
        });

        PoseDetectorOptions options =
                new PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build();

        poseDetector = PoseDetection.getClient(options);

        startAlarmSound();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );

        } else {
            startCamera();
        }
    }

    private void startAlarmSound() {
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        this::processImage
                );

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void processImage(ImageProxy imageProxy) {
        if (challengeCompleted) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image =
                InputImage.fromMediaImage(
                        imageProxy.getImage(),
                        imageProxy.getImageInfo().getRotationDegrees()
                );

        poseDetector.process(image)
                .addOnSuccessListener(pose -> {

                    boolean pushupPosition = isPushupPositionDetected(
                            pose.getPoseLandmark(PoseLandmark.NOSE),
                            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
                            pose.getPoseLandmark(PoseLandmark.LEFT_WRIST),
                            pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST),
                            imageProxy.getWidth(),
                            imageProxy.getHeight()
                    );

                    long now = System.currentTimeMillis();

                    if (lastFrameTime == 0) {
                        lastFrameTime = now;
                    }

                    long delta = now - lastFrameTime;
                    lastFrameTime = now;

                    if (delta > 700) {
                        delta = 300;
                    }

                    if (pushupPosition) {
                        validFrames++;
                        invalidFrames = 0;
                    } else {
                        invalidFrames++;

                        if (invalidFrames > 5) {
                            validFrames = 0;
                        }
                    }

                    boolean stablePushup = validFrames >= 2;

                    if (stablePushup) {
                        validTimeMs += delta;

                        poseStatusText.setText("● poziție detectată");
                        poseStatusText.setBackgroundResource(R.drawable.bg_pose_good);

                    } else {
                        validTimeMs = Math.max(0, validTimeMs - delta * 2);

                        poseStatusText.setText("poziție nedetectată");
                        poseStatusText.setBackgroundResource(R.drawable.bg_pose_bad);
                    }

                    updateProgressUI();

                    if (validTimeMs >= REQUIRED_TIME_MS) {
                        challengeCompleted = true;
                        stopAlarmButton.setEnabled(true);
                        stopAlarmButton.setAlpha(1f);
                        stopAlarmButton.setText("provocare completă ✓");
                        completeChallenge();
                    }

                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private boolean isPushupPositionDetected(
            PoseLandmark nose,
            PoseLandmark leftShoulder,
            PoseLandmark rightShoulder,
            PoseLandmark leftWrist,
            PoseLandmark rightWrist,
            int imageWidth,
            int imageHeight
    ) {
        if (nose == null ||
                leftShoulder == null ||
                rightShoulder == null ||
                leftWrist == null ||
                rightWrist == null) {
            return false;
        }

        float noseX = nose.getPosition().x;
        float noseY = nose.getPosition().y;

        float leftShoulderX = leftShoulder.getPosition().x;
        float leftShoulderY = leftShoulder.getPosition().y;

        float rightShoulderX = rightShoulder.getPosition().x;
        float rightShoulderY = rightShoulder.getPosition().y;

        float leftWristX = leftWrist.getPosition().x;
        float leftWristY = leftWrist.getPosition().y;

        float rightWristX = rightWrist.getPosition().x;
        float rightWristY = rightWrist.getPosition().y;

        float shoulderDistance = Math.abs(leftShoulderX - rightShoulderX);
        float wristDistance = Math.abs(leftWristX - rightWristX);

        float avgShoulderX = (leftShoulderX + rightShoulderX) / 2f;
        float avgShoulderY = (leftShoulderY + rightShoulderY) / 2f;
        float avgWristY = (leftWristY + rightWristY) / 2f;

        boolean shouldersVisible =
                shoulderDistance > imageWidth * 0.12f;

        boolean shouldersReasonablyHorizontal =
                Math.abs(leftShoulderY - rightShoulderY) < shoulderDistance * 0.80f;

        boolean headNearShoulderCenter =
                Math.abs(noseX - avgShoulderX) < shoulderDistance * 0.90f;

        boolean headAboveOrNearShoulders =
                noseY < avgShoulderY + shoulderDistance * 0.35f;

        boolean wristsBelowShoulders =
                avgWristY > avgShoulderY + shoulderDistance * 0.35f;

        boolean wristsNearBottomHalf =
                avgWristY > imageHeight * 0.45f;

        boolean wristsWide =
                wristDistance > shoulderDistance * 0.85f;

        return shouldersVisible &&
                shouldersReasonablyHorizontal &&
                headNearShoulderCenter &&
                headAboveOrNearShoulders &&
                wristsBelowShoulders &&
                wristsNearBottomHalf &&
                wristsWide;
    }

    private void updateProgressUI() {
        int seconds = (int) (validTimeMs / 1000);

        if (seconds > 15) {
            seconds = 15;
        }

        int remaining = 15 - seconds;

        progressBar.setProgress(seconds);
        progressText.setText(seconds + " /15 sec");

        if (remaining > 0) {
            remainingText.setText("mai ține poziția încă " + remaining + " secunde");
        } else {
            remainingText.setText("gata, poți opri alarma");
        }
    }

    private void completeChallenge() {
        challengeCompleted = true;

        stopAlarmSound();

        Intent intent = new Intent(this, SuccessActivity.class);
        intent.putExtra("alarm_id", alarmId);

        startActivity(intent);
        finish();
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) {
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopAlarmSound();

        if (poseDetector != null) {
            poseDetector.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {
            Toast.makeText(this, "Camera este necesară pentru provocare.", Toast.LENGTH_LONG).show();
        }
    }
}