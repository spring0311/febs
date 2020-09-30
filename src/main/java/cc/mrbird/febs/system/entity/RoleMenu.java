package cc.mrbird.febs.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author MrBird
 */
@Data
@TableName("t_role_menu")
@KeySequence(value = "T_ROLE_MENU_ROLE_MENU_ID")
public class RoleMenu implements Serializable {

    private static final long serialVersionUID = -5200596408874170216L;

    @ApiModelProperty(value = "映射表ID")
    @TableId(value = "ROLE_MENU_ID", type = IdType.INPUT)
    @TableField("ROLE_MENU_ID")
    private Long roleMenuId;

    /**
     * 角色ID
     */
    @TableField("ROLE_ID")
    private Long roleId;

    /**
     * 菜单/按钮ID
     */
    @TableField("MENU_ID")
    private Long menuId;


}
