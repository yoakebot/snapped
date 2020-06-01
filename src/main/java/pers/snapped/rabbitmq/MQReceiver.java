package pers.snapped.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SnappedService snappedService;
    @Autowired
    private RedisService redisService;

    @RabbitListener(queues = MQConfig.SNAPPED_QUEUE)
    public void receive(String msg) {
//        log.info("receive:" + msg);
        SnappedMessage snappedMessage = RedisService.stringToBean(msg, SnappedMessage.class);
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
        OrderVO orderInfo = snappedService.snapped(users, goods);
        if (orderInfo == null) {
            return;
        }
        redisService.set(GoodsKey.getOrderDetail, users.getId() + "" + goodsId, orderInfo);
        System.out.println(users.getId()+"秒杀了"+orderInfo.getId());
        goods.setOrderInfo(orderInfo);
    }
}
