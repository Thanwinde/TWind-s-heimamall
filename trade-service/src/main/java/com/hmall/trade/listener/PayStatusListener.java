package com.hmall.trade.listener;

import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private  final IOrderService orderService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue",durable = "true",arguments = @Argument(name = "x-queue-mode",value = "lazy")),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        orderService.markOrderPaySuccess(orderId);
    }

}
