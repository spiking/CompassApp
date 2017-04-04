package com.example.atlas.compassdev;

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

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "Compass";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Vibrator mVibrator;
    private float[] mGravityValues = new float[3];
    private float[] mGeomagneticValues = new float[3];
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationMatrix = new float[3];
    private float azimuth = 0f;
    private float currectAzimuth = 0;
    private ImageView mCompassImg;
    private TextView x_value;
    private TextView y_value;
    private TextView z_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initiateSensorTypes();
        mCompassImg = (ImageView) findViewById(R.id.compass);
        x_value = (TextView) findViewById(R.id.input_x);
        y_value = (TextView) findViewById(R.id.input_y);
        z_value = (TextView) findViewById(R.id.input_z);
    }

    public void initiateSensorTypes() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI); // Active accelerometer listener
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI); //  Active magnetometer listener
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

    private void animateMovement() {

        float azimuthInDegrees = (float) (Math.toDegrees(mOrientationMatrix[0])+360) % 360;

        Log.i("Current degree", Float.toString(azimuthInDegrees));

        Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        an.setDuration(250);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        mCompassImg.startAnimation(an);
        currectAzimuth = azimuth;

        updateAccelerometerValues();
    }

    public void updateScreenValues() {
        TextView degree = (TextView) findViewById(R.id.input_z);
        float azimuthInDegrees = (float) (Math.toDegrees(mOrientationMatrix[0])+360) % 360; // degrees of rotation about the -z axis


        degree.setText(Float.toString(azimuthInDegrees));
    }

    public void updateAccelerometerValues() {
        x_value.setText(Float.toString(mGravityValues[0]));
        y_value.setText(Float.toString(mGravityValues[1]));
        z_value.setText(Float.toString(mGravityValues[2]));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        final float alpha = 0.90f;

        synchronized (this) {

            if (event.sensor == mAccelerometer) {

                mGravityValues[0] = alpha * mGravityValues[0] + (1 - alpha)
                        * event.values[0];
                mGravityValues[1] = alpha * mGravityValues[1] + (1 - alpha)
                        * event.values[1];
                mGravityValues[2] = alpha * mGravityValues[2] + (1 - alpha)
                        * event.values[2];
            }

            if (event.sensor == mMagnetometer) {

                mGeomagneticValues[0] = alpha * mGeomagneticValues[0] + (1 - alpha)
                        * event.values[0];
                mGeomagneticValues[1] = alpha * mGeomagneticValues[1] + (1 - alpha)
                        * event.values[1];
                mGeomagneticValues[2] = alpha * mGeomagneticValues[2] + (1 - alpha)
                        * event.values[2];
                /*Log.e(TAG, Float.toString(event.values[0]));*/

            }

            boolean success = SensorManager.getRotationMatrix(mRotationMatrix, null, mGravityValues,
                    mGeomagneticValues);


            if (success) {
                SensorManager.getOrientation(mRotationMatrix, mOrientationMatrix);
                /*Log.d(TAG, "azimuth (rad): " + azimuth);*/
                azimuth = (float) Math.toDegrees(mOrientationMatrix[0]); // orientation
                azimuth = (azimuth + 360) % 360;
                /*Log.d(TAG, "azimuth (deg): " + azimuth);*/
                animateMovement();
            }
        }
    }

}