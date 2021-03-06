package com.lmfirst.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lmfirst.pojo.Users;
import com.lmfirst.pojo.bo.UserBO;
import com.lmfirst.service.UserService;
import com.lmfirst.utils.CookieUtils;
import com.lmfirst.utils.JSONResult;
import com.lmfirst.utils.JsonUtils;
import com.lmfirst.utils.MD5Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * PassportController
 *
 * @author leiming
 * @date 2019/12/17
 */
@Api(
    value = "注册登录",
    tags  = { "用于注册登录的相关接口" }
)
@RestController
@RequestMapping("passport")
public class PassportController {
    @Autowired
    private UserService userService;

    @ApiOperation(
        value      = "用户登录",
        notes      = "用户登录",
        httpMethod = "POST"
    )
    @PostMapping("/login")
    public JSONResult login(@RequestBody UserBO userBO, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return JSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1. 实现登录
        Users userResult = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));

        if (userResult == null) {
            return JSONResult.errorMsg("用户名或密码不正确");
        }

        userResult = setNullProperty(userResult);
        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(userResult), true);

        // TODO: 生成用户token，存入redis会话
        // TODO: 同步购物车数据

        return JSONResult.ok(userResult);
    }

    @ApiOperation(
        value      = "用户退出登录",
        notes      = "用户退出登录",
        httpMethod = "POST"
    )
    @PostMapping("/logout")
    public JSONResult logout(@RequestParam String userId, HttpServletRequest request, HttpServletResponse response) {

        // 清除用户的相关信息cookie
        CookieUtils.deleteCookie(request, response, "user");

        // TODO: 用户退出登录，需清空购物车
        // TODO: 分布式会话中需要清除用户数据

        return JSONResult.ok();
    }

    @ApiOperation(
        value      = "用户注册",
        notes      = "用户注册",
        httpMethod = "POST"
    )
    @PostMapping("/regist")
    public JSONResult register(@RequestBody UserBO userBO, HttpServletRequest request, HttpServletResponse response) {
        String username        = userBO.getUsername();
        String password        = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();

        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
            return JSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1. 查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);

        if (isExist) {
            return JSONResult.errorMsg("用户名已经存在");
        }

        // 2. 密码长度不能少于6位
        if (password.length() < 6) {
            return JSONResult.errorMsg("密码长度不能少于6位");
        }

        // 3. 判断两次密码是否一致
        if (!password.equals(confirmPassword)) {
            return JSONResult.errorMsg("两次输入密码不一致");
        }

        // 4. 实现注册
        Users userResult = userService.createUser(userBO);

        userResult = setNullProperty(userResult);
        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(userResult), true);

        // TODO: 生成用户token，存入redis会话
        // TODO: 同步购物车数据

        return JSONResult.ok(userResult);
    }

    @ApiOperation(
        value      = "用户名是否存在",
        notes      = "用户名是否存在",
        httpMethod = "GET"
    )
    @GetMapping("/usernameIsExist")
    public JSONResult usernameIsExist(@RequestParam String username) {

        // 1. 判断用户名不能为空
        if (StringUtils.isBlank(username)) {
            return JSONResult.errorMsg("用户名不能为空");
        }

        // 2. 查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);

        if (isExist) {
            return JSONResult.errorMsg("用户名已经存在");
        }

        // 3. 请求成功，用户名没有重复
        return JSONResult.ok();
    }

    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);

        return userResult;
    }
}
