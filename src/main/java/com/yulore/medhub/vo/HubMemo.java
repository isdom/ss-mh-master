package com.yulore.medhub.vo;

import java.util.Map;

public record HubMemo(
        // 远端服务endpoint, 典型取值:  "127.0.0.1:8080"
        String ipAndPort,
        // 路径到处理器的映射表, 典型取值 {/demo1=demoHandler, /demo2=demo2Handler, /demo3=demo3Handler}
        Map<String, String> pathMapping,
        // 记录时的时间戳
        long updateTimestamp) {
}
