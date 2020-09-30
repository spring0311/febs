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
@TableName("t_dept")
@Excel("部门信息表")
@KeySequence(value = "T_DEPT_DEPT_ID")
public class Dept implements Serializable {

    private static final long serialVersionUID = 5702271568363798328L;

    public static final Long TOP_NODE = 0L;

    /**
     * 部门 ID
     */
    @TableId(value = "DEPT_ID", type = IdType.INPUT)
    private Long deptId;

    /**
     * 上级部门 ID
     */
    @TableField("PARENT_ID")
    private Long parentId;

    /**
     * 部门名称
     */
    @TableField("DEPT_NAME")
    @NotBlank(message = "{required}")
    @Size(max = 10, message = "{noMoreThan}")
    @ExcelField(value = "部门名称")
    private String deptName;

    /**
     * 排序
     */
    @TableField("ORDER_NUM")
    private Long orderNum;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    @ExcelField(value = "创建时间", writeConverter = TimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("MODIFY_TIME")
    @ExcelField(value = "修改时间", writeConverter = TimeConverter.class)
    private Date modifyTime;

}
