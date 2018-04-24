package com.kwon.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        OnChartValueSelectedListener{
    Intent intent;
    BroadcastReceiver receiver;

    Button bt_start, bt_end;
    TextView txt;

    private LineChart mChart;

    double[] res = new double[90];
    int x = 0, y = 1, z = 2;
    boolean fin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(MainActivity.this, MyService.class);
        receiver = new MyMainLocalReceiver();

        bt_start = (Button)findViewById(R.id.bt_start);
        bt_end = (Button)findViewById(R.id.bt_end);
        txt = (TextView)findViewById(R.id.txt);

        bt_start.setOnClickListener(this);
        bt_end.setOnClickListener(this);

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
