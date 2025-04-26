package com.yulore.medhub;

import com.yulore.medhub.vo.HubMemo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class MasterServiceImpl implements LocalMasterService {
    private static final long HUB_UPDATE_TIMEOUT_IN_MS = 1000 * 30; // 30s

    @Override
    public void disableHubs(final String[] ips) {
        disableIPs.set(ips);
        log.info("disable hubs: {}", Arrays.toString(ips));
    }

    @Override
    public String status() {
        return String.format("disable ips: %s", disableIPs.get() != null ? Arrays.toString(disableIPs.get()) : "null");
    }

    @Override
    public void updateHubStatus(final long timestamp,
                                final String ipAndPort,
                                final Map<String, String> pathMapping,
                                final String infoAsJson) {
        final long now = System.currentTimeMillis();
        if (now - timestamp > HUB_UPDATE_TIMEOUT_IN_MS) {
            // out of date update, ignore
            return;
        }
        log.info("updateHubStatus: med-hub[{}] - pathMapping: {} - {}, info: {}", ipAndPort, pathMapping, timestamp, infoAsJson);
        hubMemos.put(ipAndPort, new HubMemo(ipAndPort, pathMapping, now));
        // 使用 compute 保证线程安全
//        hubMemos.compute(ipAndPort, (k, v) ->
//                new HubMemo(ipAndPort, pathMapping, now)
//        );
    }

    @Override
    public List<String> getUrlsOf(final String handler) {
        final var handler2url = this.handler2urlRef.get();
        log.info("getUrlsOf: handler2url: {}", handler2url);
        final var result = handler2url.get(handler);
        log.info("getUrlsOf: handler[{}] => result: {}", handler, result);
        return result != null ? result : List.of();
    }

    @PostConstruct
    private void updateHubsAndScheduleNext() {
        final long now = System.currentTimeMillis();
        try {
            // 步骤1: 清理过期Hub
//            hubMemos.values().removeIf(memo ->
//                    (now - memo.updateTimestamp()) >= HUB_UPDATE_TIMEOUT_IN_MS );
            for (HubMemo memo : hubMemos.values()) {
                if (now - memo.updateTimestamp() >= HUB_UPDATE_TIMEOUT_IN_MS) {
                    if (hubMemos.remove(memo.ipAndPort()) != null) {
                        log.warn("updateHubs: remove_update_timeout hub: {}", memo);
                    }
                }
            }

            final String[] disabled = disableIPs.get();
            // 步骤2: 构建新的处理器映射表 (线程安全)
            final Map<String, List<String>> newHandlerMap = new HashMap<>();

            hubMemos.forEach((ipPort, memo) -> {
                if (disabled != null
                    && Arrays.stream(disabled).anyMatch(ip -> ipPort.startsWith(ip + ":"))) {
                    // match disabled ip, so skip
                    return;
                }
                final String baseUrl = "ws://" + memo.ipAndPort();
                memo.pathMapping().forEach((path, handler) -> {
                    newHandlerMap.computeIfAbsent(handler, k -> new ArrayList<>()).add(baseUrl + path);
                });
            });

            handler2urlRef.set(newHandlerMap);
        } finally {
            schedulerProvider.getObject().schedule(this::updateHubsAndScheduleNext, _hub_check_interval, TimeUnit.MILLISECONDS);
        }
    }

    @Value("${hub.check_interval:10000}") // default: 10000ms
    private long _hub_check_interval;

    private final ConcurrentMap<String, HubMemo> hubMemos = new ConcurrentHashMap<>();
    // 新增线程安全的映射表：处理器名称 -> WebSocket地址列表
    private final AtomicReference<Map<String, List<String>>> handler2urlRef = new AtomicReference<>(Map.of());

    private final ObjectProvider<ScheduledExecutorService> schedulerProvider;
    private final AtomicReference<String[]> disableIPs = new AtomicReference<>(null);
}
