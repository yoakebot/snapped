package pers.snapped.rabbitmq;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pers.snapped.model.SnappedOrder;
import pers.snapped.model.Users;
import pers.snapped.redis.GoodsKey;
import pers.snapped.redis.RedisService;
import pers.snapped.service.GoodsService;
import pers.snapped.service.OrderService;
import pers.snapped.service.SnappedService;
import pers.snapped.vo.GoodsVO;
import pers.snapped.vo.OrderVO;

/**
 * ▓██   ██▓ ▒█████   ▄▄▄       ██ ▄█▀▓█████
 * ▒██  ██▒▒██▒  ██▒▒████▄     ██▄█▒ ▓█   ▀
 * ▒██ ██░▒██░  ██▒▒██  ▀█▄  ▓███▄░ ▒███
 * ░ ▐██▓░▒██   ██░░██▄▄▄▄██ ▓██ █▄ ▒▓█  ▄
 * ░ ██▒▓░░ ████▓▒░ ▓█   ▓██▒▒██▒ █▄░▒████▒
 * ██▒▒▒ ░ ▒░▒░▒░  ▒▒   ▓▒█░▒ ▒▒ ▓▒░░ ▒░ ░
 * ▓██ ░▒░   ░ ▒ ▒░   ▒   ▒▒ ░░ ░▒ ▒░ ░ ░  ░
 * ▒ ▒ ░░  ░ ░ ░ ▒    ░   ▒   ░ ░░ ░    ░
 * ░ ░         ░ ░        ░  ░░  ░      ░  ░
 * ░ ░
 */
@Component
public class MQReceiver {
    private static final Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SnappedService snappedService;
    @Autowired
    private RedisService redisService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order_queue", durable = "true"),
            exchange = @Exchange(name = "order.exchange", ignoreDeclarationExceptions = "true", type = "topic"),
            key = "order.routing"
    ))
    @RabbitHandler
    public void receive(@Payload SnappedMessage snappedMessage, @Headers Map<String, Object> headers, Channel channel) throws IOException {
//        log.info("receive:" + msg);
        Long goodsId = snappedMessage.getGoodsId();
        Users users = snappedMessage.getUsers();

        GoodsVO goods = goodsService.getGoodsById(goodsId);
        if (goods.getStockCount() <= 0) {
            //库存不足
            return;
        }
        SnappedOrder snappedOrder = orderService.getOrderByUserIdAndGoodId(users.getId(), goodsId);
        if (snappedOrder != null) {
            //重复秒杀
            return;
        }
        OrderVO orderInfo = null;
        try {
            orderInfo = snappedService.snapped(users, goods);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (orderInfo == null) {
            return;
        }
        redisService.set(GoodsKey.getOrderDetail, users.getId() + "" + goodsId, orderInfo);
        log.info(users.getId() + "秒杀了" + orderInfo.getId());
        goods.setOrderInfo(orderInfo);
        channel.basicAck((Long) headers.get(AmqpHeaders.DELIVERY_TAG), false);
    }
}
