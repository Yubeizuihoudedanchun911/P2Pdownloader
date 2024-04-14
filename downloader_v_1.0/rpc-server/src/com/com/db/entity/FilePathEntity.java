package com.com.db.entity;

/**
 * @author jijunling <jijunling@kuaishou.com>
 * Created on 2023-11-24
 */
public class FilePathEntity {
    private int id;
    private String path;
    private String url;

    public FilePathEntity(String path, String url) {
        this.path = path;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
