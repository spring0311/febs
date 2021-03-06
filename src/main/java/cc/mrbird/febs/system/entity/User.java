package cc.mrbird.febs.system.entity;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import cc.mrbird.febs.common.annotation.Desensitization;
import cc.mrbird.febs.common.annotation.IsMobile;
import cc.mrbird.febs.common.converter.TimeConverter;
import cc.mrbird.febs.common.entity.DesensitizationType;
import lombok.Data;

/**
 * @author weizihao
 */
@Data // 1、@Data可以为类提供读写功能，从而不用写get、set方法。2、他还会为类提供 equals()、hashCode()、toString() 方法。
@TableName("t_user") // 表名注解
@Excel("用户信息表")
@KeySequence(value = "T_USER_USER_ID")
public class User implements Serializable {

    private static final long serialVersionUID = -4352868070794165001L;

    /**
     * 用户状态：有效
     */
    public static final String STATUS_VALID = "1";
    /**
     * 用户状态：锁定
     */
    public static final String STATUS_LOCK = "0";
    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "default.jpg";
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "1234qwer";
    /**
     * 性别男
     */
    public static final String SEX_MALE = "0";
    /**
     * 性别女
     */
    public static final String SEX_FEMALE = "1";
    /**
     * 性别保密
     */
    public static final String SEX_UNKNOW = "2";
    /**
     * 黑色主题
     */
    public static final String THEME_BLACK = "black";
    /**
     * 白色主题
     */
    public static final String THEME_WHITE = "white";
    /**
     * TAB开启
     */
    public static final String TAB_OPEN = "1";
    /**
     * TAB关闭
     */
    public static final String TAB_CLOSE = "0";

    /**
     * 用户 ID
     */
    @TableId(value = "USER_ID", type = IdType.INPUT)
    private Long userId;

    /**
     * 职工号
     */
    @ExcelField(value = "职工号")
    @TableField("USER_EMPNO")
    private String userEmpno;

    /**
     * 用户名
     */
    @TableField("USERNAME") // 注解代表不与数据库字段匹配
    @Size(min = 4, max = 10, message = "{range}")
    @ExcelField(value = "用户名")
    private String username;

    /**
     * 职务
     */
    @TableField("POST") // 注解代表不与数据库字段匹配
    @ExcelField(value = "职务")
    private String post;

    /**
     * 密码
     */
    @TableField("PASSWORD")
    private String password;

    /**
     * 部门 ID
     */
    @TableField("DEPT_ID")
    private Long deptId;

    /**
     * OA ID
     */
    @ExcelField(value = "OAID")
    @TableField("OAID")
    private String oaid;

    /**
     * 邮箱
     */
    @TableField("EMAIL")
    @Size(max = 50, message = "{noMoreThan}")
    @Email(message = "{email}")
    @ExcelField(value = "邮箱")
    private String email;

    /**
     * 联系电话
     */
    @TableField("MOBILE")
    @IsMobile(message = "{mobile}")
    @ExcelField(value = "联系电话")
    @Desensitization(type = DesensitizationType.PHONE)
    private String mobile;

    /**
     * 状态 0锁定 1有效
     */
    @TableField("STATUS")
    @NotBlank(message = "{required}")
    @ExcelField(value = "状态", writeConverterExp = "0=锁定,1=有效")
    private String status;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    @ExcelField(value = "创建时间", writeConverter = TimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("MODIFY_TIME")
    @ExcelField(value = "修改时间", writeConverter = TimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date modifyTime;

    /**
     * 最近访问时间
     */
    @TableField("LAST_LOGIN_TIME")
    @ExcelField(value = "最近访问时间", writeConverter = TimeConverter.class)
    @JsonFormat(pattern = "yyyy年MM月dd日 HH时mm分ss秒", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 性别 0男 1女 2 保密
     */
    @TableField("SSEX")
    @NotBlank(message = "{required}")
    @ExcelField(value = "性别", writeConverterExp = "0=男,1=女,2=保密")
    private String sex;

    /**
     * 头像
     */
    @TableField("AVATAR")
    private String avatar;

    /**
     * 主题
     */
    @TableField("THEME")
    private String theme;

    /**
     * 是否开启 tab 0开启，1关闭
     */
    @TableField("IS_TAB")
    private String isTab;

    /**
     * 名字
     */
    @TableField("name")
    // @Size(max = 100, message = "{noMoreThan}")
    @ExcelField(value = "姓名")
    private String Name;

    /**
     * 部门名称
     */
    @ExcelField(value = "部门")
    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String createTimeFrom;
    @TableField(exist = false)
    private String createTimeTo;
    /**
     * 角色 ID
     */
    @NotBlank(message = "{required}")
    @TableField(exist = false)
    private String roleId;

    @ExcelField(value = "角色")
    @TableField(exist = false)
    private String roleName;

    @TableField(exist = false)
    private String deptIds;

    @TableField(exist = false)
    private Integer finish;

    public Long getId() {
        return userId;
    }
}
