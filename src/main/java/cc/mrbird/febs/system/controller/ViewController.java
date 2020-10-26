package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.authentication.ShiroHelper;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.DateUtil;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.Md5Util;
import cc.mrbird.febs.monitor.entity.LoginLog;
import cc.mrbird.febs.monitor.service.ILoginLogService;
import cc.mrbird.febs.system.entity.*;
import cc.mrbird.febs.system.mapper.UserMatterMapper;
import cc.mrbird.febs.system.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.ExpiredSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MrBird
 */
@Controller("systemView")
@RequiredArgsConstructor
public class ViewController extends BaseController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ShiroHelper shiroHelper;

    @Autowired
    private IUserDataPermissionService userDataPermissionService;

    @Autowired
    private IPeriodService periodService;

    @GetMapping("login")
    @ResponseBody
    public Object login(HttpServletRequest request) {
        if (FebsUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setViewName(FebsUtil.view("login"));
            return mav;
        }
    }

    @GetMapping("unauthorized")
    public String unauthorized() {
        return FebsUtil.view("error/403");
    }


    @GetMapping("/")
    public String redirectIndex() {
        return "redirect:/index";
    }

    @GetMapping("index")
    public String index(Model model) {
        AuthorizationInfo authorizationInfo = shiroHelper.getCurrentUserAuthorizationInfo();
        User user = super.getCurrentUser();
        User currentUserDetail = userService.findByName(user.getUsername());
        currentUserDetail.setPassword("It's a secret");
        model.addAttribute("user", currentUserDetail);
        model.addAttribute("permissions", authorizationInfo.getStringPermissions());
        model.addAttribute("roles", authorizationInfo.getRoles());
        return "index";
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "layout")
    public String layout() {
        return FebsUtil.view("layout");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "password/update")
    public String passwordUpdate() {
        return FebsUtil.view("system/user/passwordUpdate");
    }

    /**
     * 用户
     *
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "user/profile")
    public String userProfile() {
        return FebsUtil.view("system/user/userProfile");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/avatar")
    public String userAvatar() {
        return FebsUtil.view("system/user/avatar");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/profile/update")
    public String profileUpdate() {
        return FebsUtil.view("system/user/profileUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user")
    @RequiresPermissions("user:view")
    public String systemUser() {
        return FebsUtil.view("system/user/user");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/add")
    @RequiresPermissions("user:add")
    public String systemUserAdd() {
        return FebsUtil.view("system/user/userAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/detail/{username}")
    @RequiresPermissions("user:view")
    public String systemUserDetail(@PathVariable String username, Model model) {
        resolveUserModel(username, model, true);
        return FebsUtil.view("system/user/userDetail");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/update/{username}")
    @RequiresPermissions("user:update")
    public String systemUserUpdate(@PathVariable String username, Model model) {
        resolveUserModel(username, model, false);
        return FebsUtil.view("system/user/userUpdate");
    }

    /**
     * 角色
     *
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/role")
    @RequiresPermissions("role:view")
    public String systemRole() {
        return FebsUtil.view("system/role/role");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/role/add")
    @RequiresPermissions("role:add")
    public String systemRoleAdd() {
        return FebsUtil.view("system/role/roleAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/role/update/{roleId}")
    @RequiresPermissions("role:update")
    public String systemRoleUpdate(@PathVariable Long roleId, Model model) {
        resolveRoleModel(roleId, model, true);
        return FebsUtil.view("system/role/roleUpdate");
    }

    /**
     * 菜单
     *
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/menu")
    @RequiresPermissions("menu:view")
    public String systemMenu() {
        return FebsUtil.view("system/menu/menu");
    }


    /**
     * 部门
     *
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/dept")
    @RequiresPermissions("dept:view")
    public String systemDept() {
        return FebsUtil.view("system/dept/dept");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/dept/add")
    @RequiresPermissions("dept:add")
    public String systemDeptAdd() {
        return FebsUtil.view("system/dept/deptAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/dept/update/{deptId}")
    @RequiresPermissions("role:update")
    public String systemDeptUpdate(@PathVariable Integer deptId, Model model) {
        resolveDeptModel(deptId, model, true);
        return FebsUtil.view("system/dept/deptUpdate");
    }

    /**
     * 事务
     *
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter")
    @RequiresPermissions("matter:view")
    public String systemMatter() {
        return FebsUtil.view("system/matter/matter");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matterByOne")
    @RequiresPermissions("matter:view")
    public String systemMatterByOne() {
        return FebsUtil.view("system/matter/matterForOne");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/add")
    @RequiresPermissions("matter:view")
    public String systemMatterAdd() {
        return FebsUtil.view("system/matter/matterAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/addForOne")
    @RequiresPermissions("matter:view")
    public String systemMatterAddForOne() {
        return FebsUtil.view("system/matter/matterAddForOne");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/update/{matterId}")
    @RequiresPermissions("matter:view")
    public String systemMatterUpdate(@PathVariable Long matterId, Model model) {
        resolveMatterModel(matterId, model, true);
        return FebsUtil.view("system/matter/matterUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/updateOne/{matterId}")
    @RequiresPermissions("matter:view")
    public String systemMatterUpdateOne(@PathVariable Long matterId, Model model) {
        resolveMatterModel(matterId, model, true);
        return FebsUtil.view("system/matter/matterUpdateOne");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/updateOneIU/{matterId}/{userId}")
    @RequiresPermissions("matter:view")
    public String systemMatterUpdateOneIU(@PathVariable Long matterId, @PathVariable Long userId, Model model) {
        UserMatter userMatter = new UserMatter();
        userMatter.setMatterId(matterId);
        userMatter.setUserId(userId);
        System.err.println("usermatter!!!!!:" + userMatter);
        returnUserMatter(userMatter, model);
        return FebsUtil.view("system/matter/matterUpdateIU");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/addOut/{matterId}")
    @RequiresPermissions("matter:view")
    public String systemMatterAdder(@PathVariable Long matterId, Model model) {
        resolveMatterModel(matterId, model, true);
        return FebsUtil.view("system/matter/matterAddOut");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/out/{matterId}")
    @RequiresPermissions("matter:view")
    public String systemMatterAdder(@PathVariable Long matterId, HttpServletRequest request) {
        request.getSession().setAttribute("insertMatterId", matterId);
        System.err.println("这里这里这里这里这里这里:::" + matterId);
        return FebsUtil.view("system/matter/matterOut");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/remind/{matterId}")
    @RequiresPermissions("matter:view")
    public String systemMatterRemindUpdate(@PathVariable Long matterId, Model model) {
        resolveMatterModel(matterId, model, true);
        resolveRemindsModel(matterId, model, true);
        return FebsUtil.view("system/matter/matterRemind");
    }

    /**
     * 时间轴 日历
     *
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/cycle")
    @RequiresPermissions("matter:view")
    public String cycle() {
        return FebsUtil.view("system/matter/cycle");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/matter/calendar")
    @RequiresPermissions("matter:view")
    public String calendar() {
        return FebsUtil.view("system/matter/calendar");
    }


    /**
     * 周期
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/cycle")
    @RequiresPermissions("matter:view")
    public String systemCycle() {
        return FebsUtil.view("system/cycle/cycle");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/cycle/update/{periodId}")
    @RequiresPermissions("matter:view")
    public String updateCycle(@PathVariable("periodId") Long periodId, HttpSession session) {
        System.err.println("updateCycle:" + periodId);
        session.setAttribute("periodId", periodId);
        return FebsUtil.view("system/cycle/cycleUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/cycle/add")
    @RequiresPermissions("matter:view")
    public String addCycle() {
        return FebsUtil.view("system/cycle/cycleAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/cycle/remind/{periodId}")
    @RequiresPermissions("matter:view")
    public String cycleRemindAdd(@PathVariable("periodId") Long periodId, Model model) {
        cycleRemind(periodId, model, true);
        return FebsUtil.view("system/cycle/remind");
    }

    /**
     * 分组
     *
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/team")
    @RequiresPermissions("team:view")
    public String systemTeam() {
        return FebsUtil.view("system/team/team");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/team/add")
    @RequiresPermissions("team:view")
    public String systemTeamrAdd() {
        return FebsUtil.view("system/team/teamAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/team/update/{teamId}")
    @RequiresPermissions("team:view")
    public String systemTeamUpdate(@PathVariable Long teamId, Model model) {
        //System.err.println("teamId:" + teamId);
        resolveTeamModel(teamId, model);
        return FebsUtil.view("system/team/teamUpdate");
    }


    /**
     * 基础页
     *
     * @return
     */
    @RequestMapping(FebsConstant.VIEW_PREFIX + "index")
    public String pageIndex() {
        return FebsUtil.view("index");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "404")
    public String error404() {
        return FebsUtil.view("error/404");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "403")
    public String error403() {
        return FebsUtil.view("error/403");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "500")
    public String error500() {
        return FebsUtil.view("error/500");
    }

    /**
     * 返回user对象
     *
     * @param username
     * @param model
     * @param transform
     */
    private void resolveUserModel(String username, Model model, Boolean transform) {
        User user = userService.findByName(username);
        String deptIds = userDataPermissionService.findByUserId(String.valueOf(user.getUserId()));
        user.setDeptIds(deptIds);
        model.addAttribute("user", user);
        if (transform) {
            String sex = user.getSex();
            if (User.SEX_MALE.equals(sex)) {
                user.setSex("男");
            } else if (User.SEX_FEMALE.equals(sex)) {
                user.setSex("女");
            } else {
                user.setSex("保密");
            }
        }
        if (user.getLastLoginTime() != null) {
            model.addAttribute("lastLoginTime", DateUtil.getDateFormat(user.getLastLoginTime(), DateUtil.FULL_TIME_SPLIT_PATTERN));
        }
    }

    /**
     * 返回role对象
     */
    @Autowired
    private IRoleService roleService;
    @Autowired
    private final IRoleMenuService roleMenuService;

    private void resolveRoleModel(Long roleId, Model model, Boolean transform) {
        Role dao = new Role();
        dao.setRoleId(roleId);
        Role role = this.roleService.findByRole(dao);
        model.addAttribute("role", role);
    }

    /**
     * 返回dept对象
     */
    @Autowired
    private IDeptService deptService;

    private void resolveDeptModel(Integer deptId, Model model, Boolean transform) {
        Dept dept = deptService.findById(deptId);
        model.addAttribute("dept", dept);
    }

    @Autowired
    private IMatterService matterService;

    /**
     * 返回matter对象
     *
     * @param matterId
     * @param model
     * @param transform
     */
    private void resolveMatterModel(Long matterId, Model model, Boolean transform) {
        Matter matter = new Matter();
        matter.setMatterId(matterId);
        List<Matter> matters = matterService.findMatters(matter);
        System.err.println(matters.get(0));
        model.addAttribute("matter", matters.get(0));
    }

    @Autowired
    private final UserMatterMapper userMatterMapper;

    /**
     * @param userMatter
     * @param model
     */
    private void returnUserMatter(UserMatter userMatter, Model model) {
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(userMatter);
        UserMatter dao = userMatterMapper.selectList(queryWrapper).get(0);
        model.addAttribute("userMatter", dao);
    }

    private void cycleRemind(Long periodId, Model model, Boolean transform) {
        Period period = periodService.getById(periodId);
        model.addAttribute("period", period);
    }

    @Autowired
    private IRemindService remindService;

    /**
     * 返回提醒时间对象
     *
     * @param matterId
     * @param model
     * @param transform
     */
    private void resolveRemindsModel(Long matterId, Model model, Boolean transform) {
        Remind remind = new Remind();
        remind.setMatterId(matterId);
        List<Remind> reminds = remindService.findReminds(remind);
        model.addAttribute("reminds", reminds);
    }

    /**
     * 返回team对象
     *
     * @param matterId
     * @param model
     * @param transform
     */
    @Autowired
    private ITeamService teamService;

    private void resolveTeamModel(Long teamId, Model model) {
        Team team = new Team();
        team.setTeamId(teamId);
        //System.err.println(team);
        List<Team> teams = teamService.findTeams(team);
        //System.err.println(teams);
        model.addAttribute("team", teams.get(0));
    }

    private final ILoginLogService loginLogService;

    @Limit(key = "getInto", period = 60, count = 10, name = "单点登录接口", prefix = "limit")
    public void getInto(HttpServletRequest request) throws FebsException {
        HttpSession session = request.getSession();
        String username = request.getHeader("iv-user");
        String password = "1234qwer";
        password = Md5Util.encrypt(username.toLowerCase(), password);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        super.login(token);
        // 保存登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        loginLog.setSystemBrowserInfo();
        this.loginLogService.saveLoginLog(loginLog);
        //session里存储信息
        User user = this.userService.findByName(username);
        System.err.println();
        session.setAttribute("deptId", user.getDeptId());
        session.setAttribute("userId", user.getUserId());
    }

}
