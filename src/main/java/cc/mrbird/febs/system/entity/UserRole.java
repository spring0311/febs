package cc.mrbird.febs.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Entity
 *
 * @author weizihao
 * @date 2020-07-27 16:27:45
 */
@Data
@TableName("T_USER_ROLE")
@KeySequence(value = "T_USER_ROLE_USER_ROLE_ID")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 2354394771912648574L;
    @ApiModelProperty(value = "映射表ID")
    @TableId(value = "USER_ROLE_ID", type = IdType.INPUT)
    @TableField("USER_ROLE_ID")
    private Long userRoleId;

    /**
     * 用户ID
     */
    @TableField("USER_ID")
    private Long userId;

    /**
     * 角色ID
     */
    @TableField("ROLE_ID")
    private Long roleId;


}
