package com.yulore.medhub;

import com.yulore.api.MasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class MasterServiceImpl implements MasterService {
    private static final long AGENT_UPDATE_TIMEOUT_IN_MS = 1000 * 30; // 30s

    @Override
    public void updateHubStatus(final String ipAndPort, final Map<String, String> pathMapping, final long timestamp) {
        log.info("updateHubStatus: med-hub[{}] - pathMapping: {} - {}", ipAndPort, pathMapping, timestamp);
    }
}
