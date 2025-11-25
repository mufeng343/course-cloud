package com.zjsu.gjh.catalog.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class ScheduleSlot {
    private String dayofweek;
    private String startTime;
    private String endTime;
    private int expectedAttendance;

    public ScheduleSlot(String dayofweek,String startTime,String endTime,int expectedAttendance){
        this.dayofweek=dayofweek;
        this.startTime=startTime;
        this.endTime=endTime;
        this.expectedAttendance=expectedAttendance;
    }

    public ScheduleSlot() {}

    public String getDayofweek() {
        return dayofweek;
    }

    public void setDayofweek(String dayofweek) {
        this.dayofweek = dayofweek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getExpectedAttendance() {
        return expectedAttendance;
    }

    public void setExpectedAttendance(int expectedAttendance) {
        this.expectedAttendance = expectedAttendance;
    }
}
