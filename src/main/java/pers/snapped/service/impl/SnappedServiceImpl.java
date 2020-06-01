package pers.snapped.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.snapped.model.Users;
import pers.snapped.redis.GoodsKey;
import pers.snapped.redis.RedisService;
import pers.snapped.service.GoodsService;
import pers.snapped.service.OrderService;
import pers.snapped.service.SnappedService;
import pers.snapped.util.Md5Util;
import pers.snapped.vo.GoodsVO;
import pers.snapped.vo.OrderVO;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;

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
@Service("snappedService")
public class SnappedServiceImpl implements SnappedService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisService redisService;

    @Transactional
    @Override
    public OrderVO snapped(Users users, GoodsVO goods) {
        //减库存
        int i = goodsService.subStock(goods);
        if (i <= 0) {
            //没了,设置标识符
            redisService.set(GoodsKey.isOver, "" + goods.getId(), true);
            return null;
        }
        return orderService.create(users, goods);
    }

    @Override
    public long getResult(Long id, String goodsId) {
        OrderVO orderInfo = redisService.get(GoodsKey.getOrderDetail, id + "" + goodsId, OrderVO.class);
        if (orderInfo != null) {
            //成功
            return orderInfo.getId();
        }
        boolean exists = redisService.exists(GoodsKey.isOver, "" + goodsId);
        if (exists) {
            //没了
            return -1;
        } else {
            //排队
            return 0;
        }
    }

    @Override
    public String createSite(Users users, long goodsId) {
        if (users == null || goodsId <= 0) {
            return null;
        }
        String str = Md5Util.md5Encode(UUID.randomUUID() + "fuck");
        redisService.set(GoodsKey.siteCode, users.getId() + "" + goodsId, str);
        return str;
    }

    @Override
    public boolean verifySite(Users users, String site, long goodsId) {
        if (users == null || goodsId <= 0 || StringUtils.isEmpty(site)) {
            return false;
        }
        String result = redisService.get(GoodsKey.siteCode, users.getId() + "" + goodsId, String.class);
        return result.equals(site);
    }

    @Override
    public BufferedImage createCodeImage(Users users, String goodsId) {
        int width = 80;
        int height = 32;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        int rnd = calc(verifyCode);
        redisService.set(GoodsKey.ImageCodeResult, users.getId() + "" + goodsId, rnd);
        return image;
    }

    private static char[] ops = new char[]{'+', '-', '*'};

    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private int calc(String verifyCode) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(verifyCode);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
