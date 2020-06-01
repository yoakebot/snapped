package pers.snapped.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.snapped.dao.UsersMapper;
import pers.snapped.exception.GlobalException;
import pers.snapped.model.Users;
import pers.snapped.redis.RedisService;
import pers.snapped.redis.UserKey;
import pers.snapped.result.MsgCode;
import pers.snapped.service.UsersService;
import pers.snapped.util.Md5Util;
import pers.snapped.util.UUIDUtil;
import pers.snapped.vo.LoginUser;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
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
@Service("usersService")
@Transactional
public class UsersServiceImpl implements UsersService {

    @Resource
    private UsersMapper userMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 验证用户
     * @param response //
     * @param loginUser 用户信息
     * @return
     */
    @Override
    public String verifyUser(HttpServletResponse response, LoginUser loginUser) {
        String mobile = loginUser.getMobile();
        String password = loginUser.getPassword();
        if (StringUtils.isEmpty(mobile)) {
            //服务器异常
            throw new GlobalException(MsgCode.serverError.getMsg(), MsgCode.serverError);
        }
        Users user = userMapper.get(Long.parseLong(mobile));
        if (user == null) {
            //手机号不存在
            throw new GlobalException(MsgCode.mobileError.getMsg(), MsgCode.mobileError);
        }
        String salt = user.getSalt();
        String encodePassword = Md5Util.secEncode(password, salt);
        if (!encodePassword.equals(user.getPassword())) {
            //密码错误
            throw new GlobalException(MsgCode.passwordError.getMsg(), MsgCode.passwordError);
        }
//        user = redisService.get(UserKey.getExpire, user.getSalt(), Users.class);
        user.setLastLoginDate(new Date());
        user.setLoginCount(user.getLoginCount() + 1);
        //TODO saveDB
        String token = UUIDUtil.getUUID();
        addCookie(response, user, token);
        return token;
    }

    @Override
    public Users getUserByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        Users users = redisService.get(UserKey.getExpire, token, Users.class);
        if (users != null)
            addCookie(response, users, token);
        return users;
    }

    private void addCookie(HttpServletResponse response, Users user, String token) {
        redisService.set(UserKey.getExpire, token, user);
        Cookie cookie = new Cookie("login_token", token);
        cookie.setMaxAge(UserKey.getExpire.expireSecond());
        response.addCookie(cookie);
    }
}
