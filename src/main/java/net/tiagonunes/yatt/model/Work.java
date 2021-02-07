package net.tiagonunes.yatt.model;

import com.j256.ormlite.field.DatabaseField;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public abstract class Work {
    @Id
    @GeneratedValue
    private long id;

    @Column
    private LocalDate date;

    @Column
    private LocalTime startTime;

    @Column
    private int duration; // in minutes

    @Column
    private String name;

    //@Column @ManyToOne(optional = false) @JoinColumn(name = "category_id", nullable = false, updatable = false)
    // using ORMLite's annotation here for ease of use of foreignAutoRefresh
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Category category;

    @Column
    private String tags;

    @Column
    private Date inserted = Date.from(Instant.now());


    public Work() {}


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime start) {
        this.startTime = start;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getInserted() {
        return inserted;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
    }
}
