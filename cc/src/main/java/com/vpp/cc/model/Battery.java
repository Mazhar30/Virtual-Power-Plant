package com.vpp.cc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Objects;

@Document(collection = "battery")
public class Battery implements Serializable {

    @Id
    private String id;
    private String name;
    private String postcode;
    private int capacity;

    public Battery(String id, String name, String postcode, int capacity) {
        this.id = id;
        this.name = name;
        this.postcode = postcode;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Battery battery = (Battery) o;
        return capacity == battery.capacity &&
                Objects.equals(id, battery.id) &&
                Objects.equals(name, battery.name) &&
                Objects.equals(postcode, battery.postcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, postcode, capacity);
    }
}
