package com.api.download.manage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

/**
 * 每片的 信息
 */
public class SliceInfo {
    private  long st;
    private  long ed;
    private  long page ;
}
