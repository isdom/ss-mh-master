package com.yulore.medhub;

import com.yulore.api.MasterService;


public interface LocalMasterService extends MasterService {
    public void disableHubs(final String[] ips);
}
