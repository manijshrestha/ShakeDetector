package com.manijshrestha.shakedetector.demo;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.manijshrestha.shakedetector.ShakeDetector;


public class DemoActivity extends AppCompatActivity implements ShakeDetector.Listener {

    private static final String TAG = "DemoActivity";

    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mShakeDetector = new ShakeDetector(sensorManager, this);
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
    }

    @Override
    public void onShake() {
        Log.d(TAG, "Shaking");
    }

    @Override
    public void onShakeStop() {
        Log.d(TAG, "Shaking Ended");
    }
}
