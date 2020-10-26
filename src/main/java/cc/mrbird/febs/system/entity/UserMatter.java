package cc.mrbird.febs.system.entity;


import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity
 *
 * @author weizihao
 * @date 2020-07-27 10:01:07
 */
@Data
@TableName("t_user_matter")
@KeySequence(value = "T_USER_MATTER_USER_MATTER_ID")
public class UserMatter implements Serializable {


    private static final long serialVersionUID = 3473123953096452014L;
    /**
     * 映射表ID
     */
    @ApiModelProperty(value = "映射表ID")
    @TableId(value = "USER_MATTER_ID", type = IdType.INPUT)
    @TableField("USER_MATTER_ID")
    private Long tUserMatterId;

    /**
     * 用户id
     */
    @TableField("USER_ID")
    private Long userId;

    /**
     * 事务id
     */
    @TableField("MATTER_ID")
    private Long matterId;


    /**
     * 事项对于个人是否重要
     */
    @TableField("IMPORTANT_ONE")
    private Integer importantOne;

    /**
     * 收藏字段
     */
    @TableField("sign")
    private Integer sign;
    /**
     * 事项对于个人是否紧急
     */
    @TableField("URGENT_ONE")
    private Integer urgentOne;

    /**
     * 事项对于个人是否完成
     */
    @ApiModelProperty(value = "是否完成 0未完成 1完成 默认未完成")
    @TableField("Finish")
    private Integer finish;

    @ApiModelProperty(value = "完成时间")
    @TableField("ACTUALLY_TIME")
    private Date actuallyTime;


    @ApiModelProperty(value = "是否根据 总提醒时间修改 0是1不是")
    @TableId("IS_REMIND")
    private Integer isRemind;

    @TableField(exist = false)
    private String name;

}
