package com.hashcode.models;

import java.util.HashSet;
import java.util.Set;

public class Photo {

    public int id;
    public Integer tagCount;
    public String position;
    public Set<String> tags;

    public Photo(int id, int tagCount, String position) {
        this.id = id;
        this.tagCount = tagCount;
        this.position = position;
        this.tags = new HashSet<>(tagCount);
    }
}
