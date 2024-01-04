package com.lylh.app.api;

import com.lylh.biz.model.common.CommonResult;
import com.lylh.biz.model.common.PageListResult;
import com.lylh.biz.model.common.ResultUtils;
import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.biz.service.UserService;
import com.lylh.common.controller.annotation.IgnorePermission;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @IgnorePermission
    @GetMapping("/login")
    public CommonResult<?> login(@NonNull String username, @NonNull String password) {
        String token = userService.login(username, password);
        return ResultUtils.ok(token);
    }

    @GetMapping("/logon/user")
    public CommonResult<?> getLogonUser(HttpServletRequest request) {
        UserVO userVo = userService.getLogonUser(request);
        return ResultUtils.ok(userVo);
    }

    @PostMapping("/create")
    public CommonResult<String> createUser(@RequestBody UserDTO userDTO) {
        String uuid = userService.createUser(userDTO);
        return ResultUtils.ok(uuid);
    }

    @PostMapping("/update")
    public CommonResult<?> updateUser(@RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO);
        return ResultUtils.ok();
    }

    @GetMapping("/query")
    public PageListResult<?> queryUser(@RequestParam(defaultValue = "{}") String filter,
                                       @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                       @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return ResultUtils.ok(userService.queryUser(filter, pageNum, pageSize));
    }
}
