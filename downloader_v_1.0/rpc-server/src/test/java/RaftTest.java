//import com.raft.common.Node;
//import com.raft.common.RaftNode;
//import org.junit.jupiter.api.Test;
//
//import java.io.*;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class RaftTest {
//    @Test
//    public void s1(){
//        Map<String, Node> map = new ConcurrentHashMap<>();
//        Node n1 = new Node("127.0.0.1",7070);
//        Node n2 = new Node("127.0.0.1",8080);
//        Node n3 = new Node("127.0.0.1",6060);
//        map.put("s1",n1);
//        map.put("s2",n2);
//        map.put("s3",n3);
//        RaftNode s1 = new RaftNode("127.0.0.1", 8080, "s1");
//        s1.setNodeMap(map);
//        s1.start();
//
//
//    }
//
//    @Test
//    public void s2(){
//        Map<String, Node> map = new ConcurrentHashMap<>();
//        Node n1 = new Node("127.0.0.1",7070);
//        Node n2 = new Node("127.0.0.1",8080);
//        Node n3 = new Node("127.0.0.1",6060);
//        map.put("s1",n1);
//        map.put("s2",n2);
//        map.put("s3",n3);
//        RaftNode s1 = new RaftNode("127.0.0.1", 7070, "s2");
//        s1.setNodeMap(map);
//        s1.start();
//    }
//
//    @Test
//    public void s3() throws IOException {
//        File file = new File("E://Java/downloadTest","1-localhost_8080-1fcc640b-23cb-4ddc-9fc3-450481e50829" );
//        FileChannel fis = new FileInputStream(file).getChannel();
//        ByteBuffer buffer = ByteBuffer.allocateDirect(1 << 20);
//        int byteNum = fis.read(buffer);
//        byte[] bytes = new byte[byteNum];
//        buffer.flip();
//        buffer.get(bytes);
//
//        fis.close();
//        file.delete();
//    }
//
//}
