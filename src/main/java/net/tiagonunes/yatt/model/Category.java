package net.tiagonunes.yatt.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "category")
public class Category {
    @Id
    @GeneratedValue
    private long id;

    @Column
    private String name;


    public Category() {}


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
