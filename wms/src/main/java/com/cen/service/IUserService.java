package com.cen.service;

import com.cen.controller.dto.UserDTO;
import com.cen.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author volcano
 * 
 */
public interface IUserService extends IService<User> {

    Object login(UserDTO userDTO);

    Object register(UserDTO userDTO);

    Object saveUser(User user);

    Object editPow(User user);
}
