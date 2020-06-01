package pers.snapped.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.snapped.dao.GoodsMapper;
import pers.snapped.dao.OrderMapper;
import pers.snapped.redis.RedisService;
import pers.snapped.service.GoodsService;
import pers.snapped.vo.GoodsVO;

import javax.annotation.Resource;
import java.util.List;

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

    /*
    TODO 解决缓存造成的脏读
     */
    @Override
    public GoodsVO getGoodsById(long goodsId) {
//        GoodsVO goodsVO = redisService.get(GoodsKey.getGoodsById, "" + goodsId, GoodsVO.class);
//        if (goodsVO == null) {
//            goodsVO = goodsMapper.getGoodsById(goodsId);
//            redisService.set(GoodsKey.getGoodsById, "" + goodsId, goodsVO);
//        }
        GoodsVO goodsVO = goodsMapper.getGoodsById(goodsId);
        return goodsVO;
    }

    @Override
    public int subStock(GoodsVO goods) {
        int i = orderMapper.subStock(goods.getId());
        if (i > 0) {
            goodsMapper.subStock(goods.getId());
        }
//        goods.setGoodsStock(goods.getGoodsStock() - 1);
//        goods.setStockCount(goods.getStockCount() - 1);
//        redisService.set(GoodsKey.getGoodsById, "" + goods.getId(), goods);
        return i;
    }
}
