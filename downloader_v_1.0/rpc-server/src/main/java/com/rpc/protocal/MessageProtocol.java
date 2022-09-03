package com.rpc.protocal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//协议包
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageProtocol {
    private int len; //关键
    private byte[] content;
    private long slice_idx;

}
