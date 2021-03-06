package cc.mrbird.febs.system.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.service.ValidateCodeService;
import cc.mrbird.febs.common.utils.Md5Util;
import cc.mrbird.febs.monitor.entity.LoginLog;
import cc.mrbird.febs.monitor.service.ILoginLogService;
import cc.mrbird.febs.system.entity.User;
import cc.mrbird.febs.system.service.IUserService;
import lombok.RequiredArgsConstructor;

/**
 * @author MrBird
 */
@Validated
@RestController
@RequiredArgsConstructor
public class LoginController extends BaseController {

    private final IUserService userService;
    private final ValidateCodeService validateCodeService;
    private final ILoginLogService loginLogService;

    @PostMapping("login")
    @Limit(key = "login", period = 60, count = 10, name = "登录接口", prefix = "limit")
    public FebsResponse login(@NotBlank(message = "{required}") String username,
                              @NotBlank(message = "{required}") String password, @NotBlank(message = "{required}") String verifyCode,
                              boolean rememberMe, HttpServletRequest request) throws FebsException {
        HttpSession session = request.getSession();
        User user = null;
        String oaid = request.getHeader("iv-user");
        if (oaid != null && !"".equals(oaid)) { // 单点登录 wuzy 20201104
            user = new User();
            user.setOaid(oaid);
            List<User> users = userService.findUserList(user);
            if (users != null && users.size() > 0) {
                user = users.get(0);
                username = user.getUsername();
                password = user.getPassword();
            }
        } else {
            validateCodeService.check(session.getId(), verifyCode);
            password = Md5Util.encrypt(username.toLowerCase(), password);
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        super.login(token);
        // 保存登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        loginLog.setSystemBrowserInfo();
        this.loginLogService.saveLoginLog(loginLog);
        // session里存储信息
        if (user == null)
            user = this.userService.findByName(username);
        session.setAttribute("deptId", user.getDeptId());
        session.setAttribute("userId", user.getUserId());
        return new FebsResponse().success();
    }

    @PostMapping("regist")
    public FebsResponse regist(@NotBlank(message = "{required}") String username,
                               @NotBlank(message = "{required}") String password) throws FebsException {
        User user = userService.findByName(username);
        if (user != null) {
            throw new FebsException("该用户名已存在");
        }
        this.userService.regist(username, password);
        return new FebsResponse().success();
    }

    @GetMapping("index/{username}")
    public FebsResponse index(@NotBlank(message = "{required}") @PathVariable String username) {
        // 更新登录时间
        this.userService.updateLoginTime(username);
        Map<String, Object> data = new HashMap<>(5);
        // 获取系统访问记录
        Long totalVisitCount = this.loginLogService.findTotalVisitCount();
        data.put("totalVisitCount", totalVisitCount);
        Long todayVisitCount = this.loginLogService.findTodayVisitCount();
        data.put("todayVisitCount", todayVisitCount);
        Long todayIp = this.loginLogService.findTodayIp();
        data.put("todayIp", todayIp);
        // 获取近期系统访问记录
        List<Map<String, Object>> lastSevenVisitCount = this.loginLogService.findLastSevenDaysVisitCount(null);
        data.put("lastSevenVisitCount", lastSevenVisitCount);
        User param = new User();
        param.setUsername(username);
        List<Map<String, Object>> lastSevenUserVisitCount = this.loginLogService.findLastSevenDaysVisitCount(param);
        data.put("lastSevenUserVisitCount", lastSevenUserVisitCount);
        return new FebsResponse().success().data(data);
    }

    @GetMapping("images/captcha")
    @Limit(key = "get_captcha", period = 60, count = 10, name = "获取验证码", prefix = "limit")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException, FebsException {
        validateCodeService.create(request, response);
    }

    // @GetMapping("test")
    @Limit(key = "getInto", period = 60, count = 10, name = "单点登录接口", prefix = "limit")
    public FebsResponse getInto(HttpServletRequest request) throws FebsException {
        HttpSession session = request.getSession();
        String username = request.getHeader("iv-user");
        // String username = "MrBird";
        String password = "1234qwer";
        password = Md5Util.encrypt(username.toLowerCase(), password);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        super.login(token);
        // 保存登录日志
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        loginLog.setSystemBrowserInfo();
        this.loginLogService.saveLoginLog(loginLog);
        // session里存储信息
        User user = this.userService.findByName(username);
        System.err.println();
        session.setAttribute("deptId", user.getDeptId());
        session.setAttribute("userId", user.getUserId());
        return new FebsResponse().success();
    }

}
