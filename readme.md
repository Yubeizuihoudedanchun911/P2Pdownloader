# P2Pdownloader 

## 概述

------

通过**Peer to Peer** 的方式进行用户加速，采用**Raft** 协议在一个用户group中选举下载服务的管理者,在第三方服务器Tracker的组分配中、以及Leader 的任务分配中进行负载均衡。

## Working in progress

#### Download Process

------

用户上线时告知第三方服务器，第三方服务器通过组分配算法选取特定组将组内的Node List 发送给用户，用户broadCast消息给List 中的用户 告知 新用户加入组。

**Node A**在确认加入用户组并且在Leader 确认之后，可以进行发送**单个或者多个DownLoad Requst 进行多任务下载。**Node A 会对每个任务进行 心跳计时，当超过时间没有收到SlicePage时会 **本地计算缺失片段，并且发送下载请求**。也就是说 在任务下载途中组内某个Node失联时，不会影响任务的完整下载。

#### Completed



- DowloadCenter :任务下载中心,提供下载服务以及下载分片、下载任务安排服务，以及管理正在进行中的下载任务、分配算法
- TaskListner：管理单个任务并且提供 心跳计时，监听单个下载任务，下载任务缺失片段重新下载服务，SlicePage 合并
- Raft:https://www.cnblogs.com/xybaby/p/10124083.html#_labelTop
  - election: 按照term 、log index 进行比较
  - log replication: 只实现了term 和 index 的同步
  - state Mechine ：记录以及实现log replication 

- DownLoadUtil：任务分片下载
- RPC：远程调用实现
- Requst:CommandType 、 以及Command 、 VoteEntity、LogEntry
- tracker:组分配，分配算法
- HeartBeatTask: 心跳计时

#### Todo

------

- log relication：LogEntry复制给其他节点

- 数据持久化：对下载任务的持久化
- 在组分配中以及分配算法中：加入Hash比对，询问节点是否已经保存过部分片段