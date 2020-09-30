package cc.mrbird.febs.system.service;

import cc.mrbird.febs.system.entity.Matter;
import cc.mrbird.febs.system.entity.Remind;

import cc.mrbird.febs.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Service接口
 *
 * @author weizihao
 * @date 2020-07-28 09:51:57
 */
public interface IRemindService extends IService<Remind> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param remind  remind
     * @return IPage<Remind>
     */
    IPage<Remind> findReminds(QueryRequest request, Remind remind);

    /**
     * 查询（所有）
     *
     * @param remind remind
     * @return List<Remind>
     */
    List<Remind> findReminds(Remind remind);

    /**
     * 新增
     *
     * @param str
     */
    Matter createRemind(String str) throws ParseException;

    /**
     * 修改
     *
     * @param remind remind
     */
    void updateRemind(Remind remind);

    /**
     * 删除
     *
     * @param remind remind
     */
    void deleteRemind(Remind remind);

    /**
     * 删除
     *
     * @param remind remind
     */
    void deleteOver(Remind remind);

    /**
     * @param remind
     */
    void deleteRemindByRemindId(Remind remind);

    /**
     * 查询MatterIds
     *
     * @param date
     * @return
     */
    List<Long> selectMatterIds(Date date) throws ParseException;


    /**
     *
     *
     */
    /**
     * 根据日期查询数据 为时间轴提供
     *
     * @param
     * @return
     */
    List<Remind> findByDate(Long userId) throws ParseException;

    /**
     * 查找用户事项全部提醒时间与事项
     *
     * @return
     */
    List<Remind> findAllTReminds(Long userId);
    /**
     * 根据提醒时间查询用户的所有信息
     *
     * @return
     * @throws ParseException
     */
    List<Remind> getByremindtime(Remind tRemind) throws ParseException;

    /**
     * 增加
     *
     * @param tRemind
     * @throws ParseException
     */
    void addRemind(Remind tRemind) throws ParseException;

    /**
     * 查询单个Matter的Reminds
     *
     * @param remindId 从Matter中拿到的remindId
     * @return
     */
    List<Remind> findRemindForMatter(String remindId);
    /**
     * 查询单个Matter的Reminds
     *
     * @param matterId 从Matter中拿到的remindId
     * @return
     */
    List<Remind> getBymatterId(Long matterId);
    /**
     * 根据remind删除
     * @param remindId
     */
    void delByRemindId(Long remindId);
}

