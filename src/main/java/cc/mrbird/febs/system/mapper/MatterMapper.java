package cc.mrbird.febs.system.mapper;

import cc.mrbird.febs.system.entity.Matter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Mapper
 *
 * @author MrBird
 * @date 2020-07-16 14:57:57
 */
public interface MatterMapper extends BaseMapper<Matter> {


    /**
     * 查找Matter详细信息
     *
     * @param matter
     * @return
     */
    List<Matter> findMatterDetail(@Param("matter") Matter matter);

    /**
     * @param page
     * @param matter
     * @param <T>
     * @return
     */
    <T> IPage<Matter> findMatterDetailPage(Page<T> page, @Param("matter") Matter matter);

    long countMatterDetail(@Param("matter") Matter matter);

    long countMatterDetailForOne(@Param("matter") Matter matter);


    /**
     * 修改Matter是否紧急
     *
     * @param matterId
     */
    void updateMatterUrgent(Long matterId);

    /**
     *
     * @param date
     * @return
     */
    List<Long> findIdsByEndTime(String date);

    /**
     * 循环状态ID
     * @param forEach
     * @param date
     * @return
     */
    void updateIsOpenByEach(@Param("forEach") Integer forEach , @Param("date") String date);

    /**
     * 查询最大Id
     * @return
     */
    Long findMaxId();

    /**
     *
     * @param date
     */
    void updateIsOpen(String date);
}
