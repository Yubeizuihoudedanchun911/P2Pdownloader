import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.bytebuddy.description.method.MethodDescription;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Test {
    class Te<T extends Object>{
        T obj;
        int count;

        public Te(T obj, int count) {
            this.obj = obj;
            this.count = count;
        }

        public T getObj() {
            return obj;
        }

        public void setObj(T obj) {
            this.obj = obj;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "Te{" +
                    "obj=" + obj +
                    ", count=" + count +
                    '}';
        }
    }

    class P{
        int hh;
        String name;

        public P(int hh, String name) {
            this.hh = hh;
            this.name = name;
        }

        @Override
        public String toString() {
            return "P{" +
                    "hh=" + hh +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof P)) return false;
            P p = (P) o;
            return hh == p.hh;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hh, name);
        }
    }
    @org.junit.jupiter.api.Test
    public void test() throws UnknownHostException {


        System.out.println(InetAddress.getByName("localhost").getHostAddress().hashCode());

        System.out.println(InetAddress.getByName("localhost").getHostAddress().hashCode());

    }
}
