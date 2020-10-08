package cc.mrbird.febs.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * Entity
 *
 * @author MrBird
 * @date 2020-07-16 14:57:57
 */
@Data
@TableName("T_MATTER")
@KeySequence(value = "T_MATTER_MATTER_ID")
public class Matter implements Serializable {

    private static final long serialVersionUID = -1222167740685573379L;
    /**
     * 事务ID
     */
    @TableId(value = "MATTER_ID", type = IdType.INPUT)
    private Long matterId;

    /**
     * 事务名称
     */
    @TableField("MATTER_NAME")
    private String matterName;

    /**
     * 事务内容
     */
    @TableField("MATTER_TEXT")
    private String matterText;

    /**
     * 开始时间
     */
    @TableField("MATTER_OPEN")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date matterOpen;

    @TableField(exist = false)
    private String matterOpenStr;

    /**
     * 结束时间
     */
    @TableField("END")
    private Date end;

    @TableField(exist = false)
    private String endStr;

    /**
     * 是否开启 0开启 1关闭
     */
    @TableField("IS_OPEN")
    private Integer isOpen;

    /**
     * 重要程度  0不重要1重要 默认0
     */
    @TableField("IMPORTANT")
    private Integer important;

    /**
     * 是否紧急 0不紧急1紧急  默认0
     */
    @TableField("URGENT")
    private Integer urgent;

    /**
     * 事务周期
     */
    @TableField("PERIOD")
    private String period;

    /**
     * 部门ID
     */
    @TableField("DEPT_ID")
    private Long deptId;

    /**
     * 父事项ID
     */
    @TableField("PATRIARCH_ID")
    private Long patriarchId;

    /**
     * 是否是父事项 0不是1是
     */
    @TableField("IS_PATRIARCH")
    private Integer isPatriarch;

    /**
     *
     */
    @TableField("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @TableField(exist = false)
    private String createTimeStr;
    /**
     *
     */
    @TableField("MODIFY_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date modifyTime;

    @TableField(exist = false)
    private String modifyTimeStr;

    /**
     * 是否循环 默认0 0不循环 1循环
     */
    @TableField("FOR_EACH")
    private Integer forEach;

    /**
     * 周期ID
     */
    @TableField("CYCLE_ID")
    private Long cycleId;

    /**
     * 周期名称
     */
    @TableField(exist = false)
    private String cycleName;


    /**
     * 部门名称
     */
    //@ExcelField(value = "部门")
    @TableField(exist = false)
    private String deptName;

    /**
     * 责任人姓名
     */
    @TableField(exist = false)
    private String name;
    /**
     * 查询结果的 责任人Id
     */
    @TableField(exist = false)
    private String userId;

    /**
     * 用于查询条件的责任人Id
     */
    @TableField(exist = false)
    private Long longUserId;
    /**
     * 对于个人是否重要
     */
    @TableField(exist = false)
    private Integer importantOne;
    /**
     * 对于个人是否紧急
     */
    @TableField(exist = false)
    private Integer urgentOne;

    /**
     * 提醒时间ID
     */
    @TableField(exist = false)
    private String remindId;

    /**
     * 提醒时间S
     */
    @TableField(exist = false)
    private String remindTime;

    /**
     * 是否已完成
     */
    @TableField(exist = false)
    private Integer finish;

    /**
     * 指向责任组
     */
    @TableField("TEAM_ID")
    private String teamId;

    @TableField(exist = false)
    private String cycleIdStr;

    /**
     * 循环当天查询条件
     */
    @TableField(exist = false)
    private String date;

    @TableField(exist = false)
    private String createTimeFrom;
    @TableField(exist = false)
    private String createTimeTo;

    /**
     * 完成人数
     */
    @TableField(exist = false)
    private Integer over;
    @TableField(exist = false)
    private Integer noOver;

    public Long getId() {
        return matterId;
    }

    /**
     * 升序降序条件
     */
    @TableField(exist = false)
    private String order;
    /**
     * 排序列
     */
    @TableField(exist = false)
    private String status;
    /**
     * 完成时间
     */
    @TableField(exist = false)
    private Data actuallyTime;

}
