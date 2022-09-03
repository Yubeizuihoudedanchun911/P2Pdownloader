import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.bytebuddy.description.method.MethodDescription;

import java.io.Serializable;
import java.lang.reflect.Type;

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
    }
    @org.junit.jupiter.api.Test
    public void test(){
        Type type = new TypeToken<Te <P>>() {}.getType();
        P ps = new P(10,"woaini");
//        System.out.println(ps.toString());
        Te<P> ss = new Te<P>(ps,5);
        Gson gson = new Gson();

        String s =        gson.toJson(ss);
        System.out.println(s);

        Te t = JSON.parseObject(s,Te.class);
        System.out.println(t.count);
        System.out.println(t.getObj().toString());


//        P pp = JSON.parseObject(t.getObj().toString(),P.class);
        t = (Te<P>)t;
        System.out.println(t);


    }
}
