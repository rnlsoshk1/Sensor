package com.kwon.sensor;

public class WarpingDistance implements Comparable<WarpingDistance> {

    private double value;	//데이터
    private boolean open;	//open여부
    private int seq;		//몇 번째 값인지 확인하기 위해

    //초기화
    public WarpingDistance(double value, boolean open, int seq){
        this.value = value;
        this.open = open;
        this.seq = seq;
    }

    public boolean getOpen() {
        return open;
    }

    public String toString() {
        return "warpingdistance: " + value + "   /    open: " + open + "   /    seq: " + seq;
    }

    public int compareTo(WarpingDistance obj) {

        if (this.value < obj.value) {
            return -1;
        } else if (this.value == obj.value) {
            return 0;
        } else {
            return 1;
        }
    }
}