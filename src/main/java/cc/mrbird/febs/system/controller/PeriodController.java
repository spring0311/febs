package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.DeptTree;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.PeriodTree;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.system.entity.Period;
import cc.mrbird.febs.system.service.IPeriodService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public FebsResponse getPeriodTree(Period period) throws FebsException {
        period.setParentId(0l);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(period);
        List<Period> periods = this.periodService.list(queryWrapper);
        List<PeriodTree> periodTrees = new ArrayList<>();
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
        Page<Period> page = new Page<>(request.getPageNum(), request.getPageSize());
        page = this.periodService.page(page, queryWrapper);
        Map<String, Object> dataTable = getDataTable(this.periodService.page(page, queryWrapper));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("addOrUpdate")
    @ControllerEndpoint(exceptionMessage = "新增或修改周期失败")
    public FebsResponse addPeriod(Period period) {
        this.periodService.saveOrUpdate(period);
        return new FebsResponse().success();
    }

    @GetMapping("showList")
    @ControllerEndpoint(exceptionMessage = "获取周期失败")
    public FebsResponse showList(HttpSession session, QueryRequest request) {
        String ids = (String) session.getAttribute("ids");
        Long[] longIds = getLong(ids);
        System.err.println("ids:" + ids);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("PERIOD_ID", longIds);
        Page<Period> page = new Page<>(request.getPageNum(), request.getPageSize());
        page = this.periodService.page(page, queryWrapper);
        Map<String, Object> dataTable = getDataTable(this.periodService.page(page, queryWrapper));
        System.err.println("执行完毕");
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("sessionIn/{ids}")
    @ControllerEndpoint(exceptionMessage = "获取周期失败")
    public FebsResponse sessionIn(@PathVariable("ids") String ids, HttpSession session) {
        session.setAttribute("ids", ids);
        System.err.println("ids:" + ids);
        return new FebsResponse().success();
    }

    /**
     * 转换
     *
     * @param ids
     * @return
     */
    private Long[] getLong(String ids) {
        String[] strings = ids.split(",");
        List<Long> list = new ArrayList<>();
        for (String str : strings) {
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
