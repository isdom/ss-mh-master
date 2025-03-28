package com.yulore.medhub.controller;

import com.yulore.medhub.LocalMasterService;
import com.yulore.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

@Controller
@Slf4j
@RequestMapping("/medhub")
public class ApiController {


    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<String> queryTaskStatus(@RequestBody final DisableHubsRequest request) {
        masterService.disableHubs(request.ips);
        return ApiResponse.<String>builder().code("0000").data("ok").build();
    }

    @Autowired
    private LocalMasterService masterService;
}
