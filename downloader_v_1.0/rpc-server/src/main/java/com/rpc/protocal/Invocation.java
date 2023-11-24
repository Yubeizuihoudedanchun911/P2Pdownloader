package com.rpc.protocal;

import java.io.Serializable;

import lombok.Data;


public class Invocation implements Serializable {
    public String className;
    public String method;
    public String[] argType;
    public Object[] args;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String[] getArgType() {
        return argType;
    }

    public void setArgType(String[] argType) {
        this.argType = argType;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Invocation(String className, String method, String[] argType, Object[] args) {
        this.className = className;
        this.method = method;
        this.argType = argType;
        this.args = args;
    }
}
