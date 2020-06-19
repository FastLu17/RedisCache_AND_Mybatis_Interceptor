package com.luxf.mybatis.bootmybatisdemo.controller;

import com.luxf.mybatis.bootmybatisdemo.service.ConcurrentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 小66
 * @date 2020-06-19 19:20
 **/
@RestController
public class ConcurrentController {
    private final ConcurrentService concurrentService;

    @Autowired
    public ConcurrentController(ConcurrentService concurrentService) {
        this.concurrentService = concurrentService;
    }

    @GetMapping("/runWithLua")
    public void runWithLua() throws Exception {
        // 比较通过Lua脚本和数据库操作, 是否会出现高并发的问题 --> 此处for循环加Async

        for (int i = 0; i < 1000; i++) {
            concurrentService.runWithLua();
        }
    }

    @GetMapping("/runWithDataBase")
    public void runWithDataBase() throws Exception {
        // 比较通过Lua脚本和数据库操作, 是否会出现高并发的问题 --> 此处for循环加Async
        for (int i = 0; i < 2000; i++) {
            concurrentService.runWithDataBase();
        }
    }
}
