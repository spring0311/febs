package cc.mrbird.febs.system.mapper;

import cc.mrbird.febs.system.entity.Team;
import cc.mrbird.febs.system.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper
 *
 * @author weizihao
 * @date 2020-07-21 17:39:37
 */
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 查找Team与其User
     *
     * @param team
     * @return
     */
    List<Team> findTeams(@Param("team") Team team);


    /**
     * 分页查询Team与其User
     *
     * @param page 分页
     * @param <T>
     * @param team 查询条件
     * @return
     */
    <T> IPage<Team> findTeamDetailPage(Page<T> page, @Param("team") Team team);

    Long countTeamDetail(@Param("team") Team team);

    /**
     * 查询最大ID
     * @return
     */
    Long findMaxId();

}
