package com.kwon.sensor;

import android.util.Log;
import java.util.Arrays;

public abstract class KNearestNeighbors{
    protected final double[][] basePoint;		//기존point.
    final int NUMBER_OF_POINT;		//basePoint개수
    final int NUMBER_OF_DATA;		//basePoint를 구성하는 데이터 개수
    final boolean[] open;			//basePoint의 open 여부. 다수결 시행시 이용

    protected double[] maxArray;		//최대값 구할시 사용되는 배열
    protected double maxValue;			//정규화하기 전 최대값
    protected int K;					//KNN의 K
    protected boolean result;			//최종결과

    protected WarpingDistance[] wd;		//유사도와 true/false, seq를 가진 객체 배열

    public KNearestNeighbors(double[][] basePoint) {
        Log.i("LOG", "KNN start");
        this.basePoint = basePoint;
        this.NUMBER_OF_POINT = 45;
        this.NUMBER_OF_DATA = 150;
        this.open = new boolean[]{true, true, true, true, true, true, true, true, true, true,
                true, true, true, true, true, true, true, true, true, true,
                true, true, true, true, true, true, true, true, true, true,
                false, false, false, false, false, false, false, false, false, false,
                false, false, false, false, false};

        this.maxArray = new double[NUMBER_OF_POINT + 1];

        this.K = (int)Math.sqrt(NUMBER_OF_POINT);
        if(K % 2 == 0) K -= 1;

        this.wd = new WarpingDistance[NUMBER_OF_POINT];
    }

    public boolean run(double[] sensedPoint) {
        this.result = false;

        //basePoint 깊은복사
        double[][] tempBasePoint = new double[NUMBER_OF_POINT][NUMBER_OF_DATA];
        for(int i = 0; i < NUMBER_OF_POINT; i++)
            System.arraycopy(basePoint[i], 0, tempBasePoint[i], 0, NUMBER_OF_DATA);

        //sensedPoint와 전체 basePoint들의 데이터들중 최대값 선별
        for(int i = 0; i < NUMBER_OF_POINT; i++)
            maxArray[i] = max(tempBasePoint[i]);
        maxArray[NUMBER_OF_POINT] = max(sensedPoint);

        maxValue = max(maxArray);
        Log.i("LOG", "최대값은 "+ maxValue);

        //sensedPoint와 BasePoint 정규화 진행. (최대 기록 값으로 값을 나누고 2를 곱한 다음 1을 뺀다.)
        for(int i = 0; i < NUMBER_OF_POINT; i++)
            nomalize(tempBasePoint[i]);
        nomalize(sensedPoint);

        //DTW 실행
        for(int i = 0; i < NUMBER_OF_POINT; i++) {
            DynamicTimeWrapping dtw = new DynamicTimeWrapping(tempBasePoint[i], sensedPoint);
            wd[i] = new WarpingDistance(dtw.getDistance(), open[i], i);
        }

        Arrays.sort(wd);

        //------------- 다수결 시행 -------------------//
        int wdCount = 0;
        for(int i = 0; i < K; i++) {
            Log.i("LOG", "" + wd[i].toString());
            if(wd[i].getOpen()) wdCount++;
        }
        if(wdCount > K/2)
            result = true;
        Log.i("LOG", "최종 결과" + result);
        return result;
    }

    //최대값 반환
    public double max(double[] arr){
        double max = arr[0];
        for(int i = 1; i < arr.length; i++)
            if(max < arr[i])
                max = arr[i];

        return max;
    }

    //정규화
    public void nomalize(double[] arr) {
        for(int i = 0; i < NUMBER_OF_DATA; i++)
            arr[i] = (arr[i] / maxValue) * 2 - 1;
    }
}