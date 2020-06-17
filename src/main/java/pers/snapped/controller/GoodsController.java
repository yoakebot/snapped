package pers.snapped.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import pers.snapped.model.SnappedOrder;
import pers.snapped.model.Users;
import pers.snapped.rabbitmq.MQSender;
import pers.snapped.rabbitmq.SnappedMessage;
import pers.snapped.redis.GoodsKey;
import pers.snapped.redis.RedisService;
import pers.snapped.result.MsgCode;
import pers.snapped.result.Result;
import pers.snapped.service.GoodsService;
import pers.snapped.service.OrderService;
import pers.snapped.service.SnappedService;
import pers.snapped.vo.GoodsVO;
import pers.snapped.vo.OrderVO;
import pers.snapped.vo.SnappedOrderVO;

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
@Controller
public class GoodsController implements InitializingBean {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SnappedService snappedService;
    @Autowired
    private MQSender mqSender;
    private Map<Long, Boolean> isOver = new HashMap<Long, Boolean>();


//    @RequestMapping("mq")
//    @ResponseBody
//    public Result<Boolean> test() {
//        mqSender.sender("123");
//        return Result.success(true);
//    }

    /**
     * 秒杀商品详情
     * 缓存商品库存和标识符
     *
     * @param users
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "toDetail")
    @ResponseBody
    public Result<SnappedOrderVO> toDetail(Users users, @RequestParam String goodsId) {
        if (users == null) {
            return Result.msg(MsgCode.sessionError);
        }
        long newGoodsId = Long.parseLong(goodsId);
        GoodsVO goods = goodsService.getGoodsById(newGoodsId);
        long startTime = goods.getStartDate().getTime();
        long endTime = goods.getEndDate().getTime();
        long nowTime = System.currentTimeMillis();
        long remainSeconds;
        int status;
        if (nowTime < startTime) {
            //未开始
            remainSeconds = (startTime - nowTime) / 1000;
            status = 1;
        } else if (nowTime > startTime && nowTime <= endTime) {
            //进行中
            remainSeconds = 0;
            status = 0;
        } else {
            remainSeconds = -1;
            status = -1;
        }
        SnappedOrderVO snappedOrderVO = new SnappedOrderVO();
        snappedOrderVO.setGoodsVO(goods);
        snappedOrderVO.setGoodsId(newGoodsId);
        snappedOrderVO.setRemainSeconds(remainSeconds);
        snappedOrderVO.setStatus(status);
        snappedOrderVO.setUsers(users);
        redisService.set(GoodsKey.getStock, "" + goodsId, goods.getStockCount() * 2);
        isOver.put(goods.getId(), false);
        return Result.success(snappedOrderVO);
    }

    /*
    5000线程循环10次
    1600qps

    1900qps

    2750qps
     */

    /**
     * 秒杀请求
     * 核查库存标识符(无库存拒绝所有请求)-->核查秒杀地址-->核查是否是重复秒杀请求-->核查库存数并修改库存标识符-->入队
     *
     * @param user
     * @param goodsId
     * @param siteCode 验证码
     * @return
     */
    @RequestMapping(value = "snapped", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> snapped(Users user, @RequestParam("goodsId") String goodsId, @RequestParam("siteCode") String siteCode) {
        if (user == null) {
            return Result.msg(MsgCode.sessionError);
        }
        long newGoodsId = Long.parseLong(goodsId);
        Boolean flag = isOver.get(newGoodsId);
        if (flag) {
            return Result.msg(MsgCode.StockLess);
        }
        boolean site = snappedService.verifySite(user, siteCode, Long.parseLong(goodsId));
        if (!site) {
            return Result.msg(MsgCode.IllegalRequest);
        }
        SnappedOrder snappedOrder = orderService.getOrderByUserIdAndGoodId(user.getId(), newGoodsId);
        if (snappedOrder != null) {
            //重复秒杀
            return Result.msg(MsgCode.RepeatSnapped);
        }

        long stock = redisService.decr(GoodsKey.getStock, "" + newGoodsId);
        if (stock < 0) {
            //库存不足
            isOver.put(newGoodsId, true);
            return Result.msg(MsgCode.StockLess);
        }
        //队列
        SnappedMessage message = new SnappedMessage();
        message.setGoodsId(newGoodsId);
        message.setUsers(user);
        mqSender.sender(message);
        return Result.success(0);
    }


    /**
     * mq队列,获取订单结果
     *
     * @param users
     * @param goodsId
     * @return 0:排队,-1没了,其他则成功
     */
    @RequestMapping(value = "getOrderResult")
    @ResponseBody
    public Result<Long> getOrderResult(Users users, String goodsId) {
        if (users == null) {
            return Result.msg(MsgCode.sessionError);
        }
        long result = snappedService.getResult(users.getId(), goodsId);
        return Result.success(result);
    }

    /**
     * thymeleaf 页面缓存 测试qps
     *
     * @param request
     * @param response
     * @param model
     * @param users
     */
    @RequestMapping(value = "toList", produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, Users users) {
        model.addAttribute("user", users);

        String html = redisService.get(GoodsKey.goodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        List<GoodsVO> list = goodsService.listSnappedOrder();
        model.addAttribute("goods", list);

        IWebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.goodsList, "", html);
        }
        return html;
    }

    /**
     * 订单详情
     *
     * @param users
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "orderDetail")
    @ResponseBody
    public Result<OrderVO> orderDetail(Users users, String goodsId) {
        if (users == null) {
            return Result.msg(MsgCode.sessionError);
        }
        OrderVO orderInfo = redisService.get(GoodsKey.getOrderDetail, users.getId() + "" + goodsId, OrderVO.class);
        return Result.success(orderInfo);
    }

    /**
     * 项目启动提前缓存商品库存和标识符
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVO> list = goodsService.listSnappedOrder();
        if (list != null) {
            for (GoodsVO goodsVO : list) {
                redisService.set(GoodsKey.getStock, "" + goodsVO.getId(), goodsVO.getStockCount() * 2);
                isOver.put(goodsVO.getId(), false);
            }
        }
    }

    /**
     * 先核查验证码结果,再暴露秒杀地址
     *
     * @param request
     * @param users
     * @param goodsId
     * @param verifyResult 验证码结果
     * @return
     */
    @RequestMapping(value = "getSiteCode")
    @ResponseBody
    public Result<String> getSiteCode(HttpServletRequest request, Users users, @RequestParam("goodsId") String goodsId, @RequestParam("verifyResult") Integer verifyResult) {
        if (users == null) {
            return Result.msg(MsgCode.sessionError);
        }
        String uri = request.getRequestURI();
        GoodsKey imageCodeByTime = GoodsKey.getImageCodeByTime(60);
        Integer count = redisService.get(imageCodeByTime, users.getId() + uri, Integer.class);
        if (count == null) {
            redisService.set(imageCodeByTime, users.getId() + uri, 5);
        } else {
            redisService.decr(imageCodeByTime, users.getId() + uri);
            if (count < 0) {
                return Result.msg(MsgCode.getTooMany);
            }
        }
        if (verifyResult == null) {
            return Result.msg(MsgCode.ImageError);
        }
        Integer o = redisService.get(GoodsKey.ImageCodeResult, users.getId() + "" + goodsId, Integer.class);
        if (!verifyResult.equals(o)) {
            return Result.msg(MsgCode.ImageError);
        }
        String site = snappedService.createSite(users, Long.parseLong(goodsId));
        return Result.success(site);
    }

    /**
     * 创建图片验证码
     *
     * @param response
     * @param users
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "createCodeImage")
    @ResponseBody
    public Result<String> createCodeImage(HttpServletResponse response, Users users, @RequestParam("goodsId") String goodsId) {
        if (users == null) {
            return Result.msg(MsgCode.sessionError);
        }
        try {
            BufferedImage image = snappedService.createCodeImage(users, goodsId);
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "JPEG", outputStream);
            outputStream.flush();
            outputStream.close();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return Result.msg(MsgCode.serverError);
        }

    }
}