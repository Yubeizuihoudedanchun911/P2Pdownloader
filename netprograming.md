# 网络编程

> 并发编程的最终目的：
>
> 充分利用多核cpu，并发
>
> 互联网时代、物联网、人工智能时代，客户单发起的并发量，更大更高
>
> 一台服务器搞不定了，出来集群，硬件资源的扩展，
>
> 数据库、Tomcat、Redis进行资源的升级拆分
>
> 分布式服务、微服务、容器化技术、云计算

### 计算机网络：

#### 数据发送接收过程：

![image-20220804150630054](D:/markdown_pics/image-20220804150630054.png)



********重要：**

<img src="D:/markdown_pics/image-20220804151300742.png" alt="image-20220804151300742" style="zoom:150%;" />

<img src="D:/markdown_pics/image-20220804151446700.png" alt="image-20220804151446700" style="zoom:200%;" />

![image-20220804152129949](D:/markdown_pics/image-20220804152129949.png)

**主要注意传输层、应用层、表示层、会话层**

#### IP协议及报文结构：

![image-20220804152927229](D:/markdown_pics/image-20220804152927229.png)

> 报头校验和：检查完整性

#### **TCP协议及报文结构：**

![image-20220804153726904](D:/markdown_pics/image-20220804153726904.png)

> 接受窗口：实现流量窗口的控制。
>
> 校验和：通过hash值验证数据是否发生变化

##### **建立连接-TCP三次握手**

**目的：建立可靠的通信信道**

![image-20220804154318780](D:/markdown_pics/image-20220804154318780.png)

##### 数据传输：

![image-20220804155115758](D:/markdown_pics/image-20220804155115758.png)

> ack进行确认协议。

##### TCP四次挥手：

![image-20220804155445463](D:/markdown_pics/image-20220804155445463.png)

> q：为什么会在数据发完之后 再进行第三次挥手？
>
> a：TCP面向连接，需要确认关闭连接。



#### UDP协议及报文结构：

![image-20220804163550865](D:/markdown_pics/image-20220804163550865.png)



#### UDP与TCP比较：

![image-20220804163917775](D:/markdown_pics/image-20220804163917775.png)

**UDP：类似于异步请求**

**TCP：类似于同步请求**

> 什么情况下用到UDP：直播、日志上报、聊天



------



### Java 网络编程：

![image-20220804164355100](D:/markdown_pics/image-20220804164355100.png)

> 一般来说每个应用程序会绑定一个端口，创捷Socket进行监听端口

Demo01：



**server：**

```java
package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SoketServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try{
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        ));
        System.out.println("server start...");
        while(true){
            Socket req = serverSocket.accept();
            System.out.println("new connetion " + req.toString());
            try{
                InputStream inputStream = req.getInputStream();
                BufferedReader br  = new BufferedReader(new InputStreamReader(inputStream));
                String msg ,resps="";
                while((msg = br.readLine()) != null){
                    if(msg.length()==0){
                        break;
                    }
                    resps = msg;
                    System.out.println(msg);
                }
                System.out.println("receive data from " + req.toString());
//
                req.getOutputStream().write((resps+"\n").getBytes("utf-8"));
                req.getOutputStream().flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}

```

**client：**

```java
package network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;

public class SoketClient {
    private  static Charset charset = Charset.forName("UTF-8");
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",8080);
        OutputStream outputStream = socket.getOutputStream();
        Scanner scanner = new Scanner(System.in);
        System.out.println("input...");
        String msg = scanner.nextLine()+"\n"+"\n";
        outputStream.write(msg.getBytes(charset));
        scanner.close();

        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        int len = 0;
        while((len= inputStream.read(bytes))>0){
            System.out.println(new String(bytes,"utf-8"));
        }

        socket.close();

    }
}

```

#### Http协议： 响应包解析

![image-20220804175749818](D:/markdown_pics/image-20220804175749818.png)

#### 并发量增大-服务升级：

**伪异步：线程实现**

![image-20220804182057830](D:/markdown_pics/image-20220804182057830.png)

**阻塞非阻塞/同步异步**

 ![image-20220804183735540](D:/markdown_pics/image-20220804183735540.png)

![image-20220804184207492](D:/markdown_pics/image-20220804184207492.png)

> 同步阻塞：Socket编程
>
> 同步非阻塞：
>
> 异步阻塞：
>
> 异步非阻塞：





#### 五种IO模型：

##### 1)  BIO           阻塞IO模型：Sokect Read 面向字节流 InputStream 、 OutPutStream

![image-20220804184625514](D:/markdown_pics/image-20220804184625514.png)

在Demo01里 server端加入 多线程 ， 但会有阻塞的情况（一直等待字节流的输入）



##### 2)  NIO   非阻塞IO模型：不在面向字节流、面向缓冲API,Java NIO 面向缓冲实现

![](D:/markdown_pics/image-20220804200546843.png)

###### Buffer 缓冲区：

![image-20220804200944766](D:/markdown_pics/image-20220804200944766.png)

![image-20220804201335846](D:/markdown_pics/image-20220804201335846.png)

> byteBuffer.mark(): 标记当前postion的位置
>
> byteBuffer.reset(): 重置position位置为上次mark()标记的位置 
>
> byteBuffer.clear(): 清除整个缓冲区
>
> byteBuffer.compact(): 清除已阅读的数据 ，转为写入模式
>
> byteBuffer.flip(): postion置为0，从0开始读取。 limit = position，position = 0
>
> byteBuffer.allocateDirect(size): 分配动态内存        ===> unsafe.allocateMermory()

![image-20220804202823692](D:/markdown_pics/image-20220804202823692.png) 

![image-20220805134552914](D:/markdown_pics/image-20220805134552914.png)

**allocateDirect 直接在JVM堆外申请内存**



###### Channel 通道：

![image-20220804214245477](D:/markdown_pics/image-20220804214245477.png)

**Demo: **

**Client :**

```java
package network.NIOchannel;

import network.socket.SoketClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1",8080));
        while(!socketChannel.finishConnect()){
            //  没连接上一直等待
            Thread.yield();
        }
        Scanner in = new Scanner(System.in);
        System.out.println("input");
        //  发送内容
        String msg = in.nextLine();
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()){
            socketChannel.write(buffer);
        }
        System.out.println("receive msg");
        ByteBuffer reqBuffer = ByteBuffer.allocateDirect(1024);
        while(socketChannel.isOpen() && socketChannel.read(reqBuffer)!=-1){
            if(reqBuffer.position() > 0 ) break;
        }
        reqBuffer.flip();
        byte[] content = new byte[reqBuffer.limit()];
        reqBuffer.get(content);
        System.out.println(new String(content));

        in.close();
        socketChannel.close();
    }
}

```

**Server:**

```java
package network.NIOchannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {

    private static List<SocketChannel> channels = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        ServerSocketChannel socketChannel = ServerSocketChannel.open(); //  可以监听新进来的TCP连接的通道, 就像标准IO中的ServerSocket一样
        socketChannel.configureBlocking(false);  // 默认阻塞 , 配置为false表示不阻塞
        socketChannel.bind(new InetSocketAddress(8080));
        while(true){
            SocketChannel socketChann1 = socketChannel.accept();
            if(socketChann1 != null) {
                socketChann1.configureBlocking(false);
                System.out.println("receive new conn " + socketChann1.getRemoteAddress());
                channels.add(socketChann1);
            }else {
                //  当没有新连接的情况下， 就去处理现有连接的数据 处理完就删除掉
                Iterator<SocketChannel> iterator = channels.iterator();
                while (iterator.hasNext()) {
                    SocketChannel socketChann = iterator.next();


                    try {
                        //  超过1024byte，通过报文协议，知道长度
                        ByteBuffer dst = ByteBuffer.allocateDirect(1024);
                        //  read非阻塞
                        while (socketChann.isOpen() && socketChann.read(dst) > -1) {   //  默认写模式 ， 写入buffer
                            //  读到客户端数据 跳出
                            if (dst.position() > 0) {  //  里面有数据
                                break;
                            }
                        }
                        if (dst.position() == 0) continue; //  如果没有数据要处理就 待会来处理
                        dst.flip();  //  读模式

                        byte[] content = new byte[dst.limit()];
                        dst.get(content);
                        System.out.println(new String(content));
                        System.out.println("receive data from " + socketChann.getRemoteAddress());

                        String resp = "HTTP/1.1 200 OK\r\n" +
                                "Content-Length: 11\r\n\r\n" +
                                "Hello World";
                        ByteBuffer buffer = ByteBuffer.wrap(resp.getBytes(StandardCharsets.UTF_8));
                        while (buffer.hasRemaining()) {
                            //  非阻塞
                            socketChann.write(buffer);
                        }

                        dst.clear();
                        dst.put("hello world ".getBytes(StandardCharsets.UTF_8));
                        while(dst.hasRemaining()) {
                            socketChann.write(dst);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        iterator.remove();
                    }
                }
            }
        }



    }
}

```

![image-20220804214325088](D:/markdown_pics/image-20220804214325088.png)





##### 3)  IO复用模型：

###### Selector 多路复用：

> 在channel 处理方案中 ， 如果一个时段内请求过多 会导致 没有空闲处理时间，最后请求超时。
>
> Selector ： 可以一次读取多个管道
>
> selectionKey： 可以得到下一个环节做什么 与 Selector 进行绑定，通过Selector 进行管理， 并且通过selectionKey 可以得到selector 、 channel

![image-20220805135031258](D:/markdown_pics/image-20220805135031258.png)

![image-20220805143956142](D:/markdown_pics/image-20220805143956142.png)

```java
Demo03：

package network.NIOchannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class MutipleSever {
    public static void main(String[] args) throws IOException {
        // create Channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        //  create Selector
        Selector selector = Selector.open();

        //  将服务器端的channel 注册到selector  ，注册accept 事件
        SelectionKey selectionKey = serverSocketChannel.register(selector, 0);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);

        //  绑定端口，启动服务
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("server start...");

        while (true) {
            //   启动Selector （管理）
            int nums = selector.select();
            if (nums == 0) continue; //  阻塞 当前没有事件通知

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> itr = selectionKeys.iterator();
            while (itr.hasNext()) {
                SelectionKey key = itr.next();
                itr.remove();
                if(key.isAcceptable()) {
                    SocketChannel socketChann = ((ServerSocketChannel) key.channel()).accept();
                    socketChann.configureBlocking(false);
                    socketChann.register(selector,SelectionKey.OP_READ);
                    System.out.println("receive new connetion " + socketChann);
                }else if(key.isReadable()){
                        try {
                            SocketChannel socketChann = (SocketChannel) key.channel();
                            //  超过1024byte，通过报文协议，知道长度
                            ByteBuffer dst = ByteBuffer.allocateDirect(1024);
                            //  read非阻塞
                            while (socketChann.isOpen() && socketChann.read(dst) > -1) {   //  默认写模式 ， 写入buffer
                                //  读到客户端数据 跳出
                                if (dst.position() > 0) {  //  里面有数据
                                    break;
                                }
                            }
                            if (dst.position() == 0) continue; //  如果没有数据要处理就 待会来处理
                            dst.flip();  //  读模式

                            byte[] content = new byte[dst.limit()];
                            dst.get(content);
                            System.out.println(new String(content));
                            System.out.println("receive data from " + socketChann.getRemoteAddress());

                            String resp = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Length: 11\r\n\r\n" +
                                    "Hello World";
                            ByteBuffer buffer = ByteBuffer.wrap(resp.getBytes(StandardCharsets.UTF_8));
                            while (buffer.hasRemaining()) {
                                //  非阻塞
                                socketChann.write(buffer);
                            }

                            dst.clear();
                            dst.put("hello world ".getBytes(StandardCharsets.UTF_8));
                            while(dst.hasRemaining()) {
                                socketChann.write(dst);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

            }
        }
    }
}
```

###### Reactor设计模式：

![image-20220805151134148](D:/markdown_pics/image-20220805151134148.png)

![image-20220805163354946](D:/markdown_pics/image-20220805163354946.png)



###### **单线程Reactor**

**Demo05：**

```java
package network.NIOchannel.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Handler;

public class SingleThreadSever {

    public static void main(String[] args) throws IOException {
        Reactor ra = new Reactor();
        while (true) {
            ra.run();
        }
    }

    static  class Reactor implements Runnable{
        final Selector selector;

        public Reactor() throws IOException {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(8080));


            //  绑定 serverSocket 与 当前selector 的关系 ， 也就是accept ， 监听accept
            SelectionKey selectionKey = serverSocketChannel.register(selector,0);
            selectionKey.interestOps(SelectionKey.OP_ACCEPT);
            selectionKey.attach(new Acceptor(selectionKey,serverSocketChannel));

        }

        @Override
        public void run() {
            try{
                while(true){
                    int num = selector.select();
                    if(num>0){
                        Set<SelectionKey> selectionKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = selectionKeys.iterator();
                        while(iterator.hasNext()){
                            SelectionKey key = iterator.next();
                            dispatch(key);
                            iterator.remove();
                        }
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        public void dispatch(SelectionKey key){
            Runnable r = (Runnable) key.attachment();
            if(r!=null) r.run();
        }
    }

    static class Acceptor implements Runnable{
        final ServerSocketChannel serverSocketChannel;
        final SelectionKey selectionKey;
        public Acceptor(SelectionKey selectionKey,ServerSocketChannel serverSocketChannel){
            this.selectionKey = selectionKey;
            this.serverSocketChannel = serverSocketChannel;
        }

        @Override
        public void run() {
            try {
                //  接收到客户端的连接 ，将下一步操作绑定到 selector 上
                SocketChannel c = serverSocketChannel.accept();
                c.configureBlocking(false);
                SelectionKey key = c.register(selectionKey.selector(),SelectionKey.OP_READ);

                new Handler(key);
                System.out.println("收到新连接");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class Handler implements  Runnable{
        final SelectionKey selectionKey;
        final SocketChannel socketChannel;
        final Selector selector;
        final int MAX_IN = 1024;
        final int MAX_OUT = 1024;
        ByteBuffer input = ByteBuffer.allocate(MAX_IN);
        ByteBuffer output = ByteBuffer.allocate(MAX_OUT);

        private byte status = 0; //  0 : reading       1 : writing
        public Handler(SelectionKey selectionKey){
            selector = selectionKey.selector();
            this.selectionKey = selectionKey;
            this.socketChannel =(SocketChannel) selectionKey.channel();
            selectionKey.attach(this);
            selector.wakeup();

        }


        @Override
        public void run() {
            try {
                if(status == 0 ){
                    read();
                }else if(status == 1){
                    write();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        void read() throws IOException {
            socketChannel.read(input);
            if(isInputComplete()){
                input.flip();
                process();   //  业务处理
                input.clear();
                status = 1;
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
        }

        void write() throws IOException {
            output.flip();
            socketChannel.write(output);
            if(isOutPutComplete()){
                output.clear();
                selectionKey.cancel();
            }
        }


        boolean isInputComplete(){
            return input.position() > 0;
        }

        boolean isOutPutComplete(){
            return !output.hasRemaining();
        }
        //  业务处理
        void process(){
             //  模拟业务处理
            System.out.println(new String(input.array(),input.position(),input.limit()));
//            LockSupport.park(TimeUnit.MILLISECONDS.toNanos(300));
            String msg = "Now time is " + (new Date()).toString();
            output.put(msg.getBytes(StandardCharsets.UTF_8));
        }
    }
}

```

###### **单Reactor多线程：**

**Demo06**：

![image-20220805194319372](D:/markdown_pics/image-20220805194319372.png)

```java
public class GroupChatServer {
    //定义属性
    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    //构造器
    //初始化工作
    public GroupChatServer() {
        try {
            //得到选择器
            selector = Selector.open();
            //ServerSocketChannel
            listenChannel =  ServerSocketChannel.open();
            //绑定端口
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            //设置非阻塞模式
            listenChannel.configureBlocking(false);
            //将该listenChannel 注册到selector
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    //监听
    public void listen() {
        System.out.println("监听线程: " + Thread.currentThread().getName());
        System.out.println("-----------------------------------------------------------");
        try {
            //循环处理
            while (true) {
                int count = selector.select();
                if(count > 0) {//有事件处理
                    //遍历得到selectionKey 集合
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        //取出selectionkey
                        SelectionKey key = iterator.next();

                        //监听到accept
                        if(key.isAcceptable()) {
                            SocketChannel sc = listenChannel.accept();
                            sc.configureBlocking(false);
                            //将该 sc 注册到seletor
                            sc.register(selector, SelectionKey.OP_READ);
                            //提示
                            System.out.println(sc.getRemoteAddress() + " 上线 ");
                            System.out.println("-----------------------------------------------------------");
                        }
                        if(key.isReadable()) { //通道发送read事件，即通道是可读的状态
                            //处理读 (专门写方法..)
                            readData(key);
                        }
                        //当前的key 删除，防止重复处理
                        iterator.remove();
                    }
                } else {
                    System.out.println("等待....");
                }
            }

        }catch (Exception e) {
            e.printStackTrace();

        }finally {
            //发生异常处理....

        }
    }

    //读取客户端消息
    private void readData(SelectionKey key) {
        SocketChannel channel = null;

        try {
        //取到关联的channle
        //得到channel
        channel = (SocketChannel) key.channel();
        //创建buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int count = channel.read(buffer);
        //把缓存区的数据转成字符串
        String msg = new String(buffer.array());
        executorService.execute(()->{
            System.out.println("业务逻辑处理线程: " + Thread.currentThread().getName());
            //根据count的值做处理
            if (count > 0) {
                //输出该消息
                System.out.println("form 客户端: " + msg);
                System.out.println("-----------------------------------------------------------");
            }
        });
        //向其它的客户端转发消息(去掉自己), 专门写一个方法来处理
        sendInfoToOtherClients(msg, channel);
        }catch (Exception e){
            try {
                System.out.println(channel.getRemoteAddress() + " 离线了..");
                //取消注册
                key.cancel();
                //关闭通道
                channel.close();
            }catch (IOException e2) {
                e2.printStackTrace();;
            }
        }
    }
    //转发消息给其它客户(通道)
    private void sendInfoToOtherClients(String msg, SocketChannel self ) throws  IOException{
        System.out.println("服务器转发消息中...");
        System.out.println("服务器转发数据给客户端线程: " + Thread.currentThread().getName());

        //遍历 所有注册到selector 上的 SocketChannel,并排除 self
        for(SelectionKey key: selector.keys()) {
            //通过 key  取出对应的 SocketChannel
            Channel targetChannel = key.channel();

            //排除自己
            if(targetChannel instanceof  SocketChannel && targetChannel != self) {
                //转型
                SocketChannel dest = (SocketChannel)targetChannel;
                //将msg 存储到buffer
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                //将buffer 的数据写入 通道
                dest.write(buffer);
            }
        }

    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.execute(()->{
            //创建服务器对象
            GroupChatServer groupChatServer = new GroupChatServer();
            groupChatServer.listen();
        });
    }
}
//可以写一个Handler
class MyHandler {
    public void readData() {

    }
    public void sendInfoToOtherClients(){

    }
}

```

###### 多Reactor：

**Demo07：**

![image-20220805194705229](D:/markdown_pics/image-20220805194705229.png)



```java
package network.NIOchannel.reactor;


import java.io.IOException;

import java.net.InetSocketAddress;

import java.nio.channels.SelectionKey;

import java.nio.channels.Selector;

import java.nio.channels.ServerSocketChannel;

import java.nio.channels.SocketChannel;

import java.util.Iterator;

import java.util.Set;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

class MultiReactor {

    ServerSocketChannel serverSocket;

    AtomicInteger next = new AtomicInteger(0);

    Selector bossSelector = null;

    Reactor bossReactor = null;

    //selectors集合,引入多个selector选择器

    //多个选择器可以更好的提高通道的并发量

    Selector[] workSelectors = new Selector[2];

    //引入多个子反应器

    //如果CPU是多核的可以开启多个子Reactor反应器,这样每一个子Reactor反应器还可以独立分配一个线程。

    //每一个线程可以单独绑定一个单独的Selector选择器以提高通道并发量

    Reactor[] workReactors = null;

    MultiReactor() throws IOException {

        bossSelector = Selector.open();

        //初始化多个selector选择器

        workSelectors[0] = Selector.open();

        workSelectors[1] = Selector.open();

        serverSocket = ServerSocketChannel.open();

        InetSocketAddress address =

                new InetSocketAddress("localhost",

                        8080);

        serverSocket.socket().bind(address);

        //非阻塞

        serverSocket.configureBlocking(false);

        //第一个selector,负责监控新连接事件

        SelectionKey sk =

                serverSocket.register(bossSelector, SelectionKey.OP_ACCEPT);

        //附加新连接处理handler处理器到SelectionKey（选择键）

        sk.attach(new AcceptorHandler());

        //处理新连接的反应器

        bossReactor = new Reactor(bossSelector);

        //第一个子反应器，一子反应器负责一个选择器

        Reactor subReactor1 = new Reactor(workSelectors[0]);

        //第二个子反应器，一子反应器负责一个选择器

        Reactor subReactor2 = new Reactor(workSelectors[1]);

        workReactors = new Reactor[]{subReactor1, subReactor2};

    }

    private void startService() {

        new Thread(bossReactor).start();

        // 一子反应器对应一条线程

        new Thread(workReactors[0]).start();

        new Thread(workReactors[1]).start();

    }

    //反应器

    class Reactor implements Runnable {

        //每条线程负责一个选择器的查询

        final Selector selector;

        public Reactor(Selector selector) {

            this.selector = selector;

        }

        public void run() {

            try {

                while (!Thread.interrupted()) {

                    //单位为毫秒

                    //每隔一秒列出选择器感应列表

                    selector.select(1000);

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();

                    if (null == selectedKeys || selectedKeys.size() == 0) {

                        //如果列表中的通道注册事件没有发生那就继续执行

                        continue;

                    }

                    Iterator<SelectionKey> it = selectedKeys.iterator();

                    while (it.hasNext()) {

                        //Reactor负责dispatch收到的事件

                        SelectionKey sk = it.next();

                        dispatch(sk);

                    }

                    //清楚掉已经处理过的感应事件，防止重复处理

                    selectedKeys.clear();

                }

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }

        void dispatch(SelectionKey sk) {

            Runnable handler = (Runnable) sk.attachment();

            //调用之前attach绑定到选择键的handler处理器对象

            if (handler != null) {

                handler.run();

            }

        }

    }

    // Handler:新连接处理器

    class AcceptorHandler implements Runnable {

        public void run() {

            try {

                SocketChannel channel = serverSocket.accept();

                System.out.println(("接收到一个新的连接"));

                if (channel != null) {

                    int index = next.get();

                    System.out.println(("选择器的编号：" + index));

                    Selector selector = workSelectors[index];

                    new MultiThreadHandler(selector, channel);

                }

            } catch (IOException e) {

                e.printStackTrace();

            }

            if (next.incrementAndGet() == workSelectors.length) {

                next.set(0);

            }

        }

    }

    public static void main(String[] args) throws IOException {

        MultiReactor server =

                new MultiReactor();

        server.startService();

    }

}


package network.NIOchannel.reactor;



import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.channels.SelectionKey;

import java.nio.channels.Selector;

import java.nio.channels.SocketChannel;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

class MultiThreadHandler implements Runnable {

    final SocketChannel channel;

    final SelectionKey sk;

    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    static final int RECIEVING = 0, SENDING = 1;

    int state = RECIEVING;

    //引入线程池

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    MultiThreadHandler(Selector selector, SocketChannel c) throws IOException {

        channel = c;

        channel.configureBlocking(false);

        //唤醒选择,防止register时 boss线程被阻塞，netty 处理方式比较优雅，会在同一个线程注册事件，避免阻塞boss

        selector.wakeup();

        //仅仅取得选择键，后设置感兴趣的IO事件

        sk = channel.register(selector, 0);

        //将本Handler作为sk选择键的附件，方便事件dispatch

        sk.attach(this);

        //向sk选择键注册Read就绪事件

        sk.interestOps(SelectionKey.OP_READ);

        //唤醒选择，是的OP_READ生效

        selector.wakeup();


    }

    public void run() {

        //异步任务，在独立的线程池中执行

        pool.execute(new AsyncTask());

    }

    //异步任务，不在Reactor线程中执行

    public synchronized void asyncRun() {

        try {

            if (state == SENDING) {

                //写入通道

                channel.write(byteBuffer);

                //写完后,准备开始从通道读,byteBuffer切换成写模式

                byteBuffer.clear();

                //写完后,注册read就绪事件

                sk.interestOps(SelectionKey.OP_READ);

                //写完后,进入接收的状态

                state = RECIEVING;

            } else if (state == RECIEVING) {

                //从通道读

                int length = 0;

                while ((length = channel.read(byteBuffer)) > 0) {

                    System.out.println((new String(byteBuffer.array(), 0, length)));

                }

                //读完后，准备开始写入通道,byteBuffer切换成读模式

                byteBuffer.flip();

                //读完后，注册write就绪事件

                sk.interestOps(SelectionKey.OP_WRITE);

                //读完后,进入发送的状态

                state = SENDING;

            }

            //处理结束了, 这里不能关闭select key，需要重复使用

            //sk.cancel();

        } catch (IOException ex) {

            ex.printStackTrace();

        }

    }

    //异步任务的内部类

    class AsyncTask implements Runnable {

        public void run() {

            MultiThreadHandler.this.asyncRun();

        }

    }

}
```



##### 4)  信号驱动IO模型：



##### **5)  异步IO模型**



###  Netty：

**工作基本原理：**

![image-20220808165506670](D:/markdown_pics/image-20220808165506670.png)



![image-20220808165629581](D:/markdown_pics/image-20220808165629581.png)

![image-20220808174842329](D:/markdown_pics/image-20220808174842329.png)

#### QuickStart:

**Client:**

```java
package netty.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    public static void main(String[] args) throws Exception {

        //客户端需要一个事件循环组
        EventLoopGroup group = new NioEventLoopGroup();


        try {
            //创建客户端启动对象
            //注意客户端使用的不是 ServerBootstrap 而是 Bootstrap
            Bootstrap bootstrap = new Bootstrap();

            //设置相关参数
            bootstrap.group(group) //设置线程组
                    .channel(NioSocketChannel.class) // 设置客户端通道的实现类(反射)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyClientHandler()); //加入自己的处理器
                        }
                    });

            System.out.println("客户端 ok..");

            //启动客户端去连接服务器端
            //关于 ChannelFuture 要分析，涉及到netty的异步模型
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            //给关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }finally {

            group.shutdownGracefully();

        }
    }
}

```

 **CilentHandler:**

```java

package netty.simple;

        import io.netty.buffer.ByteBuf;
        import io.netty.buffer.Unpooled;
        import io.netty.channel.ChannelHandlerContext;
        import io.netty.channel.ChannelInboundHandlerAdapter;
        import io.netty.util.CharsetUtil;

        import java.nio.charset.StandardCharsets;

/**
 *  自定义handler 继承 规定好的handlerAdapter
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    //当通道就绪就会触发该方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client " + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, server: (>^ω^<)喵", CharsetUtil.UTF_8));
    }

    //当通道有读取事件时，会触发
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务器回复的消息:" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("服务器的地址： "+ ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

```

**Server:**

```java
package netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();  //  处理连接请求
        EventLoopGroup workerGroup = new NioEventLoopGroup();  //  客户端处理交给workerGroup
        // bossGroup 和 workerGroup 含有子线程的个数 默认为实际 cpu核数*2

        //  创建服务器端启动对象 , 配置参数
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            //  链式编程进行设置
            serverBootstrap.group(bossGroup,workerGroup) //  使用两个线程组
                    .channel(NioServerSocketChannel.class)  //  使用NioSctpServerChannel  作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG,128) //  设置线程队列得到连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true)  //  设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //  给pieline 设置处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });  //  给workerGroup的 EventLoop 对应的管道设置处理器
            System.out.println("Server Ready...");
            //  绑定一个端口并且同步，生成ChannelFuture对象
            //  启动服务器并绑定通道
            ChannelFuture cf = serverBootstrap.bind(8080).sync();

            //  对关闭通道进行监听
            cf.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

```

**ServerHandler:**

```java
package netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 *  自定义handler 继承 规定好的handlerAdapter
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 读取数据实际  读取客户端发送的消息
     * @param ctx  上下文对象 含有 管道pipeline ， 通道channel ， 地址
     * @param msg  客户端发送的数据 默认obj
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Sever ctx = " + ctx);
        // 讲msg 转成一个ByteBuffer  由netty提供 性能更好
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送消息是 ： " + buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址" + ctx.channel().remoteAddress());
    }

    /**
     *  数据读取完毕
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // write+flush  将数据写入缓存并刷新
        //  一般讲，对发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello , world" , CharsetUtil.UTF_8));

    }

    /**
     * 处理异常
     */

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

```

#### 任务队列Task:

> 每一个EventLoop中有一个TaskQueue队列，可以进行异步执行。

**三种经典使用场景：**

- 用户程序自定义的普通任务

在ServerHandler里Read方法中添加：

```java
 //比如这里我们有一个非常耗时长的业务-> 异步执行 -> 提交该channel 对应的
        //NIOEventLoop 的 taskQueue中,

        //解决方案1 用户程序自定义的普通任务

        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵2", CharsetUtil.UTF_8));
                    System.out.println("channel code=" + ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发生异常" + ex.getMessage());
                }
            }
        });

        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵3", CharsetUtil.UTF_8));
                    System.out.println("channel code=" + ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发生异常" + ex.getMessage());
                }
            }
        });

//  两个run方法是在同一个线程进行执行 运行时间为10s

```

通过异步进行处理

- 用户自定义定时任务

  ```java
   //解决方案2 : 用户自定义定时任务 -》 该任务是提交到 scheduleTaskQueue中
  
          ctx.channel().eventLoop().schedule(new Runnable() {
              @Override
              public void run() {
  
                  try {
                      Thread.sleep(5 * 1000);
                      ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵4", CharsetUtil.UTF_8));
                      System.out.println("channel code=" + ctx.channel().hashCode());
                  } catch (Exception ex) {
                      System.out.println("发生异常" + ex.getMessage());
                  }
              }
          }, 5, TimeUnit.SECONDS);
  ```

- 非当前Reactor线程调用Channel的各种方法：再推送系统的业务线程里面，根据用户的表示，找到对应的Channel引用，然后通过Write类方法向该用户推送消息，就会进入到这种该场景。最终的Write会提交到任务队列中后被异步消费

![image-20220810180703503](D:/markdown_pics/image-20220810180703503.png)

#### 异步模型：

##### 基本介绍：

1) 异步的概念和同步相对。当一个异步过程调用发出后，调用者不能立刻得到结果。实际处理这个调用的组件在 完成后，通过状态、通知和回调来通知调用者。 

2)  Netty 中的 I/O 操作是异步的，包括 Bind、Write、Connect 等操作会简单的返回一个 ChannelFuture。

3)  调用者并不能立刻获得结果，而是通过 Future-Listener 机制，用户可以方便的主动获取或者通过通知机制获得 IO 操作结果

4)  Netty 的异步模型是建立在 future 和 callback 的之上的。callback 就是回调。重点说 Future，它的核心思想 是：假设一个方法 fun，计算过程可能非常耗时，等待 fun 返回显然不合适。那么可以在调用 fun 的时候，立 马返回一个 Future，后续可以通过 Future 去监控方法 fun 的处理过程(即 ： Future-Listener机制)

   

##### Future：

1) 表示异步的执行结果, 可以通过它提供的方法来检测执行是否完成，比如检索计算等等. 
2)  ChannelFuture 是一个接口 ： public interface ChannelFuture extends Future 我们可以添加监听器，当监听的事件发生时，就会通知到监听器. 案例说明

##### 工作原理：

![image-20220810183651432](D:/markdown_pics/image-20220810183651432.png) 

> 说明：
>
> 1):在使用 Netty 进行编程时，拦截操作和转换出入站数据只需要您提供 callback 或利用 future 即可。这使得链 式操作简单、高效, 并有利于编写可重用的、通用的代码
>
> 2): Netty 框架的目标就是让业务逻辑从网络基础应用编码中分离出来、解脱出来

##### Future-Listener 机制：

1) 当 Future 对象刚刚创建时，处于非完成状态，调用者可以通过返回的 ChannelFuture 来获取操作执行的状态， 注册监听函数来执行完成后的操作。 
2) 常见有如下操作
   - 通过 isDone 方法来判断当前操作是否完成
   -  通过 isSuccess 方法来判断已完成的当前操作是否成功；
   - 通过 getCause 方法来获取已完成的当前操作失败的原因
   - 通过 isCancelled 方法来判断已完成的当前操作是否被取消
   - 通过 addListener 方法来注册监听器，当操作已完成(isDone 方法返回完成)，将会通知指定的监听器；如果 Future 对象已完成，则通知指定的监听器

eg：

```java
//绑定一个端口并且同步, 生成了一个 ChannelFuture 对象
//启动服务器(并绑定端口)
ChannelFuture cf = bootstrap.bind(6668).sync();
//给 cf 注册监听器，监控我们关心的事件
cf.addListener(new ChannelFutureListener() {
@Override
public void operationComplete(ChannelFuture future) throws Exception {
        if (cf.isSuccess()) {
        System.out.println("监听端口 6668 成功");
        } else {
        System.out.println("监听端口 6668 失败");
		}
	}
});
```

![image-20220810185032525](D:/markdown_pics/image-20220810185032525.png)



#### HTTP服务：

1) 实例要求：使用 IDEA 创建 Netty 项目 

2) Netty 服务器在 6668 端口监听，浏览器发出请求 "http://localhost:8080/ "

3) 服务器可以回复消息给客户端 "Hello! 我是服务器 5 " , 并对特定请求资源进行过滤. 
4) 目的：Netty 可以做 Http 服务开发，并且理解 Handler 实例和客户端及其请求的

```java
package com.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

/*
说明
1. SimpleChannelInboundHandler 是 ChannelInboundHandlerAdapter
2. HttpObject 客户端和服务器端相互通讯的数据被封装成 HttpObject
 */
public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    //channelRead0 读取客户端数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {


        System.out.println("对应的channel=" + ctx.channel() + " pipeline=" + ctx
        .pipeline() + " 通过pipeline获取channel" + ctx.pipeline().channel());

        System.out.println("当前ctx的handler=" + ctx.handler());

        //判断 msg 是不是 httprequest请求
        if(msg instanceof HttpRequest) {

            System.out.println("ctx 类型="+ctx.getClass());

            System.out.println("pipeline hashcode" + ctx.pipeline().hashCode() + " TestHttpServerHandler hash=" + this.hashCode());

            System.out.println("msg 类型=" + msg.getClass());
            System.out.println("客户端地址" + ctx.channel().remoteAddress());

            //获取到
            HttpRequest httpRequest = (HttpRequest) msg;
            //获取uri, 过滤指定的资源
            URI uri = new URI(httpRequest.uri());
            if("/favicon.ico".equals(uri.getPath())) {
                System.out.println("请求了 favicon.ico, 不做响应");
                return;
            }
            //回复信息给浏览器 [http协议]

            ByteBuf content = Unpooled.copiedBuffer("hello, 我是服务器", CharsetUtil.UTF_8);

            //构造一个http的相应，即 httpresponse
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            //将构建好 response返回
            ctx.writeAndFlush(response);

        }
    }



}


```

**Server:**

```java
package netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TestServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();

        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(boss,work).channel(NioServerSocketChannel.class).childHandler(new TestServerInitializer());

            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();

            channelFuture.channel().closeFuture().sync();

        }finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }
}


```

**ChannelInitializer**



```java
 package com.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;


public class TestServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        //向管道加入处理器

        //得到管道
        ChannelPipeline pipeline = ch.pipeline();

        //加入一个netty 提供的httpServerCodec codec =>[coder - decoder]
        //HttpServerCodec 说明
        //1. HttpServerCodec 是netty 提供的处理http的 编-解码器
        pipeline.addLast("MyHttpServerCodec",new HttpServerCodec());
        //2. 增加一个自定义的handler
        pipeline.addLast("MyTestHttpServerHandler", new TestHttpServerHandler());

        System.out.println("ok~~~~");

    }
}

```

#### Netty核心模块组件

######  Bootstrap、ServerBootstrap

>  Bootstrap 意思是引导，一个 Netty 应用通常由一个 Bootstrap 开始，主要作用是配置整个 Netty 程序，串联 各个组件，Netty 中 Bootstrap 类是客户端程序的启动引导类，ServerBootstrap 是服务端启动引导类

**常见方法：**

public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup)，该方法用于服务器端， 用来设置两个 EventLoop

public B group(EventLoopGroup group) ，该方法用于客户端，用来设置一个 EventLoop

public B channel(Class channelClass)，该方法用来设置一个服务器端的通道实现

public  B option(ChannelOption option, T value)，用来给 ServerChannel 添加配置

public  ServerBootstrap childOption(ChannelOption childOption, T value)，用来给接收到的通道添加配置

public ServerBootstrap childHandler(ChannelHandler childHandler)，该方法用来设置业务处理类（自定义的 handler）

public ChannelFuture bind(int inetPort) ，该方法用于服务器端，用来设置占用的端口号

public ChannelFuture connect(String inetHost, int inetPort) ，该方法用于客户端，用来连接服务器端



######  Future、ChannelFuture

> Netty 中所有的 IO 操作都是异步的，不能立刻得知消息是否被正确处理。但是可以过一会等它执行完成或 者直接注册一个监听，具体的实现就是通过 Future 和 ChannelFutures，他们可以注册一个监听，当操作执行成功 或失败时监听会自动触发注册的监听事件

**常见方法：**

Channel channel()，返回当前正在进行 IO 操作的通道 ChannelFuture sync()，等待异步操作执行完毕

###### Channel

> - Netty 网络通信的组件，能够用于执行网络 I/O 操作
>
> - 通过 Channel 可获得当前网络连接的通道
> - 通过 Channel 可获得 网络连接的配置参数 （例如接收缓冲区
> - Channel 提供异步的网络 I/O 操作(如建立连接，读写，绑定端口)，异步调用意味着任何 I/O 调用都将立即返 回，并且不保证在调用结束时所请求的 I/O 操作已完成
> - 调用立即返回一个 ChannelFuture 实例，通过注册监听器到 ChannelFuture 上，可以 I/O 操作成功、失败或取 消时回调通知调用方
> - 支持关联 I/O 操作与对应的处理程序
> - 不同协议、不同的阻塞类型的连接都有不同的 Channel 类型与之对应：常见的Channel类型：

NioSocketChannel，异步的客户端 TCP Socket 连接。 

NioServerSocketChannel，异步的服务器端 TCP Socket 连接。

 NioDatagramChannel，异步的 UDP 连接。

 NioSctpChannel，异步的客户端 Sctp 连接。

 NioSctpServerChannel，异步的 Sctp 服务器端连接，这些通道涵盖了 UDP 和 TCP 网络 IO 以及文件 IO。

###### Selector

> - Netty 基于 Selector 对象**实现 I/O 多路复用**，通过 Selector 一个线程可以监听多个连接的 Channel
> - 当向一个 Selector 中注册 Channel 后，Selector 内部的机制就可以自动不断地查询(Select) 这些注册的 Channel 是否有已就绪的 I/O 事件（例如可读，可写，网络连接完成等），这样程序就可以很简单地使用一个 线程高效地管理多个 Channe

###### ChannelHandler 及实现类：

> -  ChannelHandler 是一个接口，处理 I/O 事件或拦截 I/O 操作，并将其转发到其 ChannelPipeline(业务处理链) 中的下一个处理程序。
> -  ChannelHandler 本身并没有提供很多方法，因为这个接口有许多的方法需要实现，方便使用期间，可以继承它 的子类

**ChannelHandler相关接口和类的一览图：**



![image-20220811144658988](D:/markdown_pics/image-20220811144658988.png)

 我们经常需要自定义一个 Handler 类去继承 ChannelInboundHandlerAdapter，然后通过重写相应方法实现业务 逻辑，我们接下来看看一般都需要重写哪些方法：

![image-20220811144814732](D:/markdown_pics/image-20220811144814732.png)

###### **Pipeline 和 ChannelPipeli

> -  ChannelPipeline 是一个 Handler 的集合，它负责处理和拦截 inbound 或者 outbound 的事件和操作，相当于 一个贯穿 Netty 的链。(也可以这样理解：ChannelPipeline 是 保存 ChannelHandler 的 List，用于处理或拦截 Channel 的入站事件和出站操作)
> -  ChannelPipeline 实现了一种高级形式的拦截过滤器模式，使用户可以完全控制事件的处理方式，以及 Channel 中各个的 ChannelHandler 如何相互交互

在 Netty 中每个 Channel 都有且仅有一个 ChannelPipeline 与之对应，它们的组成关系：

![image-20220811145208748](D:/markdown_pics/image-20220811145208748.png)

> Channel 与 ChannelPipeline 是互相包含的关系

**常用方法：**

- ChannelPipeline addFirst(ChannelHandler... handlers)，把一个业务处理类（handler）添加到链中的第一个位置
- ChannelPipeline addLast(ChannelHandler... handlers)，把一个业务处理类（handler）添加到链中的最后一个位置

###### ChannelHandlerContext

- 保存 Channel 相关的所有上下文信息，同时关联一个 ChannelHandler 对象
- 即 ChannelHandlerContext 中 包 含 一 个 具 体 的 事 件 处 理 器 ChannelHandler ， 同 时 ChannelHandlerContext 中也绑定了对应的 pipeline 和 Channel 的信息，方便对 ChannelHandler 进行调用.

**常用方法：**

![image-20220811145632833](D:/markdown_pics/image-20220811145632833.png)

###### ChannelOption

> Netty 在创建 Channel 实例后,一般都需要设置 ChannelOption 参数。

**ChannelOption 参数如下:**

![image-20220811145720517](D:/markdown_pics/image-20220811145720517.png)

###### EventLoopGroup 和其实现类 NioEventLoopGroup

> -  EventLoopGroup 是一组 EventLoop 的抽象，Netty 为了更好的利用多核 CPU 资源，一般会有多个 EventLoop 同时工作，每个 EventLoop 维护着一个 Selector 实例。
> -  EventLoopGroup 提供 next 接口，可以从组里面按照一定规则获取其中一个 EventLoop 来处理任务。在 Netty 服 务 器 端 编 程 中 ， 我 们 一 般 都 需 要 提 供 两 个 EventLoopGroup ， 例 如 ： BossEventLoopGroup 和 WorkerEventLoopGroup。
> - 通常一个服务端口即一个 ServerSocketChannel 对应一个 Selector 和一个 EventLoop 线程。BossEventLoop 负责 接收客户端的连接并将 SocketChannel 交给 WorkerEventLoopGroup 来进行 IO 处理，如下图所示

![image-20220811150409680](D:/markdown_pics/image-20220811150409680.png)

**常见方法**

public NioEventLoopGroup()，构造方法

public Future shutdownGracefully()，断开连接，关闭线程



##### Unpooled类

> Netty 提供一个专门用来操作缓冲区(即 Netty 的数据容器)的工具

![image-20220812005019254](D:/markdown_pics/image-20220812005019254.png)

创建一个ByteBuf

> 创建 对象，该对象包含一个数组arr , 是一个byte[]，在netty 的buffer中，不需要使用flip 进行反转
>
> 底层维护了 **readerindex** 和 **writerIndex**
>
> 通过 readerindex 和  writerIndex 和  capacity， 将buffer分成三个区域*
>
> ​     0---readerindex 已经读取的区域
>
> ​     readerindex---writerIndex ， 可读的区域
>
> ​    writerIndex -- capacity, 可写的区域
>
> ​	getByte(index)   得到index位置的byte
>
> ​	readByte()  读取readIndex位置的byte





#### Netty群聊系统：

![image-20220811235457137](D:/markdown_pics/image-20220811235457137.png)

Demo：

**Client:**

```java
package com.netty.groupchat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;


public class GroupChatClient {

    //属性
    private final String host;
    private final int port;

    public GroupChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try {


        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {

                        //得到pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        //加入相关handler
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        //加入自定义的handler
                        pipeline.addLast(new GroupChatClientHandler());
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        //得到channel
            Channel channel = channelFuture.channel();
            System.out.println("-------" + channel.localAddress()+ "--------");
            //客户端需要输入信息，创建一个扫描器
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                //通过channel 发送到服务器端
                channel.writeAndFlush(msg + "\r\n");
            }
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new GroupChatClient("127.0.0.1", 7000).run();
    }
}

```

**ClientHandler:**

```java
package com.netty.groupchat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class GroupChatClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg.trim());
    }
}

```

**Server:**

```java
package com.netty.groupchat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class GroupChatServer {

    private int port; //监听端口


    public GroupChatServer(int port) {
        this.port = port;
    }

    //编写run方法，处理客户端的请求
    public void run() throws  Exception{

        //创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //8个NioEventLoop

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            //获取到pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            //向pipeline加入解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            //向pipeline加入编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new GroupChatServerHandler());

                        }
                    });

            System.out.println("netty 服务器启动");
            ChannelFuture channelFuture = b.bind(port).sync();

            //监听关闭
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {

        new GroupChatServer(7000).run();
    }
}

```

**ServerHandler:**

```java
package com.atguigu.netty.groupchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    //public static List<Channel> channels = new ArrayList<Channel>();

    //使用一个hashmap 管理
    //public static Map<String, Channel> channels = new HashMap<String,Channel>();

    //定义一个channle 组，管理所有的channel
    //GlobalEventExecutor.INSTANCE) 是全局的事件执行器，是一个单例
    private static ChannelGroup  channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    //handlerAdded 表示连接建立，一旦连接，第一个被执行
    //将当前channel 加入到  channelGroup
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //将该客户加入聊天的信息推送给其它在线的客户端
        /*
        该方法会将 channelGroup 中所有的channel 遍历，并发送 消息，
        我们不需要自己遍历
         */
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + " 加入聊天" + sdf.format(new java.util.Date()) + " \n");
        channelGroup.add(channel);




    }

    //断开连接, 将xx客户离开信息推送给当前在线的客户
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + " 离开了\n");
        System.out.println("channelGroup size" + channelGroup.size());

    }

    //表示channel 处于活动状态, 提示 xx上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println(ctx.channel().remoteAddress() + " 上线了~");
    }

    //表示channel 处于不活动状态, 提示 xx离线了
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        System.out.println(ctx.channel().remoteAddress() + " 离线了~");
    }

    //读取数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        //获取到当前channel
        Channel channel = ctx.channel();
        //这时我们遍历channelGroup, 根据不同的情况，回送不同的消息

        channelGroup.forEach(ch -> {
            if(channel != ch) { //不是当前的channel,转发消息
                ch.writeAndFlush("[客户]" + channel.remoteAddress() + " 发送了消息" + msg + "\n");
            }else {//回显自己发送的消息给自己
                ch.writeAndFlush("[自己]发送了消息" + msg + "\n");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //关闭通道
        ctx.close();
    }
}

```

#### Netty心跳机制：

>  编写一个 Netty 心跳检测机制案例, 当服务器超过 3 秒没有读时，就提示读空闲
>
>  当服务器超过 5 秒没有写操作时，就提示写空闲
>
> 实现当服务器超过 7 秒没有读或者写操作时，就提示读写空闲

**Server：**

```java
package netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class MyServer {
    public static void main(String[] args) throws Exception{


        //创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //8个NioEventLoop
        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    //加入一个netty 提供 IdleStateHandler
                    /*
                    说明
                    1. IdleStateHandler 是netty 提供的处理空闲状态的处理器
                    2. long readerIdleTime : 表示多长时间没有读, 就会发送一个心跳检测包检测是否连接
                    3. long writerIdleTime : 表示多长时间没有写, 就会发送一个心跳检测包检测是否连接
                    4. long allIdleTime : 表示多长时间没有读写, 就会发送一个心跳检测包检测是否连接

                    5. 文档说明
                    triggers an {@link IdleStateEvent} when a {@link Channel} has not performed
 * read, write, or both operation for a while.
 *                  6. 当 IdleStateEvent 触发后 , 就会传递给管道 的下一个handler去处理
 *                  通过调用(触发)下一个handler 的 userEventTiggered , 在该方法中去处理 IdleStateEvent(读空闲，写空闲，读写空闲)
                     */
                    pipeline.addLast(new IdleStateHandler(3,5,7, TimeUnit.SECONDS));
                    //加入一个对空闲检测进一步处理的handler(自定义)
                    pipeline.addLast(new MyServerHandler());
                }
            });

            //启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```

**Client:**

```java
package netty.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class MyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     *
     * @param ctx 上下文
     * @param evt 事件
     * @throws Exception
     */

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(evt instanceof IdleStateEvent) {

            //将  evt 向下转型 IdleStateEvent
            IdleStateEvent event = (IdleStateEvent) evt;
            String eventType = null;
            switch (event.state()) {
                case READER_IDLE:
                    eventType = "读空闲";
                    break;
                case WRITER_IDLE:
                    eventType = "写空闲";
                    break;
                case ALL_IDLE:
                    eventType = "读写空闲";
                    break;
            }
            System.out.println(ctx.channel().remoteAddress() + "--超时时间--" + eventType);
            System.out.println("服务器做相应处理..");

            //如果发生空闲，我们关闭通道
            // ctx.channel().close();
        }
    }
}
```

#### Netty实现长连接：

> 通过 WebSocket 编程实现服务器和客户端长连接



**要求：**

- Http 协议是无状态的, 浏览器和服务器间的请求响应一次，下一次会重新创建连接.
- 实现基于 webSocket 的长连接的全双工的交互
-  改变 Http 协议多次请求的约束，实现长连接了， 服务器可以发送消息给浏览器
- 客户端浏览器和服务器端会相互感知，比如服务器关闭了，浏览器会感知，同样浏览器关闭了，服务器会感知
- 运行界面

**Server：**

```java
package netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class MyServer {
    public static void main(String[] args) throws Exception{


        //创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //8个NioEventLoop
        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    //因为基于http协议，使用http的编码和解码器
                    pipeline.addLast(new HttpServerCodec());
                    //是以块方式写，添加ChunkedWriteHandler处理器
                    pipeline.addLast(new ChunkedWriteHandler());

                    /*
                    说明
                    1. http数据在传输过程中是分段, HttpObjectAggregator ，就是可以将多个段聚合
                    2. 这就就是为什么，当浏览器发送大量数据时，就会发出多次http请求
                     */
                    pipeline.addLast(new HttpObjectAggregator(8192));
                    /*
                    说明
                    1. 对应websocket ，它的数据是以 帧(frame) 形式传递
                    2. 可以看到WebSocketFrame 下面有六个子类
                    3. 浏览器请求时 ws://localhost:7000/hello 表示请求的uri
                    4. WebSocketServerProtocolHandler 核心功能是将 http协议升级为 ws协议 , 保持长连接
                    5. 是通过一个 状态码 101
                     */
                    pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));

                    //自定义的handler ，处理业务逻辑
                    pipeline.addLast(new MyTextWebSocketFrameHandler());
                }
            });

            //启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

```



**ServerHandler：**

```java
package netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.time.LocalDateTime;

//这里 TextWebSocketFrame 类型，表示一个文本帧(frame)
public class MyTextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        System.out.println("服务器收到消息 " + msg.text());

        //回复消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器时间" + LocalDateTime.now() + " " + msg.text()));
    }

    //当web客户端连接后， 触发方法
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //id 表示唯一的值，LongText 是唯一的 ShortText 不是唯一
        System.out.println("handlerAdded 被调用" + ctx.channel().id().asLongText());
        System.out.println("handlerAdded 被调用" + ctx.channel().id().asShortText());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        System.out.println("handlerRemoved 被调用" + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常发生 " + cause.getMessage());
        ctx.close(); //关闭连接
    }
}
```

**html：**

```html

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<script>
    var socket;
    //判断当前浏览器是否支持websocket
    if(window.WebSocket) {
        //go on
        socket = new WebSocket("ws://localhost:7000/hello");
        //相当于channelReado, ev 收到服务器端回送的消息
        socket.onmessage = function (ev) {
            var rt = document.getElementById("responseText");
            rt.value = rt.value + "\n" + ev.data;
        }

        //相当于连接开启(感知到连接开启)
        socket.onopen = function (ev) {
            var rt = document.getElementById("responseText");
            rt.value = "连接开启了.."
        }

        //相当于连接关闭(感知到连接关闭)
        socket.onclose = function (ev) {

            var rt = document.getElementById("responseText");
            rt.value = rt.value + "\n" + "连接关闭了.."
        }
    } else {
        alert("当前浏览器不支持websocket")
    }

    //发送消息到服务器
    function send(message) {
        if(!window.socket) { //先判断socket是否创建好
            return;
        }
        if(socket.readyState == WebSocket.OPEN) {
            //通过socket 发送消息
            socket.send(message)
        } else {
            alert("连接没有开启");
        }
    }
</script>
    <form onsubmit="return false">
        <textarea name="message" style="height: 300px; width: 300px"></textarea>
        <input type="button" value="发生消息" onclick="send(this.form.message.value)">
        <textarea id="responseText" style="height: 300px; width: 300px"></textarea>
        <input type="button" value="清空内容" onclick="document.getElementById('responseText').value=''">
    </form>
</body>
</html>
```

#### Google Protobuf

##### 编码和解码的基本介绍

编写网络应用程序时，因为数据在网络中传输的都是二进制字节码数据，在发送数据时就需要编码，接收数据 时就需要解码 [示意图]

>  codec(编解码器) 的组成部分有两个：decoder(解码器)和 encoder(编码器)。encoder 负责把业务数据转换成字节 码数据，decoder 负责把字节码数据转换成业务数据

![image-20220812004220745](D:/markdown_pics/image-20220812004220745.png)



**Netty自身编码解码机制问题分析：**

![image-20220812004302594](D:/markdown_pics/image-20220812004302594.png)

![image-20220812004309019](D:/markdown_pics/image-20220812004309019.png)

##### Protobuf：

> Protobuf 是 Google 发布的开源项目，全称 Google Protocol Buffers，是一种轻便高效的结构化数据存储格式， 可以用于结构化数据串行化，或者说序列化。它很适合做数据存储或 RPC[远程过程调用 remote procedure call ] 数据交换格式 。

**目前很多公司 http+json tcp+protobu**

- Protobuf 是以 message 

- 支持跨平台、跨语言，即[客户端和服务器端可以是不同的语言编写的] （支持目前绝大多数语言，例如 C++、 C#、Java、python 等

- 高性能，高可靠性

- 使用 protobuf 编译器能自动生成代码，Protobuf 是将类的定义使用.proto 文件进行描述。说明，在 idea 中编 写 .proto 文件时，会自动提示是否下载 .ptotot 编写插件. 可以让语法高亮
- 然后通过 protoc.exe 编译器根据.proto 自动生成.java文件
- protobuf 使用示意图

![image-20220812004500248](D:/markdown_pics/image-20220812004500248.png)

**Student.proto**

```protobuf
syntax = "proto3"; //版本
option java_outer_classname = "StudentPOJO";//生成的外部类名，同时也是文件名
//protobuf 使用message 管理数据
message Student { //会在 StudentPOJO 外部类生成一个内部类 Student， 他是真正发送的POJO对象
    int32 id = 1; // Student 类中有 一个属性 名字为 id 类型为int32(protobuf类型) 1表示属性序号，不是值
    string name = 2;
}
```

**编译：**

```cmd
protoc.exe --java_out=. Student.proto
将生成的 StudentPOJO 放入到项目使用
```

> - 客户端可以随机发送 Student PoJo/ Worker PoJo 对象到服务器 (通过 Protobuf 编码
> - 服务端能接收 Student PoJo/ Worker PoJo 对象(需要判断是哪种类型)，并显示信息(通过 Protobuf 解码

```protobuf
syntax = "proto3";
option optimize_for = SPEED; // 加快解析
option java_package="com.atguigu.netty.codec2";   //指定生成到哪个包下
option java_outer_classname="MyDataInfo"; // 外部类名, 文件名

//protobuf 可以使用message 管理其他的message
message MyMessage {

    //定义一个枚举类型
    enum DataType {
        StudentType = 0; //在proto3 要求enum的编号从0开始
        WorkerType = 1;
    }

    //用data_type 来标识传的是哪一个枚举类型
    DataType data_type = 1;

    //表示每次枚举类型最多只能出现其中的一个, 节省空间
    oneof dataBody {
        Student student = 2;
        Worker worker = 3;
    }

}


message Student {
    int32 id = 1;//Student类的属性
    string name = 2; //
}
message Worker {
    string name=1;
    int32 age=2;
}
```

