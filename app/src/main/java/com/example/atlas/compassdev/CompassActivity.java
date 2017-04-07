package com.example.atlas.compassdev;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mGravityValues = new float[3];
    private float[] mGeomagneticValues = new float[3];
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationMatrix = new float[3];
    private float mAzimuth = 0f;
    private float mCurrentAzimuth = 0;
    private ImageView mCompassImg;
    private TextView x_value;
    private TextView y_value;
    private TextView z_value;
    private final float alpha = 0.90f;
    private Vibrator vibrator;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateSensorTypes();
        mCompassImg = (ImageView) findViewById(R.id.compass);
        timer = new Timer();

        x_value = (TextView) findViewById(R.id.input_x);
        y_value = (TextView) findViewById(R.id.input_y);
        z_value = (TextView) findViewById(R.id.input_z);

        // Setup timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                vibrate();
            }
        }, 0, 250);
    }

    public void initiateSensorTypes() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI); // Initialize accelerometer listener
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI); //  Initialize magnetometer listener
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); // Stop receiving updates
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing to do here
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {

            // Low pass filter

            if (event.sensor == mAccelerometer) {

                // If accelerometer values changed, update to new values

                mGravityValues[0] = alpha * mGravityValues[0] + (1 - alpha)
                        * event.values[0];
                mGravityValues[1] = alpha * mGravityValues[1] + (1 - alpha)
                        * event.values[1];
                mGravityValues[2] = alpha * mGravityValues[2] + (1 - alpha)
                        * event.values[2];
            }

            if (event.sensor == mMagnetometer) {

                // If magnetometer values changed, update to new values

                mGeomagneticValues[0] = alpha * mGeomagneticValues[0] + (1 - alpha)
                        * event.values[0];
                mGeomagneticValues[1] = alpha * mGeomagneticValues[1] + (1 - alpha)
                        * event.values[1];
                mGeomagneticValues[2] = alpha * mGeomagneticValues[2] + (1 - alpha)
                        * event.values[2];
            }

            // Fetch rotation matrix from accelerometer and magnetometer values

            boolean success = SensorManager.getRotationMatrix(mRotationMatrix, null, mGravityValues,
                    mGeomagneticValues);

            if (success) {
                SensorManager.getOrientation(mRotationMatrix, mOrientationMatrix);
                mAzimuth = (float) Math.toDegrees(mOrientationMatrix[0]);
                mAzimuth = (mAzimuth + 360) % 360; // Orientation in degrees
                animateMovement();
                updateAccelerometerValues();
            }
        }
    }

    private void animateMovement() {

        float mAzimuthInDegrees = (float) (Math.toDegrees(mOrientationMatrix[0])+360) % 360;

        Log.i("Current degree", Float.toString(mAzimuthInDegrees));

        Animation an = new RotateAnimation(-mCurrentAzimuth, -mAzimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        an.setDuration(250);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        mCompassImg.startAnimation(an);
        mCurrentAzimuth = mAzimuth;
    }

    public void updateAccelerometerValues() {
        x_value.setText(Float.toString(mGravityValues[0]));
        y_value.setText(Float.toString(mGravityValues[1]));
        z_value.setText(Float.toString(mGravityValues[2]));
    }

    // Vibrate if degree in +- 15 from north
    public void vibrate() {
        if ((mCurrentAzimuth >= 345 || mCurrentAzimuth <= 15)) {
            vibrator.vibrate(10);
        }
    }

}