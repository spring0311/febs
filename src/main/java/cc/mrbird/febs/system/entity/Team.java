package cc.mrbird.febs.system.entity;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * Entity
 *
 * @author weizihao
 * @date 2020-07-21 17:39:37
 */
@Data
@TableName("t_team")
@KeySequence(value = "T_TEAM_TEAM_ID")
public class Team {

    /**
     * 组ID
     */
    @TableId(value = "TEAM_ID", type = IdType.INPUT)
    private Long teamId;

    /**
     * 组名称
     */
    @TableField("TEAM_NAME")
    private String teamName;

    /**
     * 组备注
     */
    @TableField("DESCRIBED")
    private String described;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;


    /**
     *
     */
    @TableField(exist = false)
    private List<User> users;

    /**
     *
     */
    @TableField(exist = false)
    private String userId;

    /**
     *
     */
    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String createTimeFrom;
    @TableField(exist = false)
    private String createTimeTo;
}
