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
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }

    public static <T> Result<T> msg(MsgCode msgCode) {
        return new Result<T>(msgCode);
    }

    private Result(T data) {
        this.data = data;
    }

    private Result(MsgCode msgCode) {
        if (msgCode != null) {
            this.code = msgCode.getCode();
            this.msg = msgCode.getMsg();
        }
    }


    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
