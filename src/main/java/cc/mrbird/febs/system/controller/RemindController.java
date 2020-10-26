package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.system.entity.Remind;
import cc.mrbird.febs.system.service.IRemindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.List;


/**
 * @author weiZiHao
 * @date 2020/9/14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("remind")
@CrossOrigin
public class RemindController extends BaseController {


    @Autowired
    private IRemindService tRemindService;

    @GetMapping("taday")
    @ControllerEndpoint(operation = "时间轴与日历", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse getTReminds(HttpServletRequest request) throws ParseException {
        HttpSession session = request.getSession();
        return new FebsResponse().success().data(tRemindService.findByDate((Long) session.getAttribute("userId")));
    }

    @RequestMapping("list")
    @ControllerEndpoint(operation = "时间轴与日历", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse getAllTReminds(HttpServletRequest request, Remind tRemind) {
        System.err.println("getAllTReminds!!!");
        HttpSession session = request.getSession();
        tRemind.setUserId((Long) session.getAttribute("userId"));
        List<Remind> reminds = tRemindService.findAllTReminds(tRemind.getUserId());
        reminds.forEach(remind -> {
            System.err.println("remind:" + remind);
        });
        return new FebsResponse().success().data(reminds);
    }

    /*
     * 根据提醒时间获取事项
     */
    @GetMapping("getByremindtime")
    @ControllerEndpoint(operation = "时间轴与日历", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse getByremindtime(Remind tRemind) throws ParseException {
        return new FebsResponse().success().data(tRemindService.getByremindtime(tRemind));
    }

    @GetMapping("remind/{remindId")
    @ControllerEndpoint(operation = "时间轴与日历", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse getTRemindForMatter(@PathVariable("remindId") String remindId) {
        return new FebsResponse().success().data(tRemindService.findRemindForMatter(remindId));
    }

    @GetMapping("getBymatterId/{matterId}")
    @ControllerEndpoint(operation = "时间轴与日历", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse getTRemindForMatterBymatterId(@PathVariable("matterId") String matterId) {
        Long matterIds = Long.valueOf(0);
        if (!(matterId == null) || !(matterId == "")) {
            matterIds = Long.valueOf(matterId);
        }
        return new FebsResponse().success().data(tRemindService.getBymatterId(matterIds));
    }

    @GetMapping("add")
    @ControllerEndpoint(operation = "新增提醒时间", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse addRemind(Remind tRemind) throws ParseException {
        tRemindService.addRemind(tRemind);
        return new FebsResponse().success();
    }

    @GetMapping("delete/{remindId}")
    @ControllerEndpoint(operation = "时间轴与日历", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse delByRemindId(@PathVariable("remindId") String remindId) {
        Long remindIds = Long.valueOf(0);
        if (!(remindId == null) || !(remindId == "")) {
            remindIds = Long.valueOf(remindId);
        }
        tRemindService.delByRemindId(remindIds);
        return new FebsResponse().success();
    }

    @GetMapping("delete")
    @ControllerEndpoint(operation = "时间轴与日历", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse delRemind(Remind remind) {
        tRemindService.removeById(remind.getRemindId());
        return new FebsResponse().success();
    }


}
