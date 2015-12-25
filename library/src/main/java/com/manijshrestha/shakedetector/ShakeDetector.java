package com.manijshrestha.shakedetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

public class ShakeDetector implements SensorEventListener {

    public interface Listener {

        void onShakeStart();

        void onShake();

        void onShakeStop();

    }

    private static final String TAG = "ShakeDetector";

    private static final int DETECTION_INTERVAL = 500;
    private static final float SHAKE_FORCE = 2.5f;
    private static final double SHAKE_PERCENT = 0.1;

    private Listener mListener;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mSensitivityThreshold;
    private boolean mIsShaking;
    private long mLastCheck;
    private PriorityBlockingQueue<SensorData> mSensorDataQueue;
    private ArrayList<SensorData> mSensorDataArrayList;

    public ShakeDetector(@NonNull SensorManager sensorManager, @NonNull Listener listener) {
        mListener = listener;
        mSensorManager = sensorManager;
        mSensitivityThreshold = SHAKE_FORCE;
        mSensorDataQueue = new PriorityBlockingQueue<>();
        mSensorDataArrayList = new ArrayList<>();
    }

    public boolean register() {
        if (mAccelerometer != null) {
            return true;
        }

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mAccelerometer != null) {
            mIsShaking = false;
            mSensorDataQueue.clear();
            mSensorDataArrayList.clear();
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        return mAccelerometer != null;
    }

    public void unregister() {
        if (mAccelerometer != null) {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mAccelerometer = null;
            mSensorDataQueue.clear();
            mSensorDataArrayList.clear();
        }
        mIsShaking = false;
    }

    public void onDestroy() {
        mAccelerometer = null;
        mSensorManager = null;
        mListener = null;
        mSensorDataQueue = null;
    }

    private boolean isShaking(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];


        final double magnitudeSquared = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        return magnitudeSquared > mSensitivityThreshold;
    }

    private void evaluateSensorData(ArrayList<SensorData> mSensorData) {
        if (!mSensorData.isEmpty()) {
            double total = mSensorData.size();
            double shaking = 0;
            for (int i = 0; i < mSensorData.size(); i++) {
                SensorData sensorData = mSensorData.get(i);
                if (sensorData.isShaking()) {
                    ++shaking;
                }
            }

            double shakePercent = shaking / total;
            Log.d(TAG, "SHAKING: " + shaking + " TOTAL: " + total + " %: " + shakePercent);
            if (shakePercent > SHAKE_PERCENT) {
                if (!mIsShaking) {
                    mIsShaking = true;
                    mListener.onShakeStart();
                }

                mListener.onShake();
            } else {
                if (mIsShaking) {
                    mIsShaking = false;
                    mListener.onShakeStop();
                }
            }
        }
    }

    //region SensorEventListener
    @Override
    public void onSensorChanged(SensorEvent event) {
        // sanity check
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            boolean isShaking = isShaking(event);
            long timestamp = event.timestamp;

            // Movement
            SensorData sensorData = new SensorData(isShaking, timestamp);
            mSensorDataQueue.add(sensorData);

            long current = System.currentTimeMillis();

            if (mLastCheck == 0) {
                mLastCheck = current;
            }

            if (current - mLastCheck > DETECTION_INTERVAL) {
                mLastCheck = current;

                mSensorDataArrayList.clear();
                mSensorDataQueue.drainTo(mSensorDataArrayList);

                evaluateSensorData(mSensorDataArrayList);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //don't care
    }
    //endregion

    /**
     * Sensor Data used for sampling
     */
    private static class SensorData implements Comparable<SensorData> {
        private boolean mIsShaking;
        private long mTimeStamp;

        public SensorData(boolean isShaking, long timeStamp) {
            mIsShaking = isShaking;
            mTimeStamp = timeStamp;
        }

        public boolean isShaking() {
            return mIsShaking;
        }

        @Override
        public int compareTo(@NonNull SensorData another) {
            if (mTimeStamp > another.mTimeStamp) {
                return 1;
            } else if (mTimeStamp == another.mTimeStamp) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
