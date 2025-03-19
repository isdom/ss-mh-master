package com.yulore.api;

import java.util.List;
import java.util.Map;

public interface MasterService {
    void updateHubStatus(
            // 远端服务发送时的远端时间戳
            final long timestamp,
            // 远端服务endpoint, 典型取值:  "127.0.0.1:8080"
            final String ipAndPort,
            // 路径到处理器的映射表, 典型取值 {/demo1=demoHandler, /demo2=demo2Handler, /demo3=demo3Handler}
            final Map<String, String> pathMapping,
            // 已 json 形式承载的可扩展信息
            final String infoAsJson
    );
    List<String> getUrlsOf(final String handler);
}
