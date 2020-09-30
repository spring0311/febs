package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.system.entity.CalendarObject;
import cc.mrbird.febs.system.entity.Matter;
import cc.mrbird.febs.system.entity.Remind;
import cc.mrbird.febs.system.entity.UserMatter;
import cc.mrbird.febs.system.mapper.UserMatterMapper;
import cc.mrbird.febs.system.service.ICycleService;
import cc.mrbird.febs.system.service.IMatterService;

import cc.mrbird.febs.system.service.IPeriodService;
import cc.mrbird.febs.system.service.IRemindService;
import cc.mrbird.febs.task.AlarmTask;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller
 *
 * @author MrBird
 * @date 2020-07-16 14:57:57
 */
@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
@CrossOrigin
public class MatterController extends BaseController {

    @Autowired
    private final IMatterService matterService;

    @Autowired
    private final IRemindService remindService;

    @Autowired
    private final ICycleService iCycleService;

    @Autowired
    private final IPeriodService iPeriodService;

    @Autowired
    private final UserMatterMapper userMatterMapper;

    @GetMapping(FebsConstant.VIEW_PREFIX + "matter")
    public String matterIndex() {
        //System.err.println("matterIndex");
        return FebsUtil.view("matter/matter");
    }

    @GetMapping("matter/flush")
    @ResponseBody
    @ControllerEndpoint(operation = "定时任务", exceptionMessage = "执行失败")
    public FebsResponse flushMatter() throws Exception {
        AlarmTask alarmTask = new AlarmTask(remindService, matterService, iCycleService, iPeriodService);
        alarmTask.afterPropertiesSet();
        return new FebsResponse().success();
    }

    @GetMapping("matter")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse getAllMatters(HttpServletRequest request, Matter matter) {
        /*List<Matter> matters = matterService.findMatters(matter);
        //未完成
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        //已完成
        QueryWrapper<UserMatter> queryWrapper1 = new QueryWrapper<>();
        if (matters.size() > 0) {
            matters.forEach(matterDao -> {
                queryWrapper.eq("Finish", 1);
                queryWrapper1.eq("Finish", 0);
                queryWrapper.eq("MATTER_ID", matterDao.getMatterId());
                queryWrapper1.eq("MATTER_ID", matterDao.getMatterId());
                List<UserMatter> noOver = userMatterMapper.selectList(queryWrapper);
                List<UserMatter> over = userMatterMapper.selectList(queryWrapper1);
                if (noOver.size() > 0) {
                    matterDao.setNoOver(noOver.size());
                }
                if (over.size() > 0) {
                    matterDao.setOver(over.size());
                }
                queryWrapper.clear();
                queryWrapper1.clear();
            });
        }*/
        if (matter.getFinish() == null) {
            matter.setFinish(0);
        } else if ("2".equals(matter.getFinish()) || matter.getFinish() == 2) {
            matter.setFinish(null);
        }
        System.err.println("时间轴:" + matter);
        HttpSession session = request.getSession();
        String line = session.getAttribute("userId").toString();
        matter.setLongUserId(Long.valueOf(line));
        matter.setIsOpen(0);
        matterService.findMatters(matter).forEach(dao -> {
            System.err.println(dao.getMatterName() + "," + dao.getCreateTimeStr() + "," + dao.getEndStr() + "," + dao.getActuallyTime());
        });
        return new FebsResponse().success().data(matterService.findMatters(matter));
    }


    @GetMapping("matter/list")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterList(QueryRequest request, Matter matter, HttpServletRequest httpRequest) {
        //System.err.println("MatterController:start...");
        // System.err.println("MatterController:" + matter);
        HttpSession session = httpRequest.getSession();
        matter.setDeptId((Long) session.getAttribute("deptId"));
        Map<String, Object> dataTable = getDataTable(this.matterService.findMatters(request, matter));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("matter/list/{deptId}")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterListByDeptId(QueryRequest request, @PathVariable("deptId") Long deptId) {
        Matter matter = new Matter();
        matter.setDeptId(deptId);
        // System.err.println("MatterController:start...");
        Map<String, Object> dataTable = getDataTable(this.matterService.findMatters(request, matter));
        // System.err.println("MatterController:" + matter);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("matter/listForOne")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterListByUserId(QueryRequest request, Matter matter, HttpServletRequest httpServletRequest) {
        matter.setLongUserId((Long) httpServletRequest.getSession().getAttribute("userId"));
        matter.setIsOpen(0);
        if (matter.getFinish() == null) {
            matter.setFinish(0);
        } else if (matter.getFinish() == 2) {
            matter.setFinish(null);
        }
        //matter.setFinish(0);
        System.err.println(matter);
        // System.err.println("MatterController:start...");
        Map<String, Object> dataTable = getDataTable(this.matterService.findMattersForOne(request, matter));
        // System.err.println("MatterController:" + matter);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增事项", exceptionMessage = "新增事项失败")
    @PostMapping("matter/add")
    @ResponseBody
    @RequiresPermissions("matter:view")
    @Valid
    public FebsResponse addMatter(@Valid Matter matter, BindingResult BR) throws ParseException {
        System.err.println("addMatter:" + matter);
        //System.err.println(matter);
        this.matterService.createMatter(matter);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除事项", exceptionMessage = "删除事项失败")
    @GetMapping("matter/delete")
    @ResponseBody
    //@RequiresPermissions("matter:delete")
    public FebsResponse deleteMatter(Matter matter) {
        System.err.println("deleteMatter(Matter matter)");
        this.matterService.deleteMatter(matter);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除事项", exceptionMessage = "删除事项失败")
    @GetMapping("matter/delete/{matterId}")
    @ResponseBody
    // @RequiresPermissions("matter:delete")
    public FebsResponse deleteMatterById(@PathVariable("matterId") Long matterId) {
        //System.err.println("MatterController:deleteMatterById:"+matterId);
        System.err.println("deleteMatterById(@PathVariable(\"matterId\") Long matterId)");
        this.matterService.deleteMatterById(matterId);
        return new FebsResponse().success();
    }

    //@GetMapping("matter/delete/{matterIds}")
    //@RequiresPermissions("user:delete")
    @ControllerEndpoint(operation = "删除用户", exceptionMessage = "删除用户失败")
    public FebsResponse deleteMatters(@NotBlank(message = "{required}") @PathVariable Long[] matterIds) {
        /*
         * String[] ids = userIds.split(StringPool.COMMA);
         * this.userService.deleteUsers(ids);
         * return new FebsResponse().success();
         *
         */
        //System.err.println(matterIds);
        for (Long maid : matterIds) {
            matterService.deleteMatterById(maid);
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改事项", exceptionMessage = "修改事项失败")
    @PostMapping("matter/update")
    @ResponseBody
    //@RequiresPermissions("matter:update")
    public FebsResponse updateMatter(Matter matter) throws ParseException {
        System.err.println("updateMatter:" + matter);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //if (matter.getCycleIdStr().equals("")) {
        if ("".equals(matter.getCycleIdStr())) {
            matter.setCycleId(0l);
        } else {
            matter.setCycleId(Long.valueOf(matter.getCycleIdStr()));
        }
        matter.setMatterOpen(simpleDateFormat.parse(matter.getMatterOpenStr()));
        matter.setEnd(simpleDateFormat.parse(matter.getEndStr()));
        this.matterService.updateMatter(matter);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改提醒时间", exceptionMessage = "修改提醒时间失败")
    @GetMapping("matter/reminds/{matterId}")
    @ResponseBody
    //@RequiresPermissions("matter:update")
    public FebsResponse getMatterRemind(@PathVariable("matterId") Long matterId) {
        //System.err.println("updateMatter:" + matter);
        System.err.println("matterId:" + matterId);
        Remind remind = new Remind();
        remind.setMatterId(matterId);
        System.err.println("updateMatterRemind:" + remindService.findReminds(remind));
        return new FebsResponse().success().data(remindService.findReminds(remind));
    }

    /**
     * 新增提醒时间
     *
     * @param value
     * @return
     * @throws ParseException
     */
    @ControllerEndpoint(operation = "新增提醒时间", exceptionMessage = "新增提醒时间失败")
    @GetMapping("matter/remindAdd/{value}")
    @ResponseBody
    public FebsResponse addMatterRemind(@PathVariable("value") String value) throws ParseException {
        return new FebsResponse().success().data(remindService.createRemind(value));
    }

    @ControllerEndpoint(operation = "修改完成状态", exceptionMessage = "修改完成状态失败")
    @GetMapping("matter/finish/{value}")
    @ResponseBody
    public FebsResponse finishMatter(@PathVariable("value") String value) {
        matterService.finishMatter(value);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改完成状态", exceptionMessage = "修改完成状态失败")
    @GetMapping("matter/finishNo/{value}")
    @ResponseBody
    public FebsResponse finishMatterNo(@PathVariable("value") String value) {
        matterService.finishMatterNo(value);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改完成状态", exceptionMessage = "修改完成状态失败")
    @GetMapping("matter/finishCycle/{matterId}")
    @ResponseBody
    public FebsResponse finishMatterCycle(@PathVariable("matterId") Long matterId, HttpServletRequest request) {
        UserMatter userMatter = new UserMatter();
        userMatter.setMatterId(matterId);
        Long userId = (Long) request.getSession().getAttribute("userId");
        userMatter.setUserId(userId);
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("USER_ID", userId);
        queryWrapper.eq("MATTER_ID", matterId);
        System.err.println(userMatter);
        UserMatter dao = userMatterMapper.selectOne(queryWrapper);
        if (dao.getFinish() == 1) {
            dao.setFinish(0);
        } else if (dao.getFinish() == 0) {
            dao.setFinish(1);
            dao.setActuallyTime(new Date());
        }
        userMatterMapper.updateById(dao);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改提醒时间", exceptionMessage = "修改提醒时间失败")
    @PostMapping("matter/remind/update")
    @ResponseBody
    public FebsResponse updateMatterRemind(Matter matter) {
        updateRemind(matter);
        return new FebsResponse().success();
    }

    private void updateRemind(Matter matter) {
        //查处原数据
        List<Matter> matters = matterService.findMatters(matter);
        matters.forEach(dao -> {
            System.err.println("updateRemind:" + dao);
        });
        Matter matterDao = matters.get(0);
        //得到修改前后的remindId
        String before = matter.getRemindId();
        String later = matterDao.getRemindId();
        //处理数据 对比不同
        String[] allBefore = before.split(",");
        String[] allLater = later.split(",");
        List<String> listBefore = Arrays.asList(allBefore);
        List<String> diff = new ArrayList<>();
        for (String t : allLater
        ) {
            if (!listBefore.contains(t)) {
                diff.add(t);
                System.err.println(t);
            }
        }
        //处理得到的不同Id
        Remind remind = new Remind();
        for (String del : diff
        ) {
            Long remindId = Long.valueOf(del);
            remind.setRemindId(remindId);
            remindService.deleteRemindByRemindId(remind);
        }
    }

    @ControllerEndpoint(operation = "修改提醒时间", exceptionMessage = "修改提醒时间失败")
    @GetMapping("matter/remindDelete/{matterId}")
    @ResponseBody
    public FebsResponse deleteMatterRemind(@PathVariable("matterId") Long matterId) {
        Remind remind = new Remind();
        remind.setMatterId(matterId);
        remindService.deleteRemind(remind);
        return new FebsResponse().success();
    }


    @ControllerEndpoint(operation = "修改Matter", exceptionMessage = "导出Excel失败")
    @PostMapping("matter/excel")
    @ResponseBody
    @RequiresPermissions("matter:export")
    public void export(QueryRequest queryRequest, Matter matter, HttpServletResponse response) {
        List<Matter> matters = this.matterService.findMatters(queryRequest, matter).getRecords();
        ExcelKit.$Export(Matter.class, response).downXlsx(matters, false);
    }

    @GetMapping("matter/calendar")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse getAllMatterToCalendar(HttpServletRequest request, Matter matter) {
        matter.setFinish(0);
        matter.setLongUserId((Long) request.getSession().getAttribute("userId"));
        matter.setIsOpen(0);
        List<Matter> matters = matterService.findMatters(matter);
        List<CalendarObject> calendarObjects = new ArrayList<>();
        Map<String, String[]> map = new HashMap<>();
        matters.forEach(dao -> {
            CalendarObject calendarObject = new CalendarObject();
            calendarObject.setName(dao.getMatterOpenStr());
            String zt = null;
            if (dao.getImportantOne() == 1 && dao.getUrgentOne() == 1) {
                zt = "l1";
            } else if (dao.getImportantOne() == 1 && dao.getUrgentOne() == 0) {
                zt = "l2";
            } else if (dao.getImportantOne() == 0 && dao.getUrgentOne() == 1) {
                zt = "l3";
            } else if (dao.getImportantOne() == 0 && dao.getUrgentOne() == 0) {
                zt = "l4";
            }
            calendarObject.setColour(zt);
            calendarObjects.add(calendarObject);
        });
        return new FebsResponse().success().data(test(calendarObjects));
    }

    private String str = null;

    private List<CalendarObject> test(List<CalendarObject> calendarObjects) {
        calendarObjects.forEach(calendarObject -> {
            str = calendarObject.getColour();
            calendarObjects.forEach(calendarObject_ -> {
                if (calendarObject.getName().equals(calendarObject_.getName())) {
                    str = str + "," + calendarObject_.getColour();
                    calendarObject.setColour(str);
                }
                String[] strs = calendarObject.getColour().split(",");
                List<String> list = new ArrayList();
                //遍历数组往集合里存元素
                for (int i = 0; i < strs.length; i++) {
                    //如果集合里面没有相同的元素才往里存
                    if (!list.contains(strs[i])) {
                        list.add(strs[i]);
                    }
                }
                //toArray()方法会返回一个包含集合所有元素的Object类型数组
                Object[] newStrs = list.toArray();
                calendarObject.setValue(newStrs);
            });
        });
        calendarObjects.forEach(d -> {
            d.setColour(null);
            System.err.println(d);
        });
        return calendarObjects;
    }

}
