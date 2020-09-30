package cc.mrbird.febs.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 周期实体类
 *
 * @author: weiZiHao
 * @create: 2020-09-03 21:28
 */
@Data
@TableName("T_PERIOD")
@KeySequence(value = "T_PERIOD_PERIOD_ID")
public class Period implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = -2513670294847194326L;

    @TableId(value = "PERIOD_ID", type = IdType.INPUT)
    private Long periodId;

    @TableField("PERIOD_NAME")
    private String periodName;

    @TableField("PERIOD_OPEN")
    @JsonFormat(pattern = "MM-dd", timezone = "GMT+8")
    private Date periodOpen;

    @TableField(exist = false)
    private String periodOpenStr;

    @TableField("PERIOD_END")
    @JsonFormat(pattern = "MM-dd", timezone = "GMT+8")
    private Date periodEnd;

    @TableField(exist = false)
    private String periodEndStr;

    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("MODIFY_TIME")
    @JsonFormat(pattern = "MM-dd", timezone = "GMT+8")
    private Date modifyTime;

    @TableField("OPEN_STR")
    private String openStr;

    @TableField("CYCLE_ID")
    private Long cycleId;

}
