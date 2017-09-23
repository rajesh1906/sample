package com.realm.snakegame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Rajesh Kumar on 18-09-2017.
 */

public class SamplePojo {


    String name;
    String address;
    int salary;
    boolean good_one;

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    HashMap<String,Object> values = new HashMap<>();

    public int getKey() {
        return key;
    }

    public void setKey(int value) {
        this.key = value;
    }

    int key;

    public SamplePojo(int key,String name, String address, int salary, boolean good_one) {
        this.name = name;
        this.address = address;
        this.salary=salary;
        this.good_one=good_one;
        this.key=key;
        AddValues();

    }

    public void AddValues(){
        values.put("name",getName());
        values.put("address",getAddress());
        values.put("salary",getSalary());
        values.put("good_one",isGood_one());
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        salary = salary;
    }

    public boolean isGood_one() {
        return good_one;
    }

    public void setGood_one(boolean good_one) {
        this.good_one = good_one;
    }


}
