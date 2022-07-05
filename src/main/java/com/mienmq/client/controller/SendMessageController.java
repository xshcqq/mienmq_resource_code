package com.mienmq.client.controller;

import com.mienmq.client.client.service.SendMessageService;
import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.client.service.base.MessageRequestBody;
import com.mienmq.client.client.service.base.PushMessage;
import com.mienmq.client.client.service.base.ResultWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mienmq")
public class SendMessageController {

    private final static Logger logger = LoggerFactory.getLogger(SendMessageController.class);

    @Autowired
    private SendMessageService sendMessageService;

    /**
     * mq发送消息
     * @param request
     * @return
     */
    @RequestMapping("/send")
    @ResponseBody
    public ResultWrapper send(@RequestBody PushMessage request) {
        return sendMessageService.send(request);
    }

    /**
     * mq发送消息
     * @param request
     * @return
     */
    @RequestMapping("/sendasync")
    @ResponseBody
    public ResultWrapper sendAsync(@RequestBody PushMessage request) {
        return sendMessageService.sendOneWay(request);
    }


    /**
     * 测试listener_2
     * @param request
     * @return
     */
    @RequestMapping("/send/listen/two")
    @ResponseBody
    public ResultWrapper sendlisten_2(@RequestBody PushMessage request) {
        return sendMessageService.sendListener_2(request);
    }

}
