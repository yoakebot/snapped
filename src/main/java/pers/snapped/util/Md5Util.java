package pers.snapped.util;

import org.apache.commons.codec.digest.DigestUtils;

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
public class Md5Util {

    public static String MD5_SALT = "././fu12c@$^442z///][";

    public static String md5Encode(String str) {
        return DigestUtils.md5Hex(str);
    }

    public static String firstEncode(String password) {
        String md5Encode = md5Encode(password + MD5_SALT);
        System.out.println("password---" + password + MD5_SALT);
        System.out.println("firstEncode---" + md5Encode);
        return md5Encode;
    }

    public static String secEncode(String password, String salt) {
        return md5Encode(password + salt);
    }

    public static String toDbEncode(String password, String salt) {
        String firstEncode = firstEncode(password);
        String secEncode = secEncode(firstEncode, salt);
        System.out.println("toDbEncode---" + secEncode);
        return secEncode;
    }

    public static void main(String[] args) {
        String salt = UUIDUtil.getUUID();
        System.out.println("salt---" + salt);
        System.out.println(toDbEncode("123456", salt));
    }
}
