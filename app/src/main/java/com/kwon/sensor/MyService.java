package com.kwon.sensor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service implements SensorEventListener {

    SensorManager mSensorManager;
    Sensor mSensor_Accelerometer;
    double[] mGravity = new double[90];
    int x = 0, y = 1, z = 2, i = 0;

    private double[] res = new double[90];

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor_Accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager.registerListener(this, mSensor_Accelerometer, SensorManager.SENSOR_DELAY_UI);

        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //데이터 처리 필요
        //90개씩 끊을 코드 필요

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(z < 90){
                res[x] = event.values[0];
                res[y] = event.values[1];
                res[z] = event.values[2];

                Log.i("service", res[x] + ", " + res[y] + ", " + res[z]);
                x += 3; y += 3; z += 3;
            }
            else{
                x = 0; y = 1; z = 2;
                Intent myFilteredResponse = new Intent("com.kwon.sensor.step");
                myFilteredResponse.putExtra("serviceData", res);
                sendBroadcast(myFilteredResponse);
            }
//            if (z >= 90) {
//                x = 0;
//                y = 1;
//                z = 2;
//            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        mSensorManager.unregisterListener(this);
    }
}
