package com.kwon.sensor;

public class DynamicTimeWrapping {
    protected double[] point1;		// point 1	비교할 두 점(각 데이터 90개)
    protected double[] point2;		// point 2
    protected int[][] warpingPath;

    protected int n;
    protected int m;
    protected int k;

    protected double warpingDistance;

    protected double temp_accumulatedDistance;//--//

    public DynamicTimeWrapping(double[] val1, double[] val2) {
        // TODO Auto-generated constructor stub
        this.point1 = val1;
        this.point2 = val2;

        n = point1.length;
        m = point2.length;
        k = 1;					//warping 이동거리. max(n, m) <= k < n + m.

        warpingPath = new int[n + m][2];        // warping 이전경로 저장.
        warpingDistance = 0.0;					// 두 점 유사도

        this.compute();
    }

    public void compute() {
        double accumulatedDistance = 0.0;

        double[][] d = new double[n][m];        // local distances.
        double[][] D = new double[n][m];        // global distances. 배열 d를 이용, dtw 도출하기 위한 배열

        for (int i = 0; i < n; i++) {			//point1과 point2의 각 데이터 차 저장
            for (int j = 0; j < m; j++) {
                d[i][j] = distanceBetween(point1[i], point2[j]);
            }
        }

        D[0][0] = d[0][0];

        for (int i = 1; i < n; i++) {			//row 맨 왼쪽
            D[i][0] = d[i][0] + D[i - 1][0];
        }

        for (int j = 1; j < m; j++) {			//column 맨 위
            D[0][j] = d[0][j] + D[0][j - 1];
        }

        for (int i = 1; i < n; i++) {			//행렬의 나머지 부분 채우기
            for (int j = 1; j < m; j++) {
                //자신을 둘러싼 3방향요소 중 최소값 선별하고 => d[i][j]와 더한 후 => D[i][j]에 저장
                accumulatedDistance = Math.min(Math.min(D[i-1][j], D[i-1][j-1]), D[i][j-1]);
                accumulatedDistance += d[i][j];
                D[i][j] = accumulatedDistance;
            }
        }

        accumulatedDistance = D[n - 1][m - 1];

        //---유사도 측정 거리 k 구하기---
        int i = n - 1;				// 시작 위치 i,j = 맨 오른쪽 맨 아래
        int j = m - 1;
        int minIndex = 1;

        warpingPath[k - 1][0] = i;
        warpingPath[k - 1][1] = j;

        while ((i + j) != 0) {
            if (i == 0) j -= 1;	//맨 위쪽 도달
            else if (j == 0) i -= 1;	//맨 왼쪽 도달
            else {        // i != 0 && j != 0
                double[] array = { D[i - 1][j], D[i][j - 1], D[i - 1][j - 1] };
                minIndex = this.getIndexOfMinimum(array);		// 주변을 둘러싼 3요소중 최소값 구하기
                if (minIndex == 0) i -= 1;						// 왼쪽으로 이동
                else if (minIndex == 1) j -= 1;				// 위로 이동
                else if (minIndex == 2) {						// 11시 방향으로 이동
                    i -= 1;
                    j -= 1;
                }
            }

            k++;		// 이동거리 증가. k는 데이터 개수(90)에 근접할수록 두 포인트가 유사
            warpingPath[k - 1][0] = i;		//이전 경로(i, j) 저장
            warpingPath[k - 1][1] = j;
        }

        warpingDistance = accumulatedDistance / k;
        temp_accumulatedDistance = accumulatedDistance;//-test-//
    }

    //-test-//
    public double getAccumulatedDistance() {
        return temp_accumulatedDistance;
    }

    //-test-//
    public double getK() {
        return k;
    }

    public double getDistance() {		//	return warpingDistance
        return warpingDistance;
    }

    protected double distanceBetween(double p1, double p2) {	// 두 데이터간의 차
        return (p1 - p2) * (p1 - p2);
    }

    protected int getIndexOfMinimum(double[] array) {	//파라미터로 전달된 값들중 최소값의 인덱스 반환, 순차탐색
        int index = 0;
        double val = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < val) {
                val = array[i];
                index = i;
            }
        }
        return index;
    }
}

