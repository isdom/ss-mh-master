package com.yulore.medhub.controller;

import com.yulore.medhub.LocalMasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping("/medhub")
public class ApiController {
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<String> disable(@RequestBody final DisableHubsRequest request) {
        masterService.disableHubs(request.ips);
        return ApiResponse.<String>builder().code("0000").data("ok").build();
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse<String> status() {
        return ApiResponse.<String>builder().code("0000").data(masterService.status()).build();
    }

    @Autowired
    private LocalMasterService masterService;
}
