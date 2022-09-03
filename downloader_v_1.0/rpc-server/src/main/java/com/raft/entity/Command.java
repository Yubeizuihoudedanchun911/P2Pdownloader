package com.raft.entity;

import com.api.download.manage.SliceInfo;
import com.raft.common.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Command<T> {
    private  String targetUri;
    private  T taskArrangement ;
    private  Node targetNode ;

}
