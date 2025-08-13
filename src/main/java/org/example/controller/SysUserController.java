package org.example.controller;

import org.example.entity.SysUser;
import org.example.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Resource
    public SysUserService sysUserService;

    @GetMapping("/list")
    public List<SysUser> getList() {
        return sysUserService.list();
    }
}