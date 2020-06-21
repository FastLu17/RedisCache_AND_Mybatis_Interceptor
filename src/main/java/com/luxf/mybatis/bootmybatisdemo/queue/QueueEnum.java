package com.luxf.mybatis.bootmybatisdemo.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 单例的队列、
 *
 * @author 小66
 * @date 2020-06-21 14:32
 **/
public enum QueueEnum {
    /**
     * 单例对象、
     */
    INSTANCE;

    /**
     * 1. add和offer方法的区别在于，add方法在队列满的情况下将选择抛异常的方法来表示队列已经满了，而offer方法通过返回false表示队列已经满了；在有限队列的情况，使用offer方法优于add方法；
     *
     * 2. remove方法和poll方法都是删除队列的头元素，remove方法在队列为空的情况下将抛异常，而poll方法将返回null；
     *
     * 3. element和peek方法都是返回队列的头元素，但是不删除头元素，区别在与element方法在队列为空的情况下，将抛异常，而peek方法将返回null
     */
    private static final Queue<String> CONCURRENT_QUEUE = new ConcurrentLinkedQueue<>();

    public boolean offer(String string) {
        return CONCURRENT_QUEUE.offer(string);
    }

    public String poll() {
        return CONCURRENT_QUEUE.poll();
    }

    public String peek() {
        return CONCURRENT_QUEUE.peek();
    }

    public boolean isEmpty() {
        return CONCURRENT_QUEUE.isEmpty();
    }

}
