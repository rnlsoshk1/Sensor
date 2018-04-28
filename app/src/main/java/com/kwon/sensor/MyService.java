package com.kwon.sensor;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static java.lang.Math.abs;

public class MyService extends IntentService implements SensorEventListener {
    private KNearestNeighbors knn;
    public MyService() {
        super("MyService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    static double callThreshold = 0.02;

    SensorManager mSensorManager;
    Sensor mSensor_Accelerometer;
    private double currentArray[] = new double[3];
    private double basePoint[][] = null;
    Object[] objectArray;

    double[] array = new double[150];
    int i = 0;

    private boolean active = false;     //1초간 배열 담을 동안은 비활성화 해야함

    // temp
    int total = 0;
    int count = 0;

//    double[] mGravity = new double[90];
//    int x = 0, y = 1, z = 2, i = 0;
//    int a;
//    private double[] res = new double[90];

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
        currentArray = intent.getExtras().getDoubleArray("currentArray");
        objectArray = (Object[])intent.getExtras().getSerializable("basePoint");
        if(objectArray != null){
            basePoint = new double[objectArray.length][];
            for(int i = 0; i < objectArray.length; i++){
                basePoint[i] = (double[])objectArray[i];
            }
        }
        mSensorManager.registerListener(this, mSensor_Accelerometer, SensorManager.SENSOR_DELAY_UI);
        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //temp
            total++;
            if (!active && (abs(event.values[0] - currentArray[0]) > callThreshold || abs(event.values[1] - currentArray[1]) > callThreshold ||
                    abs(event.values[2] - currentArray[2]) > callThreshold)){
                active = true;
                Log.i("LOG", "샘플링 횟수 : " + total + ", 호출 횟수 : " + ++count);
            }

            if(i == 50) {
                Log.i("LOG","Array is Full.");
                i = 0;

                //knn 호출
                if(knn.run(array)){
                    Toast.makeText(getApplicationContext(), "#### 감지됨####", Toast.LENGTH_SHORT).show();
                    Log.i("LOG", "#### success ####");
                }
                else {
                    Log.i("LOG", "#### fail ####");
                }
                active = false;
                Log.i("LOG", "#### active false ####");
            }
            else if(active){
                array[i] = Math.abs(event.values[0] - currentArray[0]);
                array[i + 50] = Math.abs(event.values[1] - currentArray[1]);
                array[i + 100] = Math.abs(event.values[2] - currentArray[2]);
                i++;
            }
            //마지막 생성되는 배열 브로트캐스트 해줘야됨

//
//            if(z < 90){
//                res[x] = event.values[0];
//                res[y] = event.values[1];
//                res[z] = event.values[2];
//
//                Log.i("service", res[x] + ", " + res[y] + ", " + res[z]);
//                x += 3; y += 3; z += 3;
//            }
//            else{
//                x = 0; y = 1; z = 2;
//                Intent myFilteredResponse = new Intent("com.kwon.sensor.step");
//                myFilteredResponse.putExtra("serviceData", res);
//                sendBroadcast(myFilteredResponse);
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
