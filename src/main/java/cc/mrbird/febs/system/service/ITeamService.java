package cc.mrbird.febs.system.service;

import cc.mrbird.febs.system.entity.Team;

import cc.mrbird.febs.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Service接口
 *
 * @author weizihao
 * @date 2020-07-21 17:39:37
 */
public interface ITeamService extends IService<Team> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param team    team
     * @return IPage<Team>
     */
    IPage<Team> findTeamsPage(QueryRequest request, Team team);

    /**
     * 查询（所有）
     *
     * @param team team
     * @return List<Team>
     */
    List<Team> findTeams(Team team);

    /**
     * 新增
     *
     * @param team team
     */
    void createTeam(Team team);

    /**
     * 修改
     *
     * @param team team
     */
    void updateTeam(Team team);

    /**
     * 删除
     *
     * @param team team
     */
    void deleteTeam(Team team);

    /**
     * Id删除
     *
     * @param teamId
     */
    void deleteByTeamId(String teamId);
}
