package cc.mrbird.febs.system.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * Entity
 *
 * @author weizihao
 * @date 2020-07-27 16:27:45
 */
@Data
@TableName("t_user_team")
@KeySequence(value = "T_USER_TEAM_USER_TEAM_ID")
public class UserTeam {

    @TableId(value = "USER_TEAM_ID", type = IdType.INPUT)
    private Long userTeamId;

    /**
     * 用户ID
     */
    @TableField("USER_ID")
    private Long userId;

    /**
     * 组ID
     */
    @TableField("TEAM_ID")
    private Long teamId;

}
