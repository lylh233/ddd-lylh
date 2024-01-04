package com.lylh.biz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService {

    String login(String username, String password);

    UserVO getLogonUser(HttpServletRequest request);

    String createUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);

    Page<UserVO> queryUser(String filter, Integer pageNum, Integer pageSize);

    List<UserVO> getByName(String name);
}
