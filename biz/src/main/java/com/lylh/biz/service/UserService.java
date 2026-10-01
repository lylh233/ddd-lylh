package com.lylh.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lylh.biz.model.dto.UserDTO;
import com.lylh.biz.model.vo.UserVO;
import com.lylh.common.model.BizException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户登录与资料维护的业务入口。持久化留在仓储层，这里只做校验和结果转换。
 */
public interface UserService {

    /**
     * 校验通过后签发 token。明文密码只参与比对，不写入日志。
     *
     * @param username 登录名
     * @param password 明文密码
     * @return 登录 token
     * @throws BizException 用户不存在或密码错误
     */
    String login(String username, String password);

    /**
     * 读取当前请求已经验过的登录态。身份来自拦截器写入的线程上下文，避免再解析一次 token。
     *
     * @param request 与控制层签名保持一致
     * @return 当前用户
     * @throws BizException 当前线程没有登录态
     */
    UserVO getLogonUser(HttpServletRequest request);

    /**
     * 创建用户。用户名重复时直接失败，避免同名账号被保存两次。
     *
     * @param userDTO 待创建资料，密码在转换时加密
     * @return 新用户 uuid
     * @throws BizException 用户名已存在
     */
    String createUser(UserDTO userDTO);

    /**
     * 按 uuid 更新姓名、用户名和类型。只有用户名变化时才做重名校验。
     *
     * @param userDTO 含 uuid 的更新内容
     * @throws BizException 用户不存在，或新用户名与其他人重复
     */
    void updateUser(UserDTO userDTO);

    /**
     * 按 JSON 条件分页查询。页大小在服务工具里封顶，避免一次查出过多数据。
     *
     * @param filter   查询条件 JSON，空白按无条件处理
     * @param pageNum  页码，从 1 开始
     * @param pageSize 每页条数
     * @return 用户分页结果
     * @throws BizException 条件不是合法 JSON
     */
    Page<UserVO> queryUser(String filter, Integer pageNum, Integer pageSize);

    /**
     * 按姓名或用户名精确查找。没有命中时返回空列表，调用方可以直接遍历。
     *
     * @param name 姓名或用户名
     * @return 匹配的用户，可能为空列表
     */
    List<UserVO> getByName(String name);
}
