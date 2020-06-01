package pers.snapped.intercepted;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import pers.snapped.model.Users;
import pers.snapped.service.UsersService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UsersService usersService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> aClass = methodParameter.getParameterType();
        return aClass == Users.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        HttpServletResponse response = (HttpServletResponse) nativeWebRequest.getNativeResponse();
        String paramToken = request.getParameter("login_token");
        String cookieToken = getCookieToken(request);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return usersService.getUserByToken(response, token);
    }

    private String getCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("login_token")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
