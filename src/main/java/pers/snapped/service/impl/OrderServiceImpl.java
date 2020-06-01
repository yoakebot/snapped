package pers.snapped.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.snapped.dao.OrderMapper;
import pers.snapped.enums.OrderStatus;
import pers.snapped.model.SnappedOrder;
import pers.snapped.model.Users;
import pers.snapped.redis.GoodsKey;
import pers.snapped.redis.RedisService;
import pers.snapped.service.GoodsService;
import pers.snapped.service.OrderService;
import pers.snapped.vo.GoodsVO;
import pers.snapped.vo.OrderVO;

import javax.annotation.Resource;
import java.util.Date;

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
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RedisService redisService;

    @Override
    public SnappedOrder getOrderByUserIdAndGoodId(Long id, long goodsId) {
        return redisService.get(GoodsKey.getSnappedOrder, id + "_" + goodsId, SnappedOrder.class);
    }


    @Transactional
    @Override
    public OrderVO create(Users users, GoodsVO goods) {
        Long goodsId = goods.getId();
        Long id = users.getId();
        OrderVO orderInfo = new OrderVO();
        orderInfo.setUserId(id);
        orderInfo.setGoodsId(goodsId);
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSnappedPrice());
        orderInfo.setOrderStatus(OrderStatus.UNPAID.getStatus());
        orderInfo.setCreateDate(new Date());
        orderMapper.saveOrderInfo(orderInfo);

        SnappedOrder snappedOrder = new SnappedOrder();
        snappedOrder.setGoodsId(goodsId);
        snappedOrder.setUserId(id);
        snappedOrder.setOrderId(orderInfo.getId());
        orderMapper.saveSnappedOrder(snappedOrder);

        redisService.set(GoodsKey.getSnappedOrder, id + "_" + goodsId, snappedOrder);

        orderInfo.setGoodsImg(goods.getGoodsImg());
        return orderInfo;
    }


}
