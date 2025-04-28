package com.yulore.medhub.controller;

import lombok.Builder;
import lombok.ToString;

@ToString
@Builder
public class StatusResponse {
    public String[] disableIps;
}
