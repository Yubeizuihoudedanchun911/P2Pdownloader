//import com.alibaba.fastjson.JSON;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//public class DownloadTest {
//    public static ResponseEntity<byte[]> getFileContentByUrlAndPosition(String downloadUrl, long start, long end) {
//        final RestTemplate REST_TEMPLATE = new RestTemplate();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("Range", "bytes=" + start + "-" + end);
//
//
//
//        org.springframework.http.HttpEntity<Object> httpEntity = new org.springframework.http.HttpEntity<>(httpHeaders);
//        return REST_TEMPLATE.exchange(downloadUrl, HttpMethod.GET, httpEntity, byte[].class);
//    }
//    @Test
//    public void t1(){
//        String s= "www";
//        String ss = JSON.toJSONString(s);
//
//        System.out.println(ss);
//
//        String s1 = JSON.parseObject(ss,String.class);
//
//        System.out.println(s1);
//
//    }
//
//
//}
