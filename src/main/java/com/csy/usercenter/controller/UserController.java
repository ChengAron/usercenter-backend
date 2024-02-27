package com.csy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.csy.usercenter.common.BaseResponse;
import com.csy.usercenter.common.ErrorCode;
import com.csy.usercenter.common.ResultUtils;
import com.csy.usercenter.constans.UserConstans;
import com.csy.usercenter.exception.BusinessException;
import com.csy.usercenter.mapper.UserMapper;
import com.csy.usercenter.model.domain.User;
import com.csy.usercenter.model.domain.request.UserLoginRequest;
import com.csy.usercenter.model.domain.request.UserRegisterRequest;
import com.csy.usercenter.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  用户接口
 *
 * @author csy
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest == null) {
            return ResultUtils.error(ErrorCode.NULL_ERROR);
        }
        long result = userService.userRegister(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword(),userRegisterRequest.getPlanetCode());
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        if(userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.NULL_ERROR);
        }
        User result = userService.doLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword(), httpServletRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null) {
            return ResultUtils.error(ErrorCode.NO_LOGIN);
        }
        int result = userService.userLogOut(httpServletRequest);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object object = request.getSession().getAttribute(UserConstans.USER_LOGIN_STATE);
        User user = (User) object;
        if(user == null) {
            return ResultUtils.error(ErrorCode.NO_LOGIN);
        }
        Long userId = user.getId();
        User currentUser = userService.getById(userId);
        User result = userService.getSafetyUser(currentUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if(!isAdmin(request)) {
            return ResultUtils.error(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)) {
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id, HttpServletRequest request) {
        if(!isAdmin(request)) {
            return ResultUtils.error(ErrorCode.NO_AUTH);
        }
        if(id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户id过小");
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }


    /**
     *  是否为管理员
     * @param request
     * @return
     */
    private Boolean isAdmin(HttpServletRequest request) {
        Object object = request.getSession().getAttribute(UserConstans.USER_LOGIN_STATE);
        User user = (User) object;
        if(user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return user.getUserRole() == UserConstans.USER_ROLE_ADMIN;
    }
}
