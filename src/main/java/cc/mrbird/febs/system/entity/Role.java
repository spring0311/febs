package cc.mrbird.febs.system.entity;

import cc.mrbird.febs.common.converter.TimeConverter;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @author MrBird
 */
@Data
@TableName("t_role")
@Excel("角色信息表")
@KeySequence(value = "T_ROLE_ROLE_ID")
public class Role implements Serializable {

    private static final long serialVersionUID = -4493960686192269860L;
    /**
     * 角色ID
     */
    @TableId(value = "ROLE_ID", type = IdType.INPUT)
    private Long roleId;

    /**
     * 角色名称
     */
    @TableField("ROLE_NAME")
    @ExcelField(value = "角色名称")
    @NotBlank(message = "{required}")
    @Size(max = 10, message = "{noMoreThan}")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField("REMARK")
    @ExcelField(value = "角色描述")
    @Size(max = 50, message = "{noMoreThan}")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    @ExcelField(value = "创建时间", writeConverter = TimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    @TableField(exist = false)
    private String createTimeStr;

    /**
     * 修改时间
     */
    @TableField("MODIFY_TIME")
    @ExcelField(value = "修改时间", writeConverter = TimeConverter.class)
    private Date modifyTime;

    /**
     * 角色对应的菜单（按钮） id
     */
    @TableField(exist = false)
    private transient String menuIds;
}
