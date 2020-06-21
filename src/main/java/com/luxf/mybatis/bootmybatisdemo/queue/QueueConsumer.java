package com.luxf.mybatis.bootmybatisdemo.queue;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 利用定时任务, 消费自定义的单例Queue队列、
 *
 * @author 小66
 * @date 2020-06-21 14:38
 **/
@Component
public class QueueConsumer {

    /**
     * fixedDelay：距离上次执行完1秒后、
     * fixedRate：距离上次执行开始1秒后、
     */
    @Scheduled(fixedDelay = 1000L)
    public void consumerLog() {
        QueueEnum instance = QueueEnum.INSTANCE;
        int maxSize = 0;
        while (!instance.isEmpty() && maxSize < 8) {
            String poll = instance.poll();
            System.out.println("poll = " + poll);
            // 可以对Log进行相应的CRUD操作
            maxSize++;
        }
        // 插入日志操作耗时2秒
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("LocalTime.now() = " + LocalTime.now());
    }
}
