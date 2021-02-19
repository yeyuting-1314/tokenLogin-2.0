package select.system.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import select.base.Constants;
import select.base.Result;
import select.constants.BaseEnums;
import select.constants.TransactionType;
import select.system.dao.UserMapper;
import select.system.dto.User;
import select.system.service.UserService;
import select.util.JedisUtil;
import select.util.PageBean;
import select.util.Results;
import select.util.TokenUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author yeyuting
 * @create 2021/1/25
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper ;

    @Autowired
    TokenUtil tokenUtil ;

    @Autowired
    JedisUtil jedisUtil ;

    @Autowired
    RedisTemplate redisTemplate ;

    public User selectByName(String username) {
      return  userMapper.selectByName(username) ;
    }

    public User selectById(int id){
        return userMapper.selectById(id) ;
    }

    public List<User> selectAll(){
        return userMapper.selectAll() ;
    }

    public boolean insertOne(User user) {
        return userMapper.insertOne(user) ;
    }

    public boolean insertMany(List<User> userList) {
        return userMapper.insertMany(userList) ;
    }
    public boolean updateOne(User user){
        return userMapper.updateOne(user) ;
    }

    public boolean deleteById(int id){
        return userMapper.deleteById(id) ;
    }

    public List<User> SelectByStartIndexAndPageSize(int startIndex , int pageSize) {
        return userMapper.SelectByStartIndexAndPageSize(startIndex,pageSize) ;
    }

    public List<User> selectByMap(Map<String ,Object> map){
        return userMapper.selectByMap(map) ;
    }

    public List<User> SelectByPageBean(PageBean pageBean) {
        return userMapper.SelectByPageBean(pageBean) ;
    }

    public List<User> selectByLike(Map<String , Object> map){
        return userMapper.selectByLike(map) ;
    }

    public Result loginCheck(User user , HttpServletResponse response){
        User user1 = userMapper.selectByName(user.getUserName()) ;
        if(user1 == null ){
            return Results.failure("用户不存在") ;
        }else if(!user1.getPassword().equals(user.getPassword())){
            return Results.failure("密码输入错误！") ;
        }
        ValueOperations valueOperations = redisTemplate.opsForValue() ;
        User newUser = (User)valueOperations.get(user1.getUserName()) ;

        /*Jedis jedis = jedisUtil.getSource() ;
        String jedisKey = jedis.get(user1.getUserName()) ;*/
        if(newUser != null){
            jedisUtil.delString(user1.getUserName());
        }
        String token = tokenUtil.getToken(user1) ;
        System.out.println("token:" + token);
        user1.setToken(token);
        valueOperations.set(user1.getUserName() , user1);

        //jedisUtil.tokenToJedis(user1);
        return Results.successWithData(user1);
    }

    //查询金额
    public User selectByUserName(String username) {
        return userMapper.selectByUserName(username) ;
    }

    //查询余额
    public Double selectByUserName1(String username , HttpServletRequest request) {
        String token = request.getHeader("token") ;
        DecodedJWT jwt = tokenUtil.deToken(token) ;
        Double account = jwt.getClaim("account").asDouble() ;
        return account ;
    }
    //转账
    public String transferAccount(Double accountMoney , String targetAccount , HttpServletRequest request){

        String token = request.getHeader("token") ;
        DecodedJWT jwt = tokenUtil.deToken(token) ;
        Double account = jwt.getClaim("account").asDouble() ;
        String userName = jwt.getClaim("username").asString() ;

        if(accountMoney > account){
            return "余额不足" ;
        }
        User user1 = userMapper.selectByName(targetAccount) ;
        if (user1.equals(null)){
            return "对方账户不存在" ;
        }
        //转出账户余额更新
        boolean result = userMapper.updateAccountOut(accountMoney , userName) ;
        //转入账户余额更新
        boolean result1 = userMapper.updateAccountIn(accountMoney , user1.getUserName()) ;
        if ((result == false)||(result1 == false) ){
            return "转账操作失败" ;
        }
        //转账记录生成------------
        //String accountType = TransactionType.WITHDRAWMONEY ;
        //出账记录生成
        boolean insertReult = userMapper.accountOutInsert(userName ,account ,  accountMoney , targetAccount , TransactionType.WITHDRAWMONEY ) ;
        //入账记录生成
        //String accountType1 = TransactionType.SAVEMONEY ;
        boolean insertReult1 = userMapper.accountInInsert(user1.getUserName() , user1.getAccount() , accountMoney , userName , TransactionType.SAVEMONEY ) ;

        if((insertReult == false) || (insertReult1 == false)){
            return "转账记录生成失败" ;
        }

        return "转账成功！" ;
    }

    //存钱
    public String saveMoney(Double accountMoney , HttpServletRequest request){
        String token = request.getHeader("token") ;
        DecodedJWT jwt = tokenUtil.deToken(token) ;
        Double account = jwt.getClaim("account").asDouble() ;
        String userName = jwt.getClaim("username").asString() ;
        //存入余额更新
        boolean result = userMapper.updateAccountIn(accountMoney , userName) ;
        if(result = false){
            return "存入失败" ;
        }
        //存入记录生成
        boolean insertResult = userMapper.accountInInsert(userName ,account , accountMoney , userName , TransactionType.SAVEMONEY) ;
        if((insertResult == false)){
            return "入账记录生成失败" ;
        }
        return "成功存入" + accountMoney + "元！"  ;
    }

    //取钱
    public String withdrawMoney(Double accountMoney , HttpServletRequest request){
        String token = request.getHeader("token") ;
        DecodedJWT jwt = tokenUtil.deToken(token) ;
        Double account = jwt.getClaim("account").asDouble() ;
        String userName = jwt.getClaim("username").asString() ;

        if(accountMoney > account){
            return "余额不足" ;
        }
        boolean result = userMapper.updateAccountOut(accountMoney , userName) ;
        if(result = false){
            return "取钱失败" ;
        }
        //出账记录生成
        boolean insertResult = userMapper.accountOutInsert(userName ,account, accountMoney , userName , TransactionType.WITHDRAWMONEY) ;
        if((insertResult == false)){
            return "出账记录生成失败" ;
        }
        return "成功取出" + accountMoney + "元！"  ;
    }


}
