package com.kwon.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        OnChartValueSelectedListener{
    static double INF = Double.MAX_VALUE;   //최대값
    private SensorManager mSensorManager = null;

    private SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;

    private double currentArray[] = new double[]{INF, INF, INF};
    private double basePoint[][] = new double[45][150];
    private int trueIndex = 0, falseIndex = 0;

    Intent intent;
    BroadcastReceiver receiver;

    Button bt_start, bt_end, current, basepoint, basepoint_false;
    TextView txt, trueText, falseText;

    private LineChart mChart;

    double[] res = new double[90];
    int x = 0, y = 1, z = 2;
    boolean fin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        intent = new Intent(MainActivity.this, MyService.class);
        receiver = new MyMainLocalReceiver();

        bt_start = (Button)findViewById(R.id.bt_start);
        bt_end = (Button)findViewById(R.id.bt_end);
        current = (Button)findViewById(R.id.current);
        basepoint = (Button)findViewById(R.id.basePoint);
        basepoint_false = (Button)findViewById(R.id.basePoint_false);

        txt = (TextView)findViewById(R.id.txt);
        trueText = (TextView)findViewById(R.id.trueText);
        falseText = (TextView)findViewById(R.id.falseText);

        bt_start.setOnClickListener(this);
        bt_end.setOnClickListener(this);
        current.setOnClickListener(this);
        basepoint.setOnClickListener(this);
        basepoint_false.setOnClickListener(this);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(true);

        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(false);
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        mChart.setData(data);

//        // get the legend (only possible after setting data)
//        Legend l = mChart.getLegend();
//
//        // modify the legend ...
//        l.setForm(Legend.LegendForm.LINE);
//        l.setTextColor(Color.WHITE);
//
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setAxisMinimum(-10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setLabelCount(10);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    //------------------설치된 장소의 평균값 저장
    private class CurrentAccelometerListener implements SensorEventListener {

        int i = 0;
        private double x = 0.0, y = 0.0, z= 0.0;

        //temp
        double max1 = -987654.0, min1 = Double.MAX_VALUE;
        double max2 = -987654.0, min2 = Double.MAX_VALUE;
        double max3 = -987654.0, min3 = Double.MAX_VALUE;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(i == 250){
                currentArray[0] = x / 250.0;
                currentArray[1] = y / 250.0;
                currentArray[2] = z / 250.0;
                Log.i("LOG","average accelometer : " + currentArray[0] + ", " + currentArray[1] + ", " + currentArray[2]);
                mSensorManager.unregisterListener(mAccLis);

                // temp
                Log.i("LOG", "max1 : " + String.format("%.4f", max1 - currentArray[0]) + ", min1 : " + String.format("%.4f", currentArray[0] - min1));
                Log.i("LOG", "max2 : " + String.format("%.4f", max2 - currentArray[1]) + ", min2 : " + String.format("%.4f", currentArray[1] - min2));
                Log.i("LOG", "max3 : " + String.format("%.4f", max3 - currentArray[2]) + ", min3 : " + String.format("%.4f", currentArray[2] - min3));
            } else {
                //temp
                double temx = event.values[0]; double temy = event.values[1]; double temz = event.values[2];
                max1 = max1 > temx ? max1 : temx; min1 = min1 < temx ? min1 : temx;
                max2 = max2 > temy ? max2 : temy; min2 = min2 < temy ? min2 : temy;
                max3 = max3 > temz ? max3 : temz; min3 = min3 < temz ? min3 : temz;

                x += event.values[0];
                y += event.values[1];
                z += event.values[2];
                Log.e("LOG", "current" + i + "번째           [X]:" + String.format("%.4f", event.values[0])
                        + "           [Y]:" + String.format("%.4f", event.values[1])
                        + "           [Z]:" + String.format("%.4f", event.values[2])
                );
                i++;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    //------------------True값 저장
    private class AccelometerListener implements SensorEventListener {

        double[] array = new double[150];
        int i = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {

            if(i == 50) {   //array가 꽉 차면 저장
                Log.i("LOG","Array is Full.");
                mSensorManager.unregisterListener(mAccLis);
                i = 0;
                findViewById(R.id.basePoint).setEnabled(true);
                findViewById(R.id.basePoint_false).setEnabled(true);
                System.arraycopy(array, 0, basePoint[trueIndex++], 0, array.length);
                trueText.setText("true 저장개수" + trueIndex);
            }
            else{   //basePoint를 array배열에 저장. basePoint = 절대값(측정되는 값 - 현 위치 current값)
                array[i] = abs(event.values[0] - currentArray[0]);
                array[i + 50] = abs(event.values[1] - currentArray[1]);
                array[i + 100] = abs(event.values[2] - currentArray[2]);

                Log.e("LOG", "ACCELOMETER true           [X]:" + String.format("%.4f", array[i])
                        + "           [Y]:" + String.format("%.4f", array[i + 50])
                        + "           [Z]:" + String.format("%.4f", array[i + 100])
                );

                i++;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    //------------------False값 저장
    private class AccelometerListener_False implements SensorEventListener {

        double[] array = new double[150];
        int i = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {

            if(i == 50) {
                mSensorManager.unregisterListener(mAccLis);
                i = 0;
                findViewById(R.id.basePoint).setEnabled(true);
                findViewById(R.id.basePoint_false).setEnabled(true);
                System.arraycopy(array, 0, basePoint[falseIndex++ + 30], 0, array.length);
                falseText.setText("false 저장개수" + falseIndex);
                Log.i("LOG","Array is Full.");
            }
            else{
                array[i] = abs(event.values[0] - currentArray[0]);
                array[i + 50] = abs(event.values[1] - currentArray[1]);
                array[i + 100] = abs(event.values[2] - currentArray[2]);

                Log.e("LOG", "ACCELOMETER false           [X]:" + String.format("%.4f", array[i])
                        + "           [Y]:" + String.format("%.4f", array[i + 50])
                        + "           [Z]:" + String.format("%.4f", array[i + 100])
                );

                i++;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private Thread thread;

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (fin != false) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.bt_start:
                IntentFilter mainFilter = new IntentFilter("com.kwon.sensor.step");
                registerReceiver(receiver, mainFilter);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("basePoint", basePoint);
                intent.putExtras(mBundle);
                intent.putExtra("currentArray",currentArray);
                startService(intent);
                fin = true;
                feedMultiple();
                break;

            case R.id.bt_end:
                if(intent != null){
                    unregisterReceiver(receiver);
                    stopService(intent);
                }
                fin = false;
                thread.interrupt();
                break;

            case R.id.current:
                current.setEnabled(false);
                mAccLis = new CurrentAccelometerListener();
                mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_GAME);
                break;

            case R.id.basePoint:
                if((currentArray[0] == INF) || (trueIndex >= 30))
                    Toast.makeText(getApplicationContext(), "Current 값이 없거나 저장공간을 모두 할당했습니다.", Toast.LENGTH_SHORT).show();
                else {
                    findViewById(R.id.basePoint).setEnabled(false);
                    findViewById(R.id.basePoint_false).setEnabled(false);

                    mAccLis = new AccelometerListener();
                    mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_GAME);
                }
                break;

            case R.id.basePoint_false:
                if((currentArray[0] == INF) || (falseIndex >= 15))
                    Toast.makeText(getApplicationContext(), "Current 값이 없거나 저장공간을 모두 할당했습니다.", Toast.LENGTH_SHORT).show();
                else{
                    findViewById(R.id.basePoint).setEnabled(false);
                    findViewById(R.id.basePoint_false).setEnabled(false);

                    mAccLis = new AccelometerListener_False();
                    mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_GAME);
                }
                break;
        }
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);
            ILineDataSet set2 = data.getDataSetByIndex(2);

            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            if (set1 == null) {
                set1 = createSet1();
                data.addDataSet(set1);
            }
            if (set2 == null) {
                set2 = createSet2();
                data.addDataSet(set2);
            }

            set.addEntry(new Entry(set.getEntryCount(), (float) res[x]));
            set1.addEntry(new Entry(set.getEntryCount(), (float) res[y]));
            set2.addEntry(new Entry(set.getEntryCount(), (float) res[z]));

//            data.addEntry(new Entry(set.getEntryCount(), (float) (res[i])), 0);
//            data.addEntry(new Entry(set.getEntryCount(), (float) (res[i+1])), 1);
//            data.addEntry(new Entry(set.getEntryCount(), (float) (res[i+2])), 2);
            Log.i("line", res[x] + ", " + res[y] + ", " + res[z]);
            String str = String.format("x: %.2f, y: %.2f, z: %.2f",res[x],res[y],res[z]);
            txt.setText(str);
            x += 3; y += 3; z += 3;
            if(z >= 90){
                x = 0; y = 1; z = 2;
            }
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            mChart.setVisibleYRangeMaximum(20f, YAxis.AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "X");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.MAGENTA);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        //set.setCircleColor(Color.MAGENTA);
        set.setLineWidth(2f);
        //set.setCircleRadius(4f);
        //set.setFillAlpha(65);
        //set.setFillColor(ColorTemplate.getHoloBlue());
        //set.setHighLightColor(Color.rgb(244, 117, 117));
        //set.setValueTextColor(Color.BLUE);
       //set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
    private LineDataSet createSet1() {

        LineDataSet set = new LineDataSet(null, "Y");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLUE);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        //set.setCircleColor(Color.MAGENTA);
        set.setLineWidth(2f);
       // set.setCircleRadius(4f);
        //set.setFillAlpha(65);
        //set.setFillColor(ColorTemplate.getHoloBlue());
        //set.setHighLightColor(Color.rgb(244, 117, 117));
        //set.setValueTextColor(Color.BLUE);
        //set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
    private LineDataSet createSet2() {

        LineDataSet set = new LineDataSet(null, "Z");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.GREEN);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        //set.setCircleColor(Color.MAGENTA);
        set.setLineWidth(2f);
        //set.setCircleRadius(4f);
        //set.setFillAlpha(65);
        //set.setFillColor(ColorTemplate.getHoloBlue());
        //set.setHighLightColor(Color.rgb(244, 117, 117));
        //set.setValueTextColor(Color.BLUE);
        //set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    class MyMainLocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            res = intent.getDoubleArrayExtra("serviceData");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        unregisterReceiver(receiver);
        stopService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }
}
