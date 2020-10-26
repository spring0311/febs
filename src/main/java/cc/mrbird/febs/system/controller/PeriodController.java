package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.DeptTree;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.PeriodTree;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.system.entity.Period;
import cc.mrbird.febs.system.entity.Remind;
import cc.mrbird.febs.system.service.IPeriodService;
import cc.mrbird.febs.system.service.IRemindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.plugins.JacksonSerializerConvention;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: weiZiHao
 * @create: 2020-09-10 13:35
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("period")
public class PeriodController extends BaseController {

    private final IPeriodService periodService;

    private final IRemindService remindService;

    @GetMapping("tree/test")
    @ControllerEndpoint(exceptionMessage = "获取部门树失败")
    public FebsResponse getDeptTree(Period period) throws FebsException {
        System.err.println("getDeptTree");
        List<DeptTree<Period>> periods = this.periodService.findPeriods(period);
        periods.forEach(periodDeptTree -> {
            System.err.println(periodDeptTree);
        });
        return new FebsResponse().success().data(periods);
    }

    @GetMapping("tree")
    @ControllerEndpoint(exceptionMessage = "获取周期表失败")
    public FebsResponse getPeriodTreeController(Period period, HttpServletRequest request) throws FebsException {
        period.setDeptId(Long.valueOf(request.getSession().getAttribute("deptId").toString()));
        System.err.println("getPeriodTree:" + period);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        List<Period> periods = new ArrayList<>();
        if (period.getCycleId() != null && period.getOrAnd() != null) {
            queryWrapper.eq("PARENT_ID", period.getCycleId());
            //修改 1013 1638
            queryWrapper.le("PERIOD_OPEN", new Date());
            //
        } else if (period.getCycleId() != null && period.getOrAnd() == null) {
            queryWrapper.eq("PARENT_ID", period.getCycleId());
        } else if (period.getOrAnd() != null) {
            Long[] longIds = getLong(period.getCycleIdStr());
            queryWrapper.in("PERIOD_ID", longIds);
        } else {
            period.setParentId(0l);
            queryWrapper.setEntity(period);
        }
        //修改 1013 1638
        queryWrapper.orderByDesc("PERIOD_OPEN");
        //
        periods = this.periodService.list(queryWrapper);
        List<PeriodTree> periodTrees = new ArrayList<>();
        if (period.getOrAnd() != null) {
            for (int i = 0; i < periods.size(); i++) {
                if (periods.get(i).getPeriodOpen().getTime() > System.currentTimeMillis()) {
                    periods.remove(i);
                    i--;
                }
            }
        }
        if (periods.size() != 0) {
            periodTrees = getPeriodTree(periods);
        }
        periodTrees.forEach(sys -> {
            System.err.println("sys:" + sys);
        });
        return new FebsResponse().success().data(periodTrees);
    }

    @GetMapping("list")
    @ControllerEndpoint(exceptionMessage = "获取周期列表失败")
    public FebsResponse periodList(Period period, QueryRequest request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession();
        period.setDeptId((Long) session.getAttribute("deptId"));
        period.setParentId((long) 0);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(period);
        Page<Period> page = new Page<>(request.getPageNum(), request.getPageSize());
        page = this.periodService.page(page, queryWrapper);
        Map<String, Object> dataTable = getDataTable(this.periodService.page(page, queryWrapper));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("listLittle")
    @ControllerEndpoint(exceptionMessage = "获取周期列表失败")
    public FebsResponse periodListLittle(Period period, QueryRequest request, HttpSession session) {
        period.setParentId((Long) session.getAttribute("periodId"));
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(period);
        queryWrapper.orderByAsc("PERIOD_OPEN");
        Page<Period> page = new Page<>(request.getPageNum(), request.getPageSize());
        //page = this.periodService.page(page, queryWrapper);
        List<Period> list = periodService.list(queryWrapper);
        if (list.size() > 0) {
            list.forEach(dao -> {
                String str = "";
                StringBuilder stringBuilder = new StringBuilder(str);
                QueryWrapper<Remind> remindQueryWrapper = new QueryWrapper<>();
                remindQueryWrapper.eq("IS_ACTIVATE", 0);
                remindQueryWrapper.eq("PERIOD_ID", dao.getPeriodId());
                remindQueryWrapper.orderByAsc("REMIND_TIME");
                List<Remind> reminds = remindService.list(remindQueryWrapper);
                if (reminds.size() > 0) {
                    reminds.forEach(remind -> {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
                        stringBuilder.append(simpleDateFormat.format(remind.getRemindTime()) + ",");
                    });
                }
                str = stringBuilder.toString();
                if (str.length() > 0) {
                    str = str.substring(0, str.length() - 1);
                }
                dao.setRemind(str);
            });
        }
        page.setSize(list.size());
        page.setRecords(list);
        Map<String, Object> dataTable = getDataTable(page);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("addOrUpdate")
    @ControllerEndpoint(exceptionMessage = "新增或修改周期失败")
    public FebsResponse addPeriod(Period period) {
        period.setCreateTime(new Date());
        period.setParentId(0l);
        this.periodService.saveOrUpdate(period);
        return new FebsResponse().success();
    }

    @GetMapping("showList")
    @ControllerEndpoint(exceptionMessage = "获取周期失败")
    public FebsResponse showList(HttpSession session, QueryRequest request) {
        String ids = (String) session.getAttribute("ids");
        if (ids == null) {
            return new FebsResponse().success().data(new HashMap<String, Object>());
        }
        Long[] longIds = getLong(ids);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("PERIOD_ID", longIds);
        queryWrapper.orderByDesc("PERIOD_OPEN");
        Page<Period> page = new Page<>(request.getPageNum(), request.getPageSize());
        //page = this.periodService.page(page, queryWrapper);
        List<Period> list = periodService.list(queryWrapper);
        if (list.size() > 0) {
            list.forEach(dao -> {
                String str = "";
                StringBuilder stringBuilder = new StringBuilder(str);
                QueryWrapper<Remind> remindQueryWrapper = new QueryWrapper<>();
                remindQueryWrapper.eq("IS_ACTIVATE", 0);
                remindQueryWrapper.eq("PERIOD_ID", dao.getPeriodId());
                List<Remind> reminds = remindService.list(remindQueryWrapper);
                if (reminds.size() > 0) {
                    reminds.forEach(remind -> {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
                        stringBuilder.append(simpleDateFormat.format(remind.getRemindTime()) + ",");
                    });
                }
                str = stringBuilder.toString();
                if (str.length() > 0) {
                    str = str.substring(0, str.length() - 1);
                    System.err.println("Str:" + str);
                }
                dao.setRemind(str);
            });
        }
        page.setSize(list.size());
        page.setRecords(list);
        Map<String, Object> dataTable = getDataTable(page);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("sessionIn/{ids}")
    @ControllerEndpoint(exceptionMessage = "获取周期失败")
    public FebsResponse sessionIn(@PathVariable("ids") String ids, HttpSession session) {
        if (ids == null) {
            System.err.println("null");
        }
        session.setAttribute("ids", ids);
        System.err.println("ids:" + ids);
        return new FebsResponse().success();
    }

    @GetMapping("sessionDel")
    @ControllerEndpoint(exceptionMessage = "获取周期失败")
    public FebsResponse sessionInNull(HttpSession session) {
        session.setAttribute("ids", "0");
        return new FebsResponse().success();
    }

    @GetMapping("reminds/{periodId}")
    @ControllerEndpoint(exceptionMessage = "获取提醒时间失败")
    public FebsResponse reminds(@PathVariable("periodId") Long periodId, QueryRequest request) {
        QueryWrapper<Remind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PERIOD_ID", periodId);
        Page<Remind> page = new Page<>(request.getPageNum(), request.getPageSize());
        page = remindService.page(page, queryWrapper);
        Map<String, Object> dataTable = getDataTable(remindService.page(page, queryWrapper));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增提醒时间", exceptionMessage = "新增提醒时间失败")
    @GetMapping("remindAdd/{value}")
    @ResponseBody
    public FebsResponse addMatterRemind(@PathVariable("value") String value) throws ParseException {
        remindService.cycleRemind(value);
        return new FebsResponse().success();
    }

    /**
     * 转换
     *
     * @param ids
     * @return`
     */
    private Long[] getLong(String ids) {
        String[] strings = ids.split(",");
        return getLongs(strings);
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

    /**
     * @param periods
     * @return
     */
    private List<PeriodTree> getPeriodTree(List<Period> periods) {
        List<PeriodTree> periodTrees = new ArrayList<>();
        periods.forEach(period -> {
            PeriodTree periodTree = new PeriodTree();
            /*if (period.getPeriodOpen() == null) {
                System.err.println("复制为ture!!!!!!!!!!!!!!!!!!");
                periodTree.setDisabled("true");
            } else {
                System.err.println("赋值为false???????????????????????????????");
                periodTree.setDisabled("false");
            }*/
            QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
            periodTree.setName(period.getPeriodName());
            periodTree.setValue(period.getPeriodId().toString());
            queryWrapper.eq("PARENT_ID", period.getPeriodId());
            List<Period> list = this.periodService.list(queryWrapper);
            if (list.size() != 0) {
                List<PeriodTree> periodTreeList = getPeriodTree(list);
                periodTree.setChildren(periodTreeList);
            }
            periodTrees.add(periodTree);
        });
        return periodTrees;
    }

}
