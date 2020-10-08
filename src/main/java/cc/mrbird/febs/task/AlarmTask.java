package cc.mrbird.febs.task;

import cc.mrbird.febs.system.entity.Matter;
import cc.mrbird.febs.system.entity.Period;
import cc.mrbird.febs.system.entity.Remind;
import cc.mrbird.febs.system.service.ICycleService;
import cc.mrbird.febs.system.service.IMatterService;
import cc.mrbird.febs.system.service.IPeriodService;
import cc.mrbird.febs.system.service.IRemindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定时任务
 *
 * @author: weiZiHao
 * @create: 2020-07-30 10:33
 */
@Component
public class AlarmTask implements InitializingBean {

    private final IRemindService iRemindService;

    private final IMatterService matterService;

    private final ICycleService iCycleService;

    private final IPeriodService iPeriodService;

    public AlarmTask(IRemindService iRemindService, IMatterService matterService, ICycleService iCycleService, IPeriodService iPeriodService) {
        this.iRemindService = iRemindService;
        this.matterService = matterService;
        this.iCycleService = iCycleService;
        this.iPeriodService = iPeriodService;
    }


    /**
     * 定时任务
     * 提醒时间到来当天触发条件
     * 修改为紧急事项
     * 之后提醒时间删除
     *
     * @throws ParseException
     */
    //@Scheduled(cron = "0 00 08 * * ?")//触发时间 秒 分 时
    public void selectMatterRemind() throws ParseException {
        //得到当前时间的yyyy-MM-dd
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = simpleDateFormat.format(date);
        date = simpleDateFormat.parse(dateStr);
        //查询到matterId
        List<Long> matterIds = iRemindService.selectMatterIds(date);
        //修改urgent
        Remind remind = new Remind();
        remind.setRemindTime(date);
        if (matterIds.size() != 0) {
            for (Long matterId : matterIds
            ) {
                matterService.updateMatterUrgent(matterId);
                remind.setMatterId(matterId);
                iRemindService.deleteOver(remind);
            }
        }
    }

    /**
     * 结束时间提前15天设为紧急
     *
     * @throws ParseException
     */
    //@Scheduled(cron = "0 00 08 * * ?")
    public void endTimeRemind() throws ParseException {
        String date = getDate();
        List<Long> matterIds = matterService.selectMatterIdsByEndTime(date);
        if (matterIds.size() != 0) {
            matterIds.forEach(matterId -> {
                matterService.updateMatterUrgent(matterId);
            });
        }
    }

    /**
     * 循环时开启事项
     *
     * @throws ParseException
     */
    //@Scheduled(cron = "0 00 08 * * ?")
    public void matterEach() throws ParseException {
        //得到当天MM-dd
        String date = getNow();
        //开启循环
        Integer forEach = 1;
        matterService.selectMatterIdsByForEach(forEach, date);
    }

    /**
     * 循环时复制事项
     *
     * @throws ParseException
     */
    //@Scheduled(cron = "0 00 08 * * ?")
    public void forEACH() throws ParseException {
        Matter matter = new Matter();
        /**
         * 获得去年的当天时间
         */
        Date date = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(cal.YEAR, -1);
        date = cal.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = simpleDateFormat.format(date);
        matter.setDate(dateStr);
        //补充条件
        matter.setForEach(1);
        matter.setIsOpen(0);
        List<Matter> matters = matterService.findMatters(matter);
        matters.forEach(matter1 -> {
            System.err.println(matter1);
        });
        if (matters.size() > 0) {
            matters.forEach(matterDao -> {
                //取消循环状态
                matterDao.setForEach(0);
                matterService.saveOrUpdate(matterDao);
                Long oldId = matterDao.getMatterId();
                //生成新事务
                matterDao.setMatterId(null);
                matterDao.setForEach(1);
                Calendar calendar = new GregorianCalendar();
                //时间加1年
                Date createDate = matterDao.getCreateTime();
                calendar.setTime(createDate);
                calendar.add(Calendar.YEAR, 1);
                createDate = calendar.getTime();
                matterDao.setCreateTime(createDate);

                Date matterOpen = matterDao.getMatterOpen();
                calendar.setTime(matterOpen);
                calendar.add(Calendar.YEAR, 1);
                matterOpen = calendar.getTime();
                matterDao.setMatterOpen(matterOpen);

                Date end = matterDao.getEnd();
                calendar.setTime(end);
                calendar.add(calendar.YEAR, 1);
                end = calendar.getTime();
                matterDao.setEnd(end);
                //存
                matterService.saveOrUpdate(matterDao);
                //用户映射表
                matterService.copyUserMatter(oldId, matterService.maxMatterId());
            });
        }
    }

    /**
     * 每月执行定时任务
     * 周期事务复制
     */
    //@Scheduled(cron = "0 00 07 * * ?")
    public void periodMonth() {
        //得到当天减一个月的yyyy-MM-dd
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -1);
        now = calendar.getTime();
        String nowStr = simpleDateFormat.format(now);
        //查询条件
        Matter matterU = new Matter();
        matterU.setIsOpen(0);
        matterU.setPeriod("月");
        matterU.setDate(nowStr);
        //逻辑
        List<Matter> matters = matterService.findMatters(matterU);
        if (matters.size() > 0) {
            matters.forEach(matter -> {
                /**
                 * 月周期
                 */
                //先行删除matter的周期 以免重复生成
                Long oldId = matter.getMatterId();
                matter.setPeriod("已执行");
                matterService.saveOrUpdate(matter);
                //得到起止日期
                Date matterStart = matter.getMatterOpen();
                Date matterEnd = matter.getEnd();
                //加一月
                calendar.setTime(matterStart);
                calendar.add(Calendar.MONTH, 1);
                matterStart = calendar.getTime();
                calendar.setTime(matterEnd);
                calendar.add(Calendar.MONTH, 1);
                matterEnd = calendar.getTime();
                //新matter值修改
                matter.setMatterId(null);
                matter.setPeriod("月");
                matter.setMatterOpen(matterStart);
                matter.setEnd(matterEnd);
                //存
                matterService.saveOrUpdate(matter);
                //用户映射表
                matterService.copyUserMatter(oldId, matterService.maxMatterId());
            });
        }
    }

    /**
     * 每季度执行定时任务
     * 周期事务复制
     */
    // @Scheduled(cron = "0 00 07 * * ?")
    public void periodThreeMonth() {
        //得到当天减三个月的yyyy-MM-dd
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -3);
        now = calendar.getTime();
        String nowStr = simpleDateFormat.format(now);
        //查询条件
        Matter matterU = new Matter();
        matterU.setIsOpen(0);
        matterU.setPeriod("季度");
        matterU.setDate(nowStr);
        //逻辑
        List<Matter> matters = matterService.findMatters(matterU);
        if (matters.size() > 0) {
            matters.forEach(matter -> {
                /**
                 * 季度周期
                 */
                //先行删除matter的周期 以免重复生成
                Long oldId = matter.getMatterId();
                matter.setPeriod("已执行");
                matterService.saveOrUpdate(matter);
                //得到起止日期
                Date matterStart = matter.getMatterOpen();
                Date matterEnd = matter.getEnd();
                //加一月
                calendar.setTime(matterStart);
                calendar.add(Calendar.MONTH, 3);
                matterStart = calendar.getTime();
                calendar.setTime(matterEnd);
                calendar.add(Calendar.MONTH, 3);
                matterEnd = calendar.getTime();
                //新matter值修改
                matter.setMatterId(null);
                matter.setMatterOpen(matterStart);
                matter.setEnd(matterEnd);
                matter.setPeriod("季度");
                //存
                matterService.saveOrUpdate(matter);
                //用户映射表
                matterService.copyUserMatter(oldId, matterService.maxMatterId());
            });
        }
    }

    /**
     * 每半年执行定时任务
     * 周期事务复制
     */
    // @Scheduled(cron = "0 00 07 * * ?")
    public void periodHalfYear() {
        //得到当天减半年的yyyy-MM-dd
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -6);
        now = calendar.getTime();
        String nowStr = simpleDateFormat.format(now);
        //查询条件
        Matter matterU = new Matter();
        matterU.setIsOpen(0);
        matterU.setPeriod("半年");
        matterU.setDate(nowStr);
        //逻辑
        List<Matter> matters = matterService.findMatters(matterU);
        if (matters.size() > 0) {
            matters.forEach(matter -> {
                /**
                 * 季度周期
                 */
                //先行删除matter的周期 以免重复生成
                Long oldId = matter.getMatterId();
                matter.setPeriod("已执行");
                matterService.saveOrUpdate(matter);
                //得到起止日期
                Date matterStart = matter.getMatterOpen();
                Date matterEnd = matter.getEnd();
                //加一月
                calendar.setTime(matterStart);
                calendar.add(Calendar.MONTH, 6);
                matterStart = calendar.getTime();
                calendar.setTime(matterEnd);
                calendar.add(Calendar.MONTH, 6);
                matterEnd = calendar.getTime();
                //新matter值修改
                matter.setMatterId(null);
                matter.setMatterOpen(matterStart);
                matter.setEnd(matterEnd);
                matter.setPeriod("半年");
                //存
                matterService.saveOrUpdate(matter);
                //用户映射表
                matterService.copyUserMatter(oldId, matterService.maxMatterId());
            });
        }
    }

    /**
     * 每半年执行定时任务
     * 周期事务复制
     */
    // @Scheduled(cron = "0 00 07 * * ?")
    public void periodYear() {
        //得到当天减一年的yyyy-MM-dd
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.YEAR, -1);
        now = calendar.getTime();
        String nowStr = simpleDateFormat.format(now);
        //查询条件
        Matter matterU = new Matter();
        matterU.setIsOpen(0);
        matterU.setPeriod("年");
        matterU.setDate(nowStr);
        //逻辑
        List<Matter> matters = matterService.findMatters(matterU);
        if (matters.size() > 0) {
            matters.forEach(matter -> {
                /**
                 * 季度周期
                 */
                //先行删除matter的周期 以免重复生成
                Long oldId = matter.getMatterId();
                matter.setPeriod("已执行");
                matterService.saveOrUpdate(matter);
                //得到起止日期
                Date matterStart = matter.getMatterOpen();
                Date matterEnd = matter.getEnd();
                //加一月
                calendar.setTime(matterStart);
                calendar.add(Calendar.YEAR, 1);
                matterStart = calendar.getTime();
                calendar.setTime(matterEnd);
                calendar.add(Calendar.YEAR, 1);
                matterEnd = calendar.getTime();
                //新matter值修改
                matter.setMatterId(null);
                matter.setMatterOpen(matterStart);
                matter.setEnd(matterEnd);
                matter.setPeriod("年");
                //存
                matterService.saveOrUpdate(matter);
                //用户映射表
                matterService.copyUserMatter(oldId, matterService.maxMatterId());
            });
        }
    }


    /**
     * 结束时间关闭事项
     *
     * @throws ParseException
     */
    //@Scheduled(cron = "0 00 08 * * ?")
    public void closeMatter() throws ParseException {
        //得到当天MM-dd
        String date = getNow();
        matterService.updateIsOpen(date);
        List<Long> matterIds = matterService.selectMatterIdsByEndTime(date);
        if (matterIds.size() != 0) {
            matterService.changeFinish(matterIds);
        }
    }


    /**
     * 周期定时
     */
    //@Scheduled(cron = "0 00 07 * * ?")
    public void cycleMatter() {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String str = simpleDateFormat.format(now);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeLeft("OPEN_STR", str);
        List<Period> periods = iPeriodService.list(queryWrapper);
        List<Long> cycleIds = new ArrayList<>();
        List<Matter> matters = new ArrayList<>();
        if (periods.size() > 0) {
            periods.forEach(period -> {
                cycleIds.add(period.getCycleId());
                Date start = period.getPeriodOpen();
                Date end = period.getPeriodEnd();
                if (cycleIds.size() > 0) {
                    cycleIds.forEach(cycleId -> {
                        Matter dao = new Matter();
                        dao.setCycleId(cycleId);
                        matters.addAll(matterService.findMatters(dao));
                        if (matters.size() > 0) {
                            matters.forEach(matter -> {
                                Long oldId = matter.getMatterId();
                                matter.setMatterId(null);
                                matter.setMatterOpen(start);
                                matter.setEnd(end);
                                matter.setCycleId(0l);
                                matterService.saveOrUpdate(matter);
                                matterService.copyUserMatter(oldId, matterService.maxMatterId());
                            });
                        }
                    });
                }
            });
        }

    }


    /**
     * 得到日期加15
     *
     * @return 日期加15
     * @throws ParseException
     */
    private String getDate() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        Date date = new Date();
        /*String time = simpleDateFormat.format(date);
        date = simpleDateFormat.parse(time);*/
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, 15);
        date = calendar.getTime();
        String end = simpleDateFormat.format(date);
        return end;
    }

    /**
     * 现在日期String MM-dd
     *
     * @return
     * @throws ParseException
     */
    private String getNow() throws ParseException {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    /**
     * 执行循环任务
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //matterEach();
        selectMatterRemind();
        endTimeRemind();
        forEACH();
        //closeMatter();
        periodMonth();
        periodThreeMonth();
        periodHalfYear();
        periodYear();
        cycleMatter();
    }
}
