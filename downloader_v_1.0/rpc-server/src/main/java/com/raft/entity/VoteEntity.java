package com.raft.entity;

import com.raft.common.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteEntity {

    /** 候选人的最后日志条目的索引值 */
    long lastLogIndex;

    /** 候选人最后日志条目的任期号  */
    long lastLogTerm;

}
