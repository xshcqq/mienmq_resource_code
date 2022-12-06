package com.mienmq.client.controller;

import com.mienmq.client.client.service.SendMessageService;
import com.mienmq.client.client.service.base.*;
import com.mienmq.client.util.ProtostuffUtil;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
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
     *
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
     *
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
     *
     * @param request
     * @return
     */
    @RequestMapping("/send/listen/two")
    @ResponseBody
    public ResultWrapper sendlisten_2(@RequestBody PushMessage request) {
        return sendMessageService.sendListener_2(request);
    }


    /**
     * mq发送消息
     *
     * @param request
     * @return
     */
    @RequestMapping("/sendSuccess")
    @ResponseBody
    public String send() {
        logger.info("《《《《《《《《《《《  全网异步通知已进来  》》》》》》》》》");
        PushMessage pushMessage = new PushMessage();
        pushMessage.setQueueName("NOTICE_TEST");
        pushMessage.setContent("成功接受异步通知！");
        this.sendAsync(pushMessage);
        return "success";
    }

    /**
     * 统计消息
     *
     * @param queueName
     * @return
     */
    @RequestMapping("/getMessagesCount")
    @ResponseBody
    public CountSyncMessage getMessagesCount(@Nullable String queueName) {
        CountSyncReqMessage reqMessage = new CountSyncReqMessage();
        if (Strings.isNotBlank(queueName)) reqMessage.setQueueName("NOTICE_TEST");
        reqMessage.setThreadId(Thread.currentThread().getName());
        ResultWrapper messagesCount = sendMessageService.getMessagesCount(reqMessage);
        Message result = (Message) messagesCount.getResult();
        CountSyncMessage requestSyncMessage = null;
        if (result != null) {
            requestSyncMessage = ProtostuffUtil.deserializer(result.getContent(), CountSyncMessage.class);
        }

        return requestSyncMessage;
    }
}
