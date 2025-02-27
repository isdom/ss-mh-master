package com.yulore.api;

public interface MasterService {
    void updateHubStatus(final String agentId, final int freeWorks, final long timestamp);
}
