package com.yulore.medhub;

import com.yulore.api.MasterService;
import com.yulore.medhub.controller.StatusResponse;


public interface LocalMasterService extends MasterService {
    void disableHubs(final String[] ips);
    StatusResponse status();
}
