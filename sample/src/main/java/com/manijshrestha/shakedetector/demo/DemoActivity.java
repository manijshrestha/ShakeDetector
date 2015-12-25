package com.manijshrestha.shakedetector.demo;

import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.manijshrestha.shakedetector.ShakeDetector;


public class DemoActivity extends AppCompatActivity implements ShakeDetector.Listener {

    private static final String TAG = "DemoActivity";

    private ShakeDetector mShakeDetector;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mShakeDetector = new ShakeDetector(sensorManager, this);
        mMediaPlayer = MediaPlayer.create(this, R.raw.drum_sound);
        mMediaPlayer.setLooping(true);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mShakeDetector.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShakeDetector.unregister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShakeDetector.onDestroy();
    }

    @Override
    public void onShakeStart() {
        Log.d(TAG, "Shaking Started");
        mMediaPlayer.seekTo(0);
        mMediaPlayer.start();
    }

    @Override
    public void onShake() {
        Log.d(TAG, "Shaking");
    }

    @Override
    public void onShakeStop() {
        Log.d(TAG, "Shaking Ended");
        mMediaPlayer.pause();
    }
}
