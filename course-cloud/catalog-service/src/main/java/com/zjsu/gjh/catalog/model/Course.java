package com.zjsu.gjh.catalog.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    private String code;

    @Column(nullable = false)
    private String title;

    @Embedded
    private Instructor instructor;

    @Embedded
    private ScheduleSlot schedule;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int enrolled = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Course() {}

    public Course(String code,String title,Instructor instructor,ScheduleSlot schedule,int capacity,int enrolled){
        this.code=code;
        this.title=title;
        this.instructor=instructor;
        this.schedule=schedule;
        this.capacity=capacity;
        this.enrolled=enrolled;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public ScheduleSlot getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleSlot schedule) {
        this.schedule = schedule;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public int getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(int enrolled) {
        this.enrolled = enrolled;
    }
}
