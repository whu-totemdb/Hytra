package edu.whu.hyk.model;

public class Point {
    int pid;
    double lat;
    double lon;
    String datetime;
    int tid;

    public Point (double lat, double lon){}

    public Point(int pid, double lat, double lon, String datetime, int tid) {
        this.pid = pid;
        this.lat = lat;
        this.lon = lon;
        this.datetime = datetime;
        this.tid = tid;
    }

    public int getPid() {
        return pid;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getDatetime() {
        return datetime;
    }

    public int getTid() {
        return tid;
    }
}
