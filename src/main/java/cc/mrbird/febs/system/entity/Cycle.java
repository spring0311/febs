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
 * @create: 2020-09-10 13:39
 */
@Data
@TableName("T_CYCLE")
@KeySequence(value = "T_CYCLE_CYCLE_ID")
public class Cycle implements Serializable {
    private static final long serialVersionUID = 8382616733921044350L;

    @TableId(value = "CYCLE_ID", type = IdType.INPUT)
    private Long cycleId;

    @TableField("CYCLE_NAME")
    private String cycleName;

    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField("MODIFY_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date modifyTime;
}
