package cc.mrbird.febs.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 各种映射表
 *
 * @author MrBird
 */
@Data
@TableName("t_user_data_permission")
public class UserDataPermission {

    @TableId("USER_ID")
    private Long userId;
    @TableId("DEPT_ID")
    private Long deptId;

    @TableId("MATTER_ID")
    private Long matterId;

    @TableId("PERIOD_ID")
    private Long periodId;

}