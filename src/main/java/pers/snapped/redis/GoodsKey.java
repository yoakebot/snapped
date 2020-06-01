package pers.snapped.redis;

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
public class GoodsKey extends BasePrefix {
    public GoodsKey(String prefix) {
        super(prefix);
    }

    public GoodsKey(int expireSecond, String prefix) {
        super(expireSecond, prefix);
    }

    public static GoodsKey goodsList = new GoodsKey(60, "goodsList");
    public static GoodsKey getGoodsById = new GoodsKey("goods");
    public static GoodsKey getSnappedOrder = new GoodsKey("snappedOrder");
    public static GoodsKey getOrderDetail = new GoodsKey("orderDetail");
    public static GoodsKey getStock = new GoodsKey(60, "stock");
    public static GoodsKey isOver = new GoodsKey("isOver");
    public static GoodsKey siteCode = new GoodsKey(60, "isOver");
    public static GoodsKey ImageCodeResult = new GoodsKey(60, "ImageCodeResult");

    public static GoodsKey getImageCodeByTime(int second) {
        return new GoodsKey(second, "getImageCodeByTime");
    }
}
