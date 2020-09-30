package cc.mrbird.febs.system.service.impl;

import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import cc.mrbird.febs.system.entity.Team;
import cc.mrbird.febs.system.entity.UserTeam;
import cc.mrbird.febs.system.mapper.TeamMapper;
import cc.mrbird.febs.system.mapper.UserTeamMapper;
import cc.mrbird.febs.system.service.ITeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.swing.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service实现
 *
 * @author weizihao
 * @date 2020-07-21 17:39:37
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

    private final TeamMapper teamMapper;
    private final UserTeamMapper userTeamMapper;

    @Override
    public IPage<Team> findTeamsPage(QueryRequest request, Team team) {
        //LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<Team> page = new Page<>(request.getPageNum(), request.getPageSize());
        page.setSearchCount(false);
        page.setTotal(baseMapper.countTeamDetail(team));
        SortUtil.handlePageSort(request, page, "teamId", FebsConstant.ORDER_ASC, false);
        return this.baseMapper.findTeamDetailPage(page, team);
    }

    @Override
    public List<Team> findTeams(Team team) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        //System.err.println("teamService:" + team);
        return teamMapper.findTeams(team);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTeam(Team team) {
        UserTeam userTeam = new UserTeam();
        String userId = team.getUserId();
        String[] userIds = userId.split(",");
        team.setCreateTime(new Date());
        this.save(team);
        userTeam.setTeamId(this.teamMapper.findMaxId());
        for (String idS : userIds
        ) {
            Long id = Long.valueOf(idS);
            userTeam.setUserId(id);
            userTeamMapper.insert(userTeam);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeam(Team team) {
        String userId = team.getUserId();
        Map<String, Object> map = new HashMap<>();
        map.put("team_id", team.getTeamId());
        userTeamMapper.deleteByMap(map);
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(team.getTeamId());
        String[] userIdSs = userId.split(",");
        for (String userIdS : userIdSs
        ) {
            Long id = Long.valueOf(userIdS);
            userTeam.setUserId(id);
            userTeamMapper.insert(userTeam);
        }
        this.saveOrUpdate(team);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeam(Team team) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        // TODO 设置删除条件
        //teamMapper.delete(wrapper);
        this.remove(wrapper);
    }

    @Override
    public void deleteByTeamId(String teamIds) {
        String[] teamIdSS = teamIds.split(",");
        Map<String, Object> map = new HashMap<>();
        for (String teamIdS : teamIdSS
        ) {
            Long teamId = Long.valueOf(teamIdS);
            teamMapper.deleteById(teamId);
            map.put("team_id", teamId);
            userTeamMapper.deleteByMap(map);
            map.clear();
        }
    }
}
