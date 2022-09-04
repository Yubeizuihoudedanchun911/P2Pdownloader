package com.entity;

import com.common.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class GroupEntry {
    Set<Node> nodes;
    String groupID;
}
