package com.protoc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
@AllArgsConstructor
public class Invocation implements Serializable {
    public String className;
    public String method;
    public String[] argType;
    public Object[] args;

}
