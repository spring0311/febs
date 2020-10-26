package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.system.entity.*;
import cc.mrbird.febs.system.mapper.TeamMapper;
import cc.mrbird.febs.system.mapper.UserMatterMapper;
import cc.mrbird.febs.system.service.*;

import cc.mrbird.febs.task.AlarmTaskTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @Autowired
    private final IUserDataPermissionService iUserDataPermissionService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "matter")
    public String matterIndex() {
        //System.err.println("matterIndex");
        return FebsUtil.view("matter/matter");
    }

    @GetMapping("matter/flush")
    @ResponseBody
    @ControllerEndpoint(operation = "定时任务", exceptionMessage = "执行失败")
    public FebsResponse flushMatter() throws Exception {
        AlarmTaskTime alarmTaskTime = new AlarmTaskTime(remindService, matterService, iCycleService, iPeriodService, iUserDataPermissionService);
        alarmTaskTime.afterPropertiesSet();
        return new FebsResponse().success();
    }

    @GetMapping("matter")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse getAllMatters(HttpServletRequest request, Matter matter) {
        if (matter.getFinish() == null || "0".equals(matter.getFinish()) || matter.getFinish() == 0) {
            matter.setFinish(0);
        } else if ("2".equals(matter.getFinish()) || matter.getFinish() == 2) {
            matter.setFinish(null);
        } else if ("1".equals(matter.getFinish()) || matter.getFinish() == 1) {
            matter.setFinish(1);
        }
        System.err.println("时间轴:" + matter);
        HttpSession session = request.getSession();
        String line = session.getAttribute("userId").toString();
        matter.setLongUserId(Long.valueOf(line));
        matter.setIsOpen(0);
        matter.setIsPatriarch(0);
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
        matter.setIsPatriarch(1);
        Map<String, Object> dataTable = getDataTable(this.matterService.findMatters(request, matter));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("matter/listOut")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterListPOut(QueryRequest request, Matter matter, HttpServletRequest httpRequest) {
        //System.err.println("MatterController:start...");
        // System.err.println("MatterController:" + matter);
        Matter select = new Matter();
        select.setPatriarchId(Long.valueOf(httpRequest.getSession().getAttribute("insertMatterId").toString()));
        select.setIsPatriarch(0);
        Map<String, Object> dataTable = getDataTable(this.matterService.findMatterOut(request, select));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("matter/indexList")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse indexList(QueryRequest request, Matter matter, HttpServletRequest httpRequest) {
        matter.setDeptId(Long.valueOf(httpRequest.getSession().getAttribute("deptId").toString()));
        matter.setIsPatriarch(0);
        matter.setIsOpen(0);
        Map<String, Object> dataTable = getDataTable(this.matterService.findMatterOut(request, matter));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("matter/list/{deptId}")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterListByDeptId(QueryRequest request, @PathVariable("deptId") Long deptId) {
        Matter matter = new Matter();
        matter.setDeptId(deptId);
        matter.setIsPatriarch(1);
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
        System.err.println(matter);
        if (matter.getFinish() == null) {
            matter.setFinish(0);
        } else if (matter.getFinish() == 2) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            matter.setFinish(null);
        }
        matter.setIsPatriarch(0);
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

    @ControllerEndpoint(operation = "新增事项", exceptionMessage = "新增事项失败")
    @PostMapping("matter/addOne")
    @ResponseBody
    @RequiresPermissions("matter:view")
    @Valid
    public FebsResponse addMatterOne(@Valid Matter matter, HttpServletRequest request) throws ParseException {
        matter.setLongUserId(Long.valueOf(request.getSession().getAttribute("userId").toString()));
        matter.setDeptId(0l);
        matter.setCreateTime(new Date());
        matter.setIsOpen(0);
        System.err.println("addMatterOne:" + matter);
        this.matterService.createMatterOne(matter);
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
        if (matter.getCycleIdStr() != null) {
            changePeriod(matter);
        }
        if (matter.getMatterOpenStr() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date open = simpleDateFormat.parse(matter.getMatterOpenStr());
            Date end = simpleDateFormat.parse(matter.getEndStr());
            matter.setEnd(end);
            matter.setMatterOpen(open);
        }
        this.matterService.updateMatter(matter);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "生成事项日程", exceptionMessage = "生成事项日程失败")
    @PostMapping("matter/addOut")
    @ResponseBody
    public FebsResponse addOutMatter(Matter matter) {
        System.err.println("addOutMatter:" + matter);
        /**
         * 父事项
         */
        Matter matterFather = matterService.getById(matter.getMatterId());
        String[] periodIds = matter.getCycleIdStr().split(",");
        List<String> listUserId = addOut(null, matter.getUserId(), matter.getTeamId());
        /**
         * 遍历周期ID生成新事项
         */
        for (String periodId : periodIds) {
            Period periodDao = iPeriodService.getById(Long.valueOf(periodId));
            matterFather.setIsPatriarch(0);
            matterFather.setPatriarchId(matter.getMatterId());
            matterFather.setMatterOpen(periodDao.getPeriodOpen());
            matterFather.setEnd(periodDao.getPeriodEnd());
            matterFather.setCreateTime(new Date());
            matterFather.setCycleId(periodDao.getPeriodId());
            matterFather.setForEach(null);
            matterFather.setPeriod(null);
            matterFather.setMatterId(null);
            /**
             * 是否有生成过的重复事项
             */
            QueryWrapper<Matter> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("CYCLE_ID", periodDao.getPeriodId());
            queryWrapper.eq("PATRIARCH_ID", matter.getMatterId());
            List<Matter> isCovers = matterService.list(queryWrapper);
            //!!!!! 存
            //!!!!! 查ID
            //有生成过的 覆盖
            if (isCovers.size() > 0 && matter.getCover() == 0) {
                isCovers.forEach(matter1 -> {
                    matter1.setIsPatriarch(0);
                    matter1.setPatriarchId(matter.getMatterId());
                    matter1.setMatterOpen(periodDao.getPeriodOpen());
                    matter1.setEnd(periodDao.getPeriodEnd());
                    matter1.setCreateTime(new Date());
                    matter1.setCycleId(periodDao.getPeriodId());
                    matter1.setMatterName(matterFather.getMatterName());
                    matter1.setMatterText(matterFather.getMatterText());
                    matterService.saveOrUpdate(matter1);
                    delRemind(matter1);
                    matterService.copyReminds(periodDao, matter1.getMatterId());
                    listUserId.forEach(id -> {
                        QueryWrapper<UserMatter> userMatterQueryWrapper = new QueryWrapper<>();
                        userMatterQueryWrapper.eq("MATTER_ID", matter1.getMatterId());
                        userMatterQueryWrapper.eq("USER_ID", Long.valueOf(id));
                        List<UserMatter> list = userMatterMapper.selectList(userMatterQueryWrapper);
                        if (list.size() == 0) {
                            UserMatter userMatter = new UserMatter();
                            userMatter.setMatterId(matter1.getMatterId());
                            userMatter.setUserId(Long.valueOf(id));
                            userMatter.setImportantOne(matter1.getImportant());
                            userMatter.setUrgentOne(matter1.getUrgent());
                            userMatter.setFinish(0);
                            userMatter.setIsRemind(0);
                            userMatterMapper.insert(userMatter);
                        }
                    });
                });
            }
            //其他
            else {
                matterService.saveOrUpdate(matterFather);
                Long newId = matterService.maxMatterId();
                matterService.copyReminds(periodDao, newId);
                listUserId.forEach(uId -> {
                    UserMatter userMatter = new UserMatter();
                    userMatter.setIsRemind(0);
                    userMatter.setFinish(0);
                    userMatter.setImportantOne(matterFather.getImportant());
                    userMatter.setUrgentOne(matterFather.getUrgentOne());
                    userMatter.setMatterId(newId);
                    userMatter.setUserId(Long.valueOf(uId));
                    userMatterMapper.insert(userMatter);
                });
            }
        }
        return new FebsResponse().success();
    }

    private final TeamMapper teamMapper;

    private void delRemind(Matter matter) {
        QueryWrapper<Remind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MATTER_ID", matter.getMatterId());
        remindService.remove(queryWrapper);
    }

    private List<String> addOut(Long newId, String userId, String teamId) {
        if (!"".equals(teamId)) {
            String[] teamIds = teamId.split(",");
            for (String daoId : teamIds) {
                Team team = new Team();
                Long id = Long.valueOf(daoId);
                team.setTeamId(id);
                team = teamMapper.findTeams(team).get(0);
                userId = userId + "," + team.getUserId();
            }
        }
        String[] userIdS = userId.split(",");
        List<String> list = Arrays.asList(userIdS);
        Set<String> set = new HashSet(list);
        String[] ret = (String[]) set.toArray(new String[0]);
        List<String> listUserId = Arrays.asList(ret);
        return listUserId;
    }

    private void changePeriod(Matter matter) {
        String[] periodIds = matter.getCycleIdStr().split(",");
        QueryWrapper<UserDataPermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MATTER_ID", matter.getMatterId());
        iUserDataPermissionService.remove(queryWrapper);
        UserDataPermission userDataPermission = new UserDataPermission();
        userDataPermission.setMatterId(matter.getMatterId());
        for (String periodId : periodIds) {
            userDataPermission.setUserId(System.currentTimeMillis());
            userDataPermission.setDeptId(System.currentTimeMillis());
            userDataPermission.setPeriodId(Long.valueOf(periodId));
            iUserDataPermissionService.saveOrUpdate(userDataPermission);
        }
    }

    @ControllerEndpoint(operation = "修改提醒时间", exceptionMessage = "修改提醒时间失败")
    @GetMapping("matter/reminds/{matterId}")
    @ResponseBody
    //@RequiresPermissions("matter:update")
    public FebsResponse getMatterRemind(@PathVariable("matterId") Long matterId) {
        //System.err.println("updateMatter:" + matter);
        Remind remind = new Remind();
        remind.setMatterId(matterId);
        remind.setUserBy(0);
        return new FebsResponse().success().data(remindService.findReminds(remind));
    }

    @ControllerEndpoint(operation = "修改提醒时间", exceptionMessage = "修改提醒时间失败")
    @GetMapping("matter/reminds")
    @ResponseBody
    //@RequiresPermissions("matter:update")
    public FebsResponse getMatterRemindOne(Remind remind, QueryRequest request) {
        //System.err.println("updateMatter:" + matter);
        System.err.println("getMatterRemindOne:" + remind);
        System.err.println("getMatterRemindOne::" + remind);
        QueryWrapper<Remind> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(remind);
        Page<Remind> page = new Page<>(request.getPageNum(), request.getPageSize());
        List<Remind> reminds = remindService.list(queryWrapper);
        page.setSize(reminds.size());
        page.setRecords(reminds);
        Map<String, Object> dataTable = getDataTable(page);
        return new FebsResponse().success().data(dataTable);
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
    public FebsResponse addMatterRemind(@PathVariable("value") String value, HttpServletRequest request, Model model) throws ParseException {
        return new FebsResponse().success().data(remindService.createRemind(value, request, model));
    }


    @ControllerEndpoint(operation = "修改开启状态", exceptionMessage = "修改开启状态失败")
    @GetMapping("matter/changeOpen")
    @ResponseBody
    public FebsResponse changeMaterOpen(Matter matter) {
        if (matter.getMatterIds() != null) {
            String[] strs = matter.getMatterIds().split(",");
            Long[] Longs = getLongs(strs);
            QueryWrapper<Matter> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("MATTER_ID", Longs);
            Matter change = new Matter();
            change.setIsOpen(1);
            matterService.saveOrUpdate(change, queryWrapper);
        } else {
            matterService.saveOrUpdate(matter);
        }
        return new FebsResponse().success();
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

    @ControllerEndpoint(operation = "事项日程单", exceptionMessage = "查看事项日程单失败")
    @GetMapping("matter/insertMatterId")
    @ResponseBody
    public FebsResponse insertMatterId(HttpServletRequest request, Matter matter) {
        request.getSession().setAttribute("insertMatterId", matter.getMatterId());
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

    @ControllerEndpoint(operation = "修改信息", exceptionMessage = "修改信息失败")
    @PostMapping("matter/changeUserMatter")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse changeUserMatter(UserMatter userMatter) {
        userMatterMapper.updateById(userMatter);
        return new FebsResponse().success();
    }

    @GetMapping("matter/calendar")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse getAllMatterToCalendar(HttpServletRequest request, Matter matter) {
        System.err.println("getAllMatterToCalendar!!!!!!!!!!:" + matter);
        matter.setLongUserId((Long) request.getSession().getAttribute("userId"));
        matter.setIsPatriarch(0);
        matter.setIsOpen(0);
        if (matter.getFinish() == null) {
            matter.setFinish(0);
        } else if ("2".equals(matter.getFinish()) || matter.getFinish() == 2) {
            matter.setFinish(null);
        }
        List<Matter> matters = matterService.findMatters(matter);
        List<CalendarObject> calendarObjects = new ArrayList<>();
        //Map<String, String[]> map = new HashMap<>();
        matters.forEach(dao -> {
            CalendarObject calendarObject = new CalendarObject();
            if ("1".equals(matter.getStatus()) || matter.getStatus() == null) {
                calendarObject.setName(dao.getMatterOpenStr());
            } else if ("2".equals(matter.getStatus())) {
                calendarObject.setName(dao.getEndStr());
            }
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

    static Long[] getLongs(String[] stringArray) {
        List<Long> list = new ArrayList<>();
        for (String str : stringArray) {
            try {
                list.add(Long.parseLong(str));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        Long[] longArray = list.toArray(new Long[list.size()]);
        return longArray;
    }

    private String[] getUserIds(Matter matter) {
        String userIds = matter.getUserId();
        String teamIds = matter.getTeamId();
        String[] teamIdS = teamIds.split(",");
        for (String teamId : teamIdS
        ) {
            Team team = new Team();
            Long id = Long.valueOf(teamId);
            team.setTeamId(id);
            team = teamMapper.findTeams(team).get(0);
            userIds = userIds + "," + team.getUserId();
        }
        //得到用户Id去重复 转换为Set
        String[] userIdS = userIds.split(",");
        List<String> list = Arrays.asList(userIdS);
        Set<String> set = new HashSet(list);
        String[] ret = (String[]) set.toArray(new String[0]);
        return ret;
    }

}
