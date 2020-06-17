package pers.snapped.service.impl;

import java.util.List;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.snapped.dao.GoodsMapper;
import pers.snapped.dao.OrderMapper;
import pers.snapped.redis.GoodsKey;
import pers.snapped.redis.RedisService;
import pers.snapped.service.GoodsService;
import pers.snapped.vo.GoodsVO;

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
@Service("goodsService")
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private OrderMapper orderMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public List<GoodsVO> listSnappedOrder() {
        return goodsMapper.listSnappedOrder();
    }

    @Override
    public GoodsVO getGoodsById(long goodsId) {
        GoodsVO goodsVO = redisService.get(GoodsKey.getGoodsById, "" + goodsId, GoodsVO.class);
        if (goodsVO == null) {
            goodsVO = goodsMapper.getGoodsById(goodsId);
            redisService.set(GoodsKey.getGoodsById, "" + goodsId, goodsVO);
        }
        return goodsVO;
    }

    @Override
    public int subStock(GoodsVO goods) {
        int i = orderMapper.subStock(goods.getId());
        if (i > 0) {
            goodsMapper.subStock(goods.getId());
        }
        redisService.del(GoodsKey.getGoodsById, "" + goods);
        return i;
    }
}
