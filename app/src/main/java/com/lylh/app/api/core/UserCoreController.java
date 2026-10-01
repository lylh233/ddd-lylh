package com.lylh.app.api.core;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.biz.service.UserService;
import com.lylh.common.controller.annotation.IgnorePermission;
import lombok.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部按姓名查用户。类上跳过登录校验，只给服务间调用，不对外暴露凭据。
 */
@IgnorePermission
@RestController
@RequestMapping("/api/core/user")
public class UserCoreController {

    private final UserService userService;

    public UserCoreController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getByName")
    public CommonResult<?> getByName(@NonNull String name) {
        List<UserVO> userVOList = userService.getByName(name);
        return ResultUtils.ok(userVOList);
    }
}
