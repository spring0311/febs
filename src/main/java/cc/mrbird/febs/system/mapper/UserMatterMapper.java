package cc.mrbird.febs.system.mapper;

import cc.mrbird.febs.system.entity.UserMatter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Mapper
 *
 * @author weizihao
 * @date 2020-07-27 10:01:07
 */
@Repository
public interface UserMatterMapper extends BaseMapper<UserMatter> {

    /**
     * @param tUserMatter
     */
    void updateByUserIdAndMatterId(@Param("tUserMatter") UserMatter tUserMatter);

    void updateByUserIdAndMatterIds(@Param("tUserMatter") UserMatter tUserMatter);

    List<Long> selectMatterIds(@Param("userId") Long userId);

}
