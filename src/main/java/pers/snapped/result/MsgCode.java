package pers.snapped.result;
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
public class MsgCode {

    private int code;
    private String msg;

    public static MsgCode success = new MsgCode(200, "成功");
    public static MsgCode serverError = new MsgCode(500, "服务器异常");
    public static MsgCode mobileError = new MsgCode(600, "手机号不存在");
    public static MsgCode passwordError = new MsgCode(601, "密码错误");
    public static MsgCode sessionError = new MsgCode(602, "Session异常");
    public static MsgCode BindError = new MsgCode(700, "参数校验异常：%s");
    public static MsgCode StockLess = new MsgCode(800, "抢购人数太多了,请尝试刷新页面");
    public static MsgCode RepeatSnapped = new MsgCode(801, "重复秒杀");
    public static MsgCode IllegalRequest = new MsgCode(802, "非法请求");
    public static MsgCode ImageError = new MsgCode(803, "验证码错误");
    public static MsgCode getTooMany = new MsgCode(804, "请求太频繁");

    public MsgCode fillArgs(Object... args) {
        int code = this.code;
        String msg = String.format(this.msg, args);
        return new MsgCode(code, msg);
    }

    private MsgCode(int code, String msg) {
        this.code = code;
        this.msg = msg;

    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
