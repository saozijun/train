package com.cen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cen.common.Constants;
import com.cen.common.Result;
import com.cen.common.lang.Const;
import com.cen.controller.dto.UserDTO;
import com.cen.entity.User;
import com.cen.exception.ServiceException;
import com.cen.mapper.UserMapper;
import com.cen.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.utils.RedisUtil;
import com.cen.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author volcano
 * 
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private RedisUtil redisUtil;
    //登录逻辑
    @Override
    public UserDTO login(UserDTO userDTO) {
        //获得存储在redis中的验证码
//        String redisCode = (String) redisUtil.get(Const.CAPTCHA_KEY);
//        if(redisCode== null || !redisCode.equals(userDTO.getCode())){
//            throw new ServiceException(Constants.CODE_402,"验证码错误");
//        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userDTO.getUsername());
        queryWrapper.eq("password",userDTO.getPassword());
        User one = getOne(queryWrapper);
        if(one!=null){
            //把数据库查到的one数据拷贝到userDTO里  true是忽略大小写
            BeanUtil.copyProperties(one,userDTO,true);
            String token = TokenUtils.getToken(one);
            userDTO.setToken(token);
            return userDTO;
        }else{
            throw new ServiceException(Constants.CODE_402,"用户名或密码错误");
        }
    }
    //注册逻辑
    @Override
    public Object register(UserDTO userDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userDTO.getUsername());
        User one = getOne(queryWrapper);
        if (one==null){
            one = new User();
            BeanUtil.copyProperties(userDTO,one,true);
            one.setRole("用户");
            one.setRoleId(2);
            save(one);
        }else{
            throw new ServiceException(Constants.CODE_500,"用户已存在");
        }
        return one;
    }

    //用户新增的时候判断一下是否存在
    @Override
    public Result saveUser(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",user.getUsername());
        User one = getOne(queryWrapper);
        if (one==null){
            return Result.success(saveOrUpdate(user));
        }else{
            if(user.getId()!=null){
                QueryWrapper<User> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("id",user.getId());
                User upOne = getOne(queryWrapper2);
                if(user.getUsername().equals(upOne.getUsername())){
                    return Result.success(saveOrUpdate(user));
                }else{
                    throw new ServiceException(Constants.CODE_500,"修改失败,用户已存在");
                }
            }else{
                throw new ServiceException(Constants.CODE_500,"用户已存在");
            }
        }
    }

    @Override
    public Result editPow(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",user.getId());
        User one = getOne(queryWrapper);
        String pow = one.getPassword();
        String newPow = user.getPassword();
        if(one.getPassword().equals(user.getPassword())){
            throw new ServiceException(Constants.CODE_500,"与旧密码重复");
        }else{
            return Result.success(saveOrUpdate(user));
        }
    }
}
