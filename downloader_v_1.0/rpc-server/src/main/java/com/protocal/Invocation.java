package com.protocal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Invocation implements Serializable {
    public String className;
    public String method;
    public String[] argType;
    public Object[] args;


}
