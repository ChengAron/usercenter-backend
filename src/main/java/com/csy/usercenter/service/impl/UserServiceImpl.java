package com.csy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csy.usercenter.common.ErrorCode;
import com.csy.usercenter.constans.UserConstans;
import com.csy.usercenter.exception.BusinessException;
import com.csy.usercenter.model.domain.User;
import com.csy.usercenter.mapper.UserMapper;
import com.csy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author 18043
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-01-26 16:42:42
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String STAL = "csy";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //校验
        if(userAccount == null || userPassword == null || checkPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if(planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号过长");
        }
        //账户不能包含特殊字符
        String regEx="[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能包含特殊字符");
        }

        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能重复");
        }

        //编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", userAccount);
        count = userMapper.selectCount(queryWrapper);
        if(count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号不能重复");
        }

        //密码和校验密码相同
        if(!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验密码相同");
        }
        //加密
        String newPassword = DigestUtils.md5DigestAsHex((STAL + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该账号已存在");
        }

        return user.getId();

    }


    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验
        if(userAccount == null || userPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        if(userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        //账户不能包含特殊字符
        String regEx="[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }

        //加密
        String newPassword = DigestUtils.md5DigestAsHex((STAL + userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", newPassword);
        User user = userMapper.selectOne(queryWrapper);
        if(user == null) {
            log.info("用户登录失败");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码不对");
        }

        //用户信息脱敏
        User safetyUser = getSafetyUser(user);

        //记录用户的登录态
        request.getSession().setAttribute(UserConstans.USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    @Override
    public User getSafetyUser(User user) {
        if(user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户数据为空");
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setUserPassword(user.getUserPassword());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setPlanetCode(user.getPlanetCode());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUserRole(user.getUserRole());

        return safetyUser;
    }

    @Override
    public int userLogOut(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(UserConstans.USER_LOGIN_STATE);
        return 1;
    }
}




