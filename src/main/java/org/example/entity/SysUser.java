package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer userId;

    private String username;

    private String password;

    private String sex;

    private String address;

    private Integer enabled;

    @TableField(exist = false)
    private Integer accountNoExpired;

    @TableField(exist = false)
    private Integer credentialsNoExpired;

    @TableField(exist = false)
    private Integer accountNoLocked;
}
