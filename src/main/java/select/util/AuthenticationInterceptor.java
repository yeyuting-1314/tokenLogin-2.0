package select.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.thymeleaf.util.StringUtils;
import redis.clients.jedis.Jedis;
import select.base.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yeyuting
 * @create 2021/1/26
 */
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    TokenUtil tokenUtil ;

    @Autowired
    RedisTemplate redisTemplate ;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("token") ;
        //Jedis jedis = new Jedis("localhost" , 6379) ;
        if(StringUtils.isEmpty(token)){
            response.sendRedirect("/sys/user/login");
            return false ;
        }
        DecodedJWT jwt = tokenUtil.deToken(token) ;
        String userName = jwt.getClaim("username").asString() ;

        if((userName == null) ||(userName.trim().equals("")) ){
            response.sendRedirect("/sys/user/login");
            return false ;
        }
        System.out.println("token匹配成功！");
        return true ;
    }



}
