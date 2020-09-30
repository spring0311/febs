package cc.mrbird.febs.system.mapper;

import cc.mrbird.febs.system.entity.Remind;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Mapper
 *
 * @author weizihao
 * @date 2020-07-28 09:51:57
 */
public interface RemindMapper extends BaseMapper<Remind> {

    /**
     * 查找提醒时间
     *
     * @param date
     * @return
     */
    List<Long> selectMatterId(Date date);

    List<Remind> testSelect(@Param("tRemind") Remind tRemind);
}
