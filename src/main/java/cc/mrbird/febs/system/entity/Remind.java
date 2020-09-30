package cc.mrbird.febs.system.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Entity
 *
 * @author weizihao
 * @date 2020-07-28 09:51:57
 */
@Data
@TableName("t_remind")
@KeySequence(value = "T_REMIND_REMIND_ID")
public class Remind {

    /**
     * 提醒时间ID
     */
    @TableId(value = "REMIND_ID", type = IdType.INPUT)
    private Long remindId;

    /**
     * 提醒时间yyyy-MM-dd
     */
    @TableField("REMIND_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date remindTime;

    @TableField(exist = false)
    private String remindTimestr;

    /**
     * 事务Id
     */
    @TableField("MATTER_ID")
    private Long matterId;

    /**
     * 用于查询条件的责任人Id
     */
    @TableField(exist = false)
    private Long userId;
    /**
     * 事项名称
     */
    @TableField(exist = false)
    private Matter tMatter;

}
