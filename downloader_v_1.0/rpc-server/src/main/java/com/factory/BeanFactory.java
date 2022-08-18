package com.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * record <interfaceName , implName>  map
 */
public class BeanFactory {
    static Map<String,Class> map ;

    public BeanFactory() {
        map = new HashMap<>();
    }

    /**
     *
     * @param key Interface class name
     * @return a InterfaceImpl Obj
     *
     */
    public static Object getObj(String key) throws InstantiationException, IllegalAccessException {
        Class<?> value = map.get(key);
        if(value==null){
            throw new NullPointerException("no such class recorded");
        }else {

            return value.newInstance();

        }
    }

    /**
     *
     * @param key  : Interface class name
     * @param value : impl name
     */
    public static void regist(String key , Class value ){
        map.put(key,value);
    }
}
