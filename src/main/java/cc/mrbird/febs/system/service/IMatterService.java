package cc.mrbird.febs.system.service;

import cc.mrbird.febs.system.entity.Matter;

import cc.mrbird.febs.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.collect.ListMultimap;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service接口
 *
 * @author MrBird
 * @date 2020-07-16 14:57:57
 */
public interface IMatterService extends IService<Matter> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param matter  matter
     * @return IPage<Matter>
     */
    IPage<Matter> findMatters(QueryRequest request, Matter matter);

    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param matter  matter
     * @return IPage<Matter>
     */
    IPage<Matter> findMattersForOne(QueryRequest request, Matter matter);

    /**
     * 查询（所有）
     *
     * @param matter matter
     * @return List<Matter>
     */
    List<Matter> findMatters(Matter matter);

    /**
     * 新增
     *
     * @param matter matter
     */
    void createMatter(Matter matter) throws ParseException;

    /**
     * 修改
     *
     * @param matter matter
     */
    void updateMatter(Matter matter);

    /**
     * 删除
     *
     * @param matter matter
     */
    void deleteMatter(Matter matter);

    /**
     * @param matterId
     */
    void deleteMatterById(Long matterId);

    /**
     * 修改Matter为紧急
     *
     * @param matterId
     */
    void updateMatterUrgent(Long matterId);

    /**
     * 根据结束时间查询MatterId
     * @param date
     * @return MatterIds
     */
    List<Long> selectMatterIdsByEndTime(String date);

    /**
     * 修改循环状态
     * @param forEach
     * @param date
     * @return
     */
    void selectMatterIdsByForEach(Integer forEach , String date);

    /**
     * 修改IsOpen
     * @param date
     */
    void updateIsOpen(String date);

    /**
     * 完成
     * @param value
     */
    void finishMatter(String value);

    /**
     * 未完成
     * @param value
     */
    void finishMatterNo(String value);

    /**
     * 定时任务修改userMatter的finish
     * @param matterIds
     */
    void changeFinish(List<Long> matterIds);

    /**
     *
     */
    void copyUserMatter(Long oldMatterId , Long newMatterId);

    /**
     * 最大Id
     * @return
     */
    Long maxMatterId();
}
