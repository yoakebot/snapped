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
public abstract class BasePrefix implements KeyPrefix {

    private int expireSecond;

    private String prefix;

    public BasePrefix(String prefix) {
        this(0, prefix);
    }

    public BasePrefix(int expireSecond, String prefix) {
        this.expireSecond = expireSecond;
        this.prefix = prefix;
    }

    public int expireSecond() {
        return expireSecond;
    }

    public String getPrefix() {
        String name = getClass().getSimpleName();
        return name + ":" + prefix;
    }
}
