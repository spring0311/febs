package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.system.entity.Cycle;
import cc.mrbird.febs.system.entity.Period;
import cc.mrbird.febs.system.service.ICycleService;
import cc.mrbird.febs.system.service.IPeriodService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 周期控制类
 *
 * @author: weiZiHao
 * @create: 2020-09-10 13:48
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("cycle")
public class CycleController extends BaseController {

    @Autowired
    private final ICycleService iCycleService;

    @Autowired
    private final IPeriodService iPeriodService;


    @GetMapping("list")
    @ControllerEndpoint(operation = "周期列表", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse cycleList(QueryRequest request, Cycle cycle) {
        QueryWrapper<Cycle> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(cycle);
        Page<Cycle> page = new Page<>(request.getPageNum(), request.getPageSize());
        page = iCycleService.page(page, queryWrapper);
        Map<String, Object> dataTable = getDataTable(this.iCycleService.page(page, queryWrapper));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("all")
    @ControllerEndpoint(operation = "周期列表", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse cycleAll() {
        return new FebsResponse().success().data(this.iCycleService.list());
    }


    @GetMapping("periodList")
    @ControllerEndpoint(operation = "执行时间列表", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse period(HttpSession model, QueryRequest queryRequest) {
        Long cycleId = (Long) model.getAttribute("cycleId");
        Period period = new Period();
        period.setCycleId(cycleId);
        System.err.println("periodList:" + period);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(period);
        Page<Period> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        page = iPeriodService.page(page, queryWrapper);
        Map<String, Object> dataTable = getDataTable(this.iPeriodService.page(page, queryWrapper));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("period/delete/{periodId}")
    @ControllerEndpoint(operation = "删除执行时间", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse periodDele(@PathVariable("periodId") Long periodId) {
        this.iPeriodService.removeById(periodId);
        return new FebsResponse().success();
    }

    @GetMapping("add")
    @ControllerEndpoint(operation = "新增周期", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse addCycle(Cycle cycle) {
        cycle.setCreateTime(new Date());
        iCycleService.saveOrUpdate(cycle);
        return new FebsResponse().success();
    }

    @GetMapping("delete/{cycleId}")
    @ControllerEndpoint(operation = "删除周期", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse deleteCycle(@PathVariable("cycleId") Long cycleId) {
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("CYCLE_ID", cycleId);
        this.iPeriodService.remove(queryWrapper);
        this.iCycleService.removeById(cycleId);
        return new FebsResponse().success();
    }

    /*period/add*/

    @GetMapping("period/add")
    @ControllerEndpoint(operation = "新增执行时间", exceptionMessage = "执行失败")
    @ResponseBody
    public FebsResponse periodAdd(Period period, HttpSession session) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date periodOpen = simpleDateFormat.parse(period.getPeriodOpenStr());
        Date periodEnd = simpleDateFormat.parse(period.getPeriodEndStr());
        period.setOpenStr(period.getPeriodOpenStr());
        period.setPeriodOpen(periodOpen);
        period.setPeriodEnd(periodEnd);
        period.setCreateTime(new Date());
        period.setCycleId((Long) session.getAttribute("cycleId"));
        System.err.println("period:" + period);
        this.iPeriodService.saveOrUpdate(period);
        return new FebsResponse().success();
    }


}
