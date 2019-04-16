package com.cfs.sqlkv.util;


import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;


public class CurrentDatetime {


    private java.util.Date currentDatetime;

    private Date currentDate;

    private Time currentTime;

    private Timestamp currentTimestamp;


    public CurrentDatetime() {
    }

    final private void setCurrentDatetime() {
        if (currentDatetime == null)
            currentDatetime = new java.util.Date();
    }

    // class interface

    public Date getCurrentDate() {
        if (currentDate == null) {
            setCurrentDatetime();
            currentDate = new Date(currentDatetime.getTime());
        }
        return currentDate;
    }

    public Time getCurrentTime() {
        if (currentTime == null) {
            setCurrentDatetime();
            currentTime = new Time(currentDatetime.getTime());
        }
        return currentTime;
    }

    public Timestamp getCurrentTimestamp() {
        if (currentTimestamp == null) {
            setCurrentDatetime();
            currentTimestamp = new Timestamp(currentDatetime.getTime());
        }
        return currentTimestamp;
    }


    public void forget() {
        currentDatetime = null;
        currentDate = null;
        currentTime = null;
        currentTimestamp = null;
    }

}
