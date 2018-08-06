package com.lwz.test;

import com.lwz.model.Vo.UserVo;
import com.lwz.utils.TaleUtils;

public class PwdTest {
    public static void main(String args[]){
        UserVo user = new UserVo();
        user.setUsername("admin");
        user.setPassword("123456");
        String encodePwd = TaleUtils.MD5encode(user.getUsername() + user.getPassword());
        System.out.println(encodePwd);
    }
}
