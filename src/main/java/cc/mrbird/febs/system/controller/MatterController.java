package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import cc.mrbird.febs.system.entity.*;
import cc.mrbird.febs.system.mapper.TeamMapper;
import cc.mrbird.febs.system.mapper.UserMapper;
import cc.mrbird.febs.system.mapper.UserMatterMapper;
import cc.mrbird.febs.system.service.*;

import cc.mrbird.febs.task.AlarmTaskTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.ListMultimap;
import com.wuwenze.poi.ExcelKit;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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

    private StringBuilder str = new StringBuilder("");

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

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private AlarmTaskTime alarmTaskTime;

    @GetMapping("matter/listUserIndex")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterIndexUser(QueryRequest request, Matter matter) {
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MATTER_ID", matter.getMatterId());
        List<UserMatter> userMatters = userMatterMapper.selectList(queryWrapper);
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        page.setSearchCount(false);
        page.setTotal(userMatters.size());
        SortUtil.handlePageSort(request, page, "userId", FebsConstant.ORDER_ASC, false);
        Map<String, Object> dataTable = getDataTable(this.userMapper.findUserByMatterId(page, matter.getMatterId()));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("matter/listOutIndex")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterIndexListPOut(QueryRequest request, Matter matter, HttpServletRequest httpRequest) {
        matter.setIsOpen(0);
        matter.setIsPatriarch(0);
        QueryWrapper<Matter> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(matter);
        Page<Matter> page = new Page<>(request.getPageNum(), request.getPageSize());
        page = this.matterService.page(page, queryWrapper);
        SortUtil.handlePageSort(request, page, "end", FebsConstant.ORDER_ASC, true);
        page = this.matterService.page(page, queryWrapper);
        List<Matter> list = page.getRecords();
        if (list.size() > 0)
            list.forEach(dao -> {
                dao.setOver(isFinishUnm(dao, 1));
                dao.setNoOver(isFinishUnm(dao, 0));
            });
        page.setRecords(list);
        Map<String, Object> dataTable = getDataTable(page);
        return new FebsResponse().success().data(dataTable);
    }

    /**
     * 子事项 完成情况
     *
     * @param matter
     * @param finish
     * @return
     */
    private Integer isFinishUnm(Matter matter, Integer finish) {
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MATTER_ID", matter.getMatterId());
        queryWrapper.eq("Finish", finish);
        List<UserMatter> list = userMatterMapper.selectList(queryWrapper);
        Integer unm = list.size();
        return unm;
    }

    @GetMapping("matter/echarts")
    @ResponseBody
    @ControllerEndpoint(operation = "饼图统计", exceptionMessage = "执行失败")
    public FebsResponse getEcharts(Matter matter, HttpServletRequest request) {
        HttpSession session = request.getSession();
        matter.setDeptId(Long.valueOf(session.getAttribute("deptId").toString()));
        matter.setIsOpen(0);
        matter.setIsPatriarch(1);
        QueryWrapper<Matter> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(matter);
        List<Echarts> list = new ArrayList<>();
        List<Matter> matters = matterService.list(queryWrapper);
        matters.forEach(dao -> {
            //查询未完成人数
            Echarts echarts = new Echarts();
            echarts.setName(dao.getMatterName());
            echarts.setValue(getNoOverNum(dao));
            if (getNoOverNum(dao) != 0 && getNoOverNum(dao) != null)
                list.add(echarts);
        });
        return new FebsResponse().success().data(list);
    }

    private Integer num = 0;

    /**
     * 父事项参数
     *
     * @param matter
     * @return
     */
    private Integer getNoOverNum(Matter matter) {
        num = 0;
        QueryWrapper<Matter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PATRIARCH_ID", matter.getMatterId());
        queryWrapper.eq("IS_OPEN", 0);
        List<Matter> matters = matterService.list(queryWrapper);
        if (matters.size() > 0) {
            matters.forEach(dao -> {
                num = num + getUserMatterNum(dao);
            });
        }
        return num;
    }

    /**
     * 子事项
     *
     * @param matter
     * @return
     */
    private Integer getUserMatterNum(Matter matter) {
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MATTER_ID", matter.getMatterId());
        queryWrapper.eq("Finish", 0);
        List<UserMatter> list = userMatterMapper.selectList(queryWrapper);
        if (list.size() > 0) {
            return list.size();
        }
        return 0;
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "matter")
    public String matterIndex() {
        return FebsUtil.view("matter/matter");
    }

    @GetMapping("matter/flush")
    @ResponseBody
    @ControllerEndpoint(operation = "定时任务", exceptionMessage = "执行失败")
    public FebsResponse flushMatter() throws Exception {
        forEachOne();
        cycleMatter();
        endTimeRemind();
        selectMatterRemind();
        selectMatterRemindByOne();
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
        HttpSession session = request.getSession();
        String line = session.getAttribute("userId").toString();
        matter.setLongUserId(Long.valueOf(line));
        matter.setIsOpen(0);
        matter.setIsPatriarch(0);
        return new FebsResponse().success().data(matterService.findMatters(matter));
    }


    @GetMapping("matter/list")
    @ResponseBody
    @RequiresPermissions("matter:view")
    public FebsResponse matterList(QueryRequest request, Matter matter, HttpServletRequest httpRequest) {
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
        Map<String, Object> dataTable = getDataTable(this.matterService.findMatters(request, matter));
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
        matter.setIsPatriarch(0);
        Map<String, Object> dataTable = getDataTable(this.matterService.findMattersForOne(request, matter));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增事项", exceptionMessage = "新增事项失败")
    @PostMapping("matter/add")
    @ResponseBody
    @RequiresPermissions("matter:view")
    @Valid
    public FebsResponse addMatter(@Valid Matter matter, BindingResult BR) throws ParseException {
        this.matterService.createMatter(matter);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "新增事项", exceptionMessage = "新增事项失败")
    @GetMapping("matter/addOne")
    @ResponseBody
    @RequiresPermissions("matter:view")
    @Valid
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public FebsResponse addMatterOne(@Valid Matter matter, HttpServletRequest request) throws ParseException {
        matter.setLongUserId(Long.valueOf(request.getSession().getAttribute("userId").toString()));
        matter.setDeptId(0l);
        matter.setCreateTime(new Date());
        matter.setIsOpen(0);
        this.matterService.createMatterOne(matter);
        Long matterId = this.matterService.maxMatterId();
        return new FebsResponse().success().data(matterId);
    }

    @ControllerEndpoint(operation = "删除事项", exceptionMessage = "删除事项失败")
    @GetMapping("matter/delete")
    @ResponseBody
    //@RequiresPermissions("matter:delete")
    public FebsResponse deleteMatter(Matter matter) {
        this.matterService.deleteMatter(matter);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除事项", exceptionMessage = "删除事项失败")
    @GetMapping("matter/delete/{matterId}")
    @ResponseBody
    // @RequiresPermissions("matter:delete")
    public FebsResponse deleteMatterById(@PathVariable("matterId") Long matterId) {
        this.matterService.deleteMatterById(matterId);
        return new FebsResponse().success();
    }

    //@GetMapping("matter/delete/{matterIds}")
    //@RequiresPermissions("user:delete")
    @ControllerEndpoint(operation = "删除用户", exceptionMessage = "删除用户失败")
    public FebsResponse deleteMatters(@NotBlank(message = "{required}") @PathVariable Long[] matterIds) {
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
        Long time = System.currentTimeMillis();
        for (int i = 0; i < periodIds.length; i++) {
            time += 1;
            userDataPermission.setDeptId(time);
            userDataPermission.setUserId(time);
            userDataPermission.setPeriodId(Long.valueOf(periodIds[i]));
            iUserDataPermissionService.saveOrUpdate(userDataPermission);
        }
        /*for (String periodId : periodIds) {
            userDataPermission.setUserId(System.currentTimeMillis());
            userDataPermission.setDeptId(System.currentTimeMillis());
            userDataPermission.setPeriodId(Long.valueOf(periodId));
            iUserDataPermissionService.saveOrUpdate(userDataPermission);
        }*/
    }

    @ControllerEndpoint(operation = "修改提醒时间", exceptionMessage = "修改提醒时间失败")
    @GetMapping("matter/reminds/{matterId}")
    @ResponseBody
    //@RequiresPermissions("matter:update")
    public FebsResponse getMatterRemind(@PathVariable("matterId") Long matterId) {
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
            if (dao.getImportantOne() == 1 && dao.getUrgentOne() == 1) {
                calendarObject.setColour("l1");
            } else if (dao.getImportantOne() == 1 && dao.getUrgentOne() == 0) {
                calendarObject.setColour("l2");
            } else if (dao.getImportantOne() == 0 && dao.getUrgentOne() == 1) {
                calendarObject.setColour("l3");
            } else if (dao.getImportantOne() == 0 && dao.getUrgentOne() == 0) {
                calendarObject.setColour("l4");
            }
            calendarObjects.add(calendarObject);
        });
        return new FebsResponse().success().data(viewDispose(calendarObjects));
    }

    private int i = 0;

    private List<CalendarObject> viewDispose(List<CalendarObject> calendarObjects) {
        calendarObjects.forEach(calendarObject -> {
            str = str.delete(0, str.length());
            str = str.append(calendarObject.getColour());
            i = 0;
            calendarObjects.forEach(calendarObject_ -> {
                if (calendarObject.getName().equals(calendarObject_.getName())) {
                    if (i > 0)
                        return;
                    if (calendarObject_.getColour().length() > 3) {
                        calendarObject.setColour(calendarObject_.getColour());
                        i++;
                    } else {
                        str = str.append("," + calendarObject_.getColour());
                        calendarObject.setColour(str.toString());
                    }
                }
            });
        });
        calendarObjects.forEach(d -> {
            String[] strs = d.getColour().split(",");
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
            d.setValue(newStrs);
            d.setColour(null);
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

    /**
     * 调用定时
     * !!!!!!!!!!!!!!!
     * !!!!!!!!!!!!!!!!!
     * !!!!!!!!!!!!!!!!!!
     * !!!!!!!!!!!!!!!!
     */

    private final IRemindService iRemindService;


    @Value("${day.value}")
    private int addDay;


    /**
     * 提醒时间
     * <p>
     * 定时任务
     * 提醒时间到来当天触发条件
     * 修改为紧急事项
     * 之后提醒时间删除
     *
     * @throws ParseException
     */
    private void selectMatterRemind() throws ParseException {
        //得到当前时间的yyyy-MM-dd
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String dateStr = simpleDateFormat.format(date);
        QueryWrapper<Remind> remindQueryWrapper = new QueryWrapper<>();
        //不是用户创建
        remindQueryWrapper.eq("USER_BY", 0);
        //已激活
        remindQueryWrapper.eq("IS_ACTIVATE", 1);
        remindQueryWrapper.eq("REMIND_STR", dateStr);
        List<Remind> reminds = iRemindService.list(remindQueryWrapper);
        if (reminds.size() > 0) {
            reminds.forEach(remind -> {
                QueryWrapper<UserMatter> userMatterQueryWrapper = new QueryWrapper<>();
                userMatterQueryWrapper.eq("MATTER_ID", remind.getMatterId());
                userMatterQueryWrapper.eq("FINISH", 0);
                userMatterQueryWrapper.eq("IS_REMIND", 0);
                matterService.findUserMatters(userMatterQueryWrapper);
            });
        }
    }

    /**
     * @throws ParseException
     */
    private void selectMatterRemindByOne() throws ParseException {
        //得到当前时间的yyyy-MM-dd
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String dateStr = simpleDateFormat.format(date);
        QueryWrapper<Remind> remindQueryWrapper = new QueryWrapper<>();
        //不是用户创建
        remindQueryWrapper.eq("USER_BY", 1);
        //已激活
        remindQueryWrapper.eq("IS_ACTIVATE", 1);
        remindQueryWrapper.eq("REMIND_STR", dateStr);
        List<Remind> reminds = iRemindService.list(remindQueryWrapper);
        if (reminds.size() > 0) {
            reminds.forEach(remind -> {
                QueryWrapper<UserMatter> userMatterQueryWrapper = new QueryWrapper<>();
                userMatterQueryWrapper.eq("MATTER_ID", remind.getMatterId());
                userMatterQueryWrapper.eq("USER_ID", remind.getUserId());
                userMatterQueryWrapper.eq("FINISH", 0);
                userMatterQueryWrapper.eq("IS_REMIND", 1);
                matterService.findUserMatters(userMatterQueryWrapper);
            });
        }
    }

    /**
     * 结束时间
     * 结束时间提前15天设为紧急
     *
     * @throws ParseException
     */
    private void endTimeRemind() throws ParseException {
        String date = getDate();
        List<Long> matterIds = matterService.selectMatterIdsByEndTime(date);
        if (matterIds.size() != 0) {
            matterIds.forEach(matterId -> {
                matterService.updateMatterUrgent(matterId);
            });
        }
    }

    /**
     * 个人周期循环 月
     */
    private void forEachOne() throws ParseException {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -1);
        //往前推一个月
        Date before = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = simpleDateFormat.format(before);
        Matter matter = new Matter();
        matter.setMatterOpenStr(dateStr);
        matter.setForEach(1);
        matter.setIsOpen(0);
        matter.setIsPatriarch(0);
        //
        List<Matter> list = matterService.findMatters(matter);
        if (list.size() > 0) {
            list.forEach(dao -> {
                dao.setForEach(0);
                Long userId = Long.valueOf(dao.getUserId());
                Long oldId = dao.getMatterId();
                matterService.saveOrUpdate(dao);
                dao.setMatterId(null);
                dao.setMatterOpen(now);
                calendar.setTime(dao.getEnd());
                calendar.add(Calendar.MONTH, +1);
                dao.setEnd(calendar.getTime());
                dao.setForEach(1);
                matterService.saveOrUpdate(dao);
                Long newId = matterService.maxMatterId();
                matterService.userMatter(oldId, newId, userId);
            });
        }
    }


    /**
     * 周期定时
     */
    private void cycleMatter() {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String str = simpleDateFormat.format(now);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeLeft("OPEN_STR", str);
        List<Period> periods = iPeriodService.list(queryWrapper);
        if (periods.size() > 0) {
            periods.forEach(period -> {
                QueryWrapper<UserDataPermission> queryWrapperU = new QueryWrapper<>();
                queryWrapperU.eq("PERIOD_ID", period.getPeriodId());
                List<UserDataPermission> list = iUserDataPermissionService.list(queryWrapperU);
                if (list.size() > 0) {
                    list.forEach(dao -> {
                        //得到matter
                        Matter matter = matterService.getById(dao.getMatterId());
                        if (matter != null && matter.getIsOpen() == 0) {
                            Long oldId = matter.getMatterId();
                            matter.setIsPatriarch(0);
                            matter.setPatriarchId(matter.getMatterId());
                            matter.setMatterOpen(new Date());
                            try {
                                matter.setEnd(getEndTime(period));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            matter.setCreateTime(new Date());
                            matter.setCycleId(period.getPeriodId());
                            matter.setForEach(null);
                            matter.setPeriod(null);
                            matter.setMatterId(null);
                            //存储
                            matterService.saveOrUpdate(matter);
                            Long newId = matterService.maxMatterId();
                            //用户事项映射
                            matterService.copyUserMatter(oldId, newId);
                            //提醒时间
                            matterService.copyReminds(period, newId);
                        }
                    });
                }
            });
        }
    }

    /**
     * 得到日期加15
     *
     * @return 日期加15
     * @throws ParseException
     */
    private String getDate() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        Date date = new Date();
        /*String time = simpleDateFormat.format(date);
        date = simpleDateFormat.parse(time);*/
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, addDay);
        date = calendar.getTime();
        String end = simpleDateFormat.format(date);
        return end;
    }

    /**
     * 结束时间
     *
     * @param period
     * @return
     * @throws ParseException
     */
    private Date getEndTime(Period period) throws ParseException {
        SimpleDateFormat MMdd = new SimpleDateFormat("MM-dd");
        SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
        String year = yyyy.format(new Date());
        String mw = MMdd.format(period.getPeriodEnd());
        String endStr = year + "-" + mw;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date end = simpleDateFormat.parse(endStr);
        return end;
    }

}
