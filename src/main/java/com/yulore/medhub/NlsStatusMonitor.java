package com.yulore.medhub;

import com.yulore.metric.DisposableGauge;
import com.yulore.metric.MetricCustomized;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.redisson.misc.Tuple;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


@Slf4j
@RequiredArgsConstructor
@Component
public class NlsStatusMonitor {
    @Value("${nls.asr-prefix}")
    private String _asr_prefix;

    @Value("#{${nls.asr}}")
    private Map<String,String> _all_asr;

    private List<Object> keys;
    // 加载 Lua 脚本
    private final static String luaScript = """
            local total = 0
            for _, key in ipairs(KEYS) do
                local val = redis.call('GET', key)
                total = total + (val and tonumber(val) or 0)
            end
            return total""";

    @PostConstruct
    public void init() {
        this.script = redisson.getScript(LongCodec.INSTANCE);
        // 生成完整的 Redis 键列表
        keys = _all_asr.keySet().stream()
                .<Object>map(name -> _asr_prefix + ":" + name)
                .toList();
        for (String name : _all_asr.keySet()) {
            final var rcc = redisson.getAtomicLong(_asr_prefix + ":" + name);;
            final var cc = new AtomicInteger(0);
            _aliasr_ccs.put(name, new Tuple<>(rcc, cc));
            gaugeProvider.getObject((Supplier<Number>)cc::get, "mm.aliasr.cc", MetricCustomized.builder().tags(List.of("name", name)).build());
        }

        gaugeProvider.getObject((Supplier<Number>)_aliasr_all_cc::get, "mm.aliasr.cc", MetricCustomized.builder().tags(List.of("name", "all")).build());
    }

    @Scheduled(fixedDelay = 1_000)  // 每1秒推送一次
    void asrStatus() {
        try {
            /*final Long total =*/ script.<Long>evalAsync(RScript.Mode.READ_ONLY,
                    luaScript,
                    RScript.ReturnType.INTEGER,
                    keys).whenComplete((total, ex)->{
                _aliasr_all_cc.set(total.intValue());
                log.info("status => {}: {}", _asr_prefix, total);
            });
            for (var entry : _aliasr_ccs.entrySet()) {
                final var name = entry.getKey();
                final var rcc = entry.getValue().getT1();
                final var cc = entry.getValue().getT2();
                rcc.getAsync().whenComplete((value, ex) -> {
                    cc.set(value.intValue());
                    log.info("status => {} : {}", name, value);
                });
            }
        } catch (Exception ex) {
            log.warn("asrStatus with exception", ex);
        }
    }


    private RScript script;

    private final AtomicInteger _aliasr_all_cc = new AtomicInteger(0);
    private final Map<String, Tuple<RAtomicLong,AtomicInteger>> _aliasr_ccs = new HashMap<>();

    private final RedissonClient redisson;
    private final ObjectProvider<DisposableGauge> gaugeProvider;
}
