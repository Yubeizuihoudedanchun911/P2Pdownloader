# P2Pdownloader 

## 概述

------

通过**Peer to Peer** 的方式进行用户加速，采用**Raft** 协议在一个用户group中选举下载服务的管理者,在第三方服务器Tracker的组分配中、以及Leader 的任务分配中进行负载均衡。

## Working in progress

1.下载可靠性2.服务器下载负担3.下载速度带宽限制4.下载完整性检验5.P2P网络

#### Download Process

------

用户上线时告知第三方服务器，第三方服务器通过组分配算法选取特定组将组内的Node List 发送给用户，用户broadCast消息给List 中的用户 告知 新用户加入组。

**Node A**在确认加入用户组并且在Leader 确认之后，可以进行发送**单个或者多个DownLoad Requst 进行多任务下载。**Node A 会对每个任务进行 心跳计时，当超过时间没有收到SlicePage时会 **本地计算缺失片段，并且发送下载请求**。也就是说 在任务下载途中组内某个Node失联时，不会影响任务的完整下载。





![image-20220913152223679](https://user-images.githubusercontent.com/83199295/230703713-f39f96a4-fdb9-4a83-8d07-8253edd4fab3.png)

------

#### Tech Details

- taskArrange (SlicePageInfo 总片数) 通过记录的各节点任务数的Map 创建 小根堆，逐个将每片分配各个节点返回taskArrangement
- dealDownLoad(TaskArrangement)通过taskArrangeMent找到自己节点需要的下载任务并且获取到请求下载节点地址，进行http分片下载。下载完毕后直接传给请求下载节点，并且将完成下载片数作为返回值回传给leader更新记录任务数的Map
- fullDownLoad(URL) 完整下载url中的资源，进行taskArrange  派发给各个Node
- partDownLoad(List,URL)根据List中记录的缺失片段 进行比对重新进行taskArrange  派发给各个Node
- Class TaskListener监听下载任务是否完成，并且同步计时是否下载超时。下载超时通过checkSlice 进行检查缺少的片数返回List(缺失Slice的index），RPC调用partDownLoad
- ![image-20221012200434387](https://user-images.githubusercontent.com/83199295/230703752-846a7792-098f-4dae-8d94-bacfd2721a0d.png)

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

- 数据持久化：对下载任务的持久化，下载片的暂时保存
- 在组分配中以及分配算法中：加入Hash比对，询问节点是否已经保存过部分片段
- 通过本地状态机的查询找到缓存分片
- 可靠UDP传输

https://github.com/Yubeizuihoudedanchun911/P2Pdownloader
