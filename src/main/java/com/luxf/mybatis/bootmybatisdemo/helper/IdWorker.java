package com.luxf.mybatis.bootmybatisdemo.helper;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

/**
 * @author 小66
 * @date 2020-06-19 18:58
 **/
public class IdWorker {

    /**
     * 正常情况下就生成 String的ID和Long的ID、
     *
     * @param idType
     * @param <I>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <I extends Serializable> I nextId(Class<I> idType) {
        if ("String".equals(idType.getSimpleName())) {
            return (I) nextStringId();
        } else if ("Long".equals(idType.getSimpleName())) {
            return (I) nextLongId();
        } else if ("Integer".equals(idType.getSimpleName())) {
            String value = String.valueOf(System.currentTimeMillis());
            double random = Math.random() * 20;
            Object millis = (int) (Integer.parseInt(value.substring(value.length() - 6)) * random);
            return (I) millis;
        }
        throw new RuntimeException("生成ID异常！");
    }

    public static String nextStringId() {
        return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public static Long nextLongId() {
        return System.currentTimeMillis() * new Random(100).nextLong();
    }

}
