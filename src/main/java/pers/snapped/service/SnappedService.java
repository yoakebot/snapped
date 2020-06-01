package pers.snapped.service;

import pers.snapped.model.Users;
import pers.snapped.vo.GoodsVO;
import pers.snapped.vo.OrderVO;

import java.awt.image.BufferedImage;

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
public interface SnappedService {

    /**
     * 执行秒杀
     *
     * @param users
     * @param goods
     * @return
     */
    OrderVO snapped(Users users, GoodsVO goods);

    /**
     * 获取秒杀结果
     * @param id
     * @param goodsId
     * @return
     */
    long getResult(Long id, String goodsId);

    /**
     * 创建秒杀地址 并缓存
     *
     * @param users
     * @param goodsId
     * @return
     */
    String createSite(Users users, long goodsId);

    /**
     * 从缓存中获取核查秒杀地址
     *
     * @param users
     * @param site
     * @param goodsId
     * @return
     */
    boolean verifySite(Users users, String site, long goodsId);

    /**
     * 创建图片验证码,并缓存正确结果
     *
     * @param users
     * @param goodsId
     * @return
     */
    BufferedImage createCodeImage(Users users, String goodsId);
}
