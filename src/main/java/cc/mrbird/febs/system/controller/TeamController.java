package cc.mrbird.febs.system.controller;

import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.system.entity.Team;
import cc.mrbird.febs.system.service.ITeamService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.formula.functions.T;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller
 *
 * @author weizihao
 * @date 2020-07-21 17:39:37
 */
@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
public class TeamController extends BaseController {

    private final ITeamService teamService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "team")
    public String teamIndex() {
        return FebsUtil.view("team/team");
    }

    @GetMapping("team")
    @ResponseBody
    //@RequiresPermissions("team:view")
    public FebsResponse getAllTeams(Team team) {
        //System.err.println("getAllTeams");
        return new FebsResponse().success().data(teamService.findTeams(team));
    }

    /*@GetMapping("team/all")
    @ResponseBody
    //@RequiresPermissions("team:view")
    public FebsResponse getTeamList(Team team) {
        //System.err.println("getAllTeams");
        return new FebsResponse().success().data(teamService.findTeams(team));
    }*/

    @GetMapping("team/list")
    @ResponseBody
    @RequiresPermissions("team:view")
    public FebsResponse teamList(QueryRequest request, Team team) {
        Map<String, Object> dataTable = getDataTable(this.teamService.findTeamsPage(request, team));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增Team", exceptionMessage = "新增Team失败")
    @PostMapping("team/add")
    @ResponseBody
    //@RequiresPermissions("team:add")
    public FebsResponse addTeam(@Valid Team team) {
        //System.err.println("addTeam");
        this.teamService.createTeam(team);
        return new FebsResponse().success();
    }

   /* @ControllerEndpoint(operation = "删除Team", exceptionMessage = "删除Team失败")
    @GetMapping("team/delete/{teamId}")
    @ResponseBody
    //@RequiresPermissions("team:delete")
    public FebsResponse deleteTeam(@PathVariable("teamId") Long teamId) {
        teamService.deleteByTeamId(teamId);
        return new FebsResponse().success();
    }*/

    @ControllerEndpoint(operation = "删除Team", exceptionMessage = "删除Team失败")
    @GetMapping("team/delete/{teamIds}")
    @ResponseBody
    //@RequiresPermissions("team:delete")
    public FebsResponse deleteTeams(@PathVariable("teamIds") String teamIds) {
        teamService.deleteByTeamId(teamIds);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改Team", exceptionMessage = "修改Team失败")
    @PostMapping("team/update")
    @ResponseBody
    //@RequiresPermissions("team:update")
    public FebsResponse updateTeam(Team team) {
        this.teamService.updateTeam(team);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改Team", exceptionMessage = "导出Excel失败")
    @PostMapping("team/excel")
    @ResponseBody
    //@RequiresPermissions("team:export")
    public void export(QueryRequest queryRequest, Team team, HttpServletResponse response) {
        List<Team> teams = this.teamService.findTeamsPage(queryRequest, team).getRecords();
        ExcelKit.$Export(Team.class, response).downXlsx(teams, false);
    }
}
