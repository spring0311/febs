package cc.mrbird.febs.system.service.impl;

import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.system.entity.Matter;
import cc.mrbird.febs.system.entity.Remind;
import cc.mrbird.febs.system.mapper.MatterMapper;
import cc.mrbird.febs.system.mapper.RemindMapper;
import cc.mrbird.febs.system.mapper.UserMatterMapper;
import cc.mrbird.febs.system.service.IRemindService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service实现
 *
 * @author weizihao
 * @date 2020-07-28 09:51:57
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class RemindServiceImpl extends ServiceImpl<RemindMapper, Remind> implements IRemindService {

    private final RemindMapper remindMapper;

    private final MatterMapper matterMapper;

    private final UserMatterMapper userMatterMapper;

    @Override
    public IPage<Remind> findReminds(QueryRequest request, Remind remind) {
        LambdaQueryWrapper<Remind> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<Remind> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Remind> findReminds(Remind remind) {
        LambdaQueryWrapper<Remind> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Map<String, Object> map = new HashMap<>();
        map.put("matter_id", remind.getMatterId());
        List<Remind> reminds = remindMapper.selectByMap(map);
        return reminds;//this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Matter createRemind(String string) throws ParseException {
        //拆分为Id与date
        String[] strings = string.split(",");
        String dateStr = strings[0];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //remindTime
        Date date = simpleDateFormat.parse(dateStr);
        //matterId
        Long matterId = Long.valueOf(strings[1]);
        //判断提醒时间是否存在
        Map<String, Object> map = new HashMap<>();
        map.put("MATTER_ID", matterId);
        map.put("REMIND_TIME", date);
        List<Remind> reminds = remindMapper.selectByMap(map);
        if (reminds.size() != 0) {
            throw new FebsException("提醒时间已存在");
        }
        Remind remind = new Remind();
        remind.setMatterId(matterId);
        remind.setRemindTime(date);
        remindMapper.insert(remind);
        Matter matter = new Matter();
        matter.setMatterId(matterId);
        return matterMapper.findMatterDetail(matter).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRemind(Remind remind) {
        this.saveOrUpdate(remind);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRemind(Remind remind) {
        LambdaQueryWrapper<Remind> wrapper = new LambdaQueryWrapper<>();
        // TODO 设置删除条件
        Map<String, Object> map = new HashMap<>();
        map.put("matter_id", remind.getMatterId());
        remindMapper.deleteByMap(map);
    }

    @Override
    public void deleteOver(Remind remind) {
        Map<String, Object> map = new HashMap<>();
        map.put("MATTER_ID", remind.getMatterId());
        map.put("REMIND_TIME", remind.getRemindTime());
        remindMapper.deleteByMap(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRemindByRemindId(Remind remind) {
        LambdaQueryWrapper<Remind> wrapper = new LambdaQueryWrapper<>();
        // TODO 设置删除条件
        Map<String, Object> map = new HashMap<>();
        map.put("remind_id", remind.getRemindId());
        remindMapper.deleteByMap(map);
    }

    /**
     * 定时任务查询提醒时间
     * 获得事项IDs
     *
     * @param date
     * @return
     * @throws ParseException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectMatterIds(Date date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateTime = simpleDateFormat.format(date);
        date = simpleDateFormat.parse(dateTime);
        return remindMapper.selectMatterId(date);
    }

    /**
     * 查询单个用户的所有remind
     *
     * @param userId
     * @return
     */
    @Override
    public List<Remind> findAllTReminds(Long userId) {
        //通过用户Id先行获取MatterId UserMatter映射表
        //获得的Matter是开启状态的 mapper.xml中写死的
        System.err.println("userID:" + userId);
        List<Long> matterIds = userMatterMapper.selectMatterIds(userId);
        //声明TRemind的集合 得到所有Matter对象的对应Remind
        List<Remind> tReminds = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (Long matterId : matterIds
        ) {
            map.clear();
            map.put("MATTER_ID", matterId);
            List<Remind> list = remindMapper.selectByMap(map);
            tReminds.addAll(list);
        }
        //Remind 注入 Matter
        for (Remind t : tReminds
        ) {
            Matter tMatter = new Matter();
            tMatter.setMatterId(t.getMatterId());
            tMatter.setIsOpen(0);
            // tMatter = tMatterMapper.selectMattersByMatter(tMatter).get(0);
            List<Matter> tmatter = matterMapper.findMatterDetail(tMatter);
            if (tmatter.size() <= 0) {
                tMatter = new Matter();
            } else {
                tMatter = matterMapper.findMatterDetail(tMatter).get(0);
            }
            t.setTMatter(tMatter);
        }
        return tReminds;
    }

    @Override
    public List<Remind> getByremindtime(Remind tRemindy) throws ParseException {
        List<Remind> tReminds = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        List<Long> matterIds = userMatterMapper.selectMatterIds(tRemindy.getUserId());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Remind tRemind = new Remind();
        tRemind.setRemindTime(simpleDateFormat.parse(tRemindy.getRemindTimestr()));
        for (Long matterId : matterIds
        ) {
            //  map.clear();
            map.put("matter_id", matterId);
            map.put("REMIND_TIME", tRemind.getRemindTime());
            List<Remind> list = remindMapper.selectByMap(map);
            tReminds.addAll(list);
        }
        //  map.put("REMIND_TIME", tRemind.getRemindTime());
        //   List<TRemind> list = tRemindMapper.selectByMap(map);
        //   tReminds.addAll(list);
        for (Remind t : tReminds
        ) {
            Matter tMatter = new Matter();
            tMatter.setMatterId(t.getMatterId());
            tMatter.setIsOpen(0);
            List<Matter> tmatter = matterMapper.findMatterDetail(tMatter);
            if (tmatter.size() <= 0) {
                tMatter = new Matter();
            } else {
                tMatter = matterMapper.findMatterDetail(tMatter).get(0);
            }
            t.setTMatter(tMatter);
        }
        return tReminds;
    }

    @Override
    public void addRemind(Remind tRemind) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        tRemind.setRemindTime(simpleDateFormat.parse(tRemind.getRemindTimestr()));
        remindMapper.insert(tRemind);
    }

    @Override
    public List<Remind> findRemindForMatter(String remindId) {
        List<Long> remindIds = getRemindIds(remindId);
        List<Remind> tReminds = new ArrayList<>();
        Remind tRemind = new Remind();
        for (Long id : remindIds
        ) {
            tRemind = remindMapper.selectById(id);
            tReminds.add(tRemind);
        }
        return tReminds;
    }

    @Override
    public List<Remind> getBymatterId(Long matterId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("MATTER_ID", matterId);
        List<Remind> tRemind = remindMapper.selectByMap(map);
        return tRemind;
    }

    @Override
    public void delByRemindId(Long remindId) {
        remindMapper.deleteById(remindId);
    }

    /**
     * String转换List
     *
     * @param remindId
     * @return
     */
    private List<Long> getRemindIds(String remindId) {
        List<Long> remindIds = new ArrayList<>();
        String[] strs = remindId.split(",");
        for (String str : strs
        ) {
            Long id = Long.valueOf(str);
            remindIds.add(id);
        }
        return remindIds;
    }

    private Date getDate() throws ParseException {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String string = simpleDateFormat.format(date);
        date = simpleDateFormat.parse(string);
        return date;
    }

    @Override
    public List<Remind> findByDate(Long userId) throws ParseException {


        return null;
    }


}
