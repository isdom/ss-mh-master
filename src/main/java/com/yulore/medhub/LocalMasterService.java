package com.yulore.medhub;

import com.yulore.api.MasterService;


public interface LocalMasterService extends MasterService {
    void disableHubs(final String[] ips);
    String status();
}
