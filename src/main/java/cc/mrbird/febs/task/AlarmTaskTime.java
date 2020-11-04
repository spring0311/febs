package cc.mrbird.febs.task;

import cc.mrbird.febs.system.entity.*;
import cc.mrbird.febs.system.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "file")
public class AlarmTaskTime implements InitializingBean {

    private final IRemindService iRemindService;

    private final IMatterService matterService;

    private final ICycleService iCycleService;

    private final IPeriodService iPeriodService;

    private final IUserDataPermissionService iUserDataPermissionService;

    @Value("${day.value}")
    private int addDay;


    public AlarmTaskTime(IRemindService iRemindService, IMatterService matterService, ICycleService iCycleService, IPeriodService iPeriodService, IUserDataPermissionService iUserDataPermissionService) {
        this.iRemindService = iRemindService;
        this.matterService = matterService;
        this.iCycleService = iCycleService;
        this.iPeriodService = iPeriodService;
        this.iUserDataPermissionService = iUserDataPermissionService;
    }


    /**
     * 提醒时间
     * <p>
     * 定时任务
     * 提醒时间到来当天触发条件
     * 修改为紧急事项
     * 之后提醒时间删除
     *
     * @throws ParseException
     */
    @Scheduled(cron = "0 30 09 * * ?")//触发时间 秒 分 时
    public void selectMatterRemind() throws ParseException {
        //得到当前时间的yyyy-MM-dd
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String dateStr = simpleDateFormat.format(date);
        QueryWrapper<Remind> remindQueryWrapper = new QueryWrapper<>();
        //不是用户创建
        remindQueryWrapper.eq("USER_BY", 0);
        //已激活
        remindQueryWrapper.eq("IS_ACTIVATE", 1);
        remindQueryWrapper.eq("REMIND_STR", dateStr);
        List<Remind> reminds = iRemindService.list(remindQueryWrapper);
        if (reminds.size() > 0) {
            reminds.forEach(remind -> {
                QueryWrapper<UserMatter> userMatterQueryWrapper = new QueryWrapper<>();
                userMatterQueryWrapper.eq("MATTER_ID", remind.getMatterId());
                userMatterQueryWrapper.eq("FINISH", 0);
                userMatterQueryWrapper.eq("IS_REMIND", 0);
                matterService.findUserMatters(userMatterQueryWrapper);
            });
        }
    }

    /**
     * @throws ParseException
     */
    @Scheduled(cron = "10 30 09 * * ?")//触发时间 秒 分 时
    public void selectMatterRemindByOne() throws ParseException {
        //得到当前时间的yyyy-MM-dd
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String dateStr = simpleDateFormat.format(date);
        QueryWrapper<Remind> remindQueryWrapper = new QueryWrapper<>();
        //不是用户创建
        remindQueryWrapper.eq("USER_BY", 1);
        //已激活
        remindQueryWrapper.eq("IS_ACTIVATE", 1);
        remindQueryWrapper.eq("REMIND_STR", dateStr);
        List<Remind> reminds = iRemindService.list(remindQueryWrapper);
        if (reminds.size() > 0) {
            reminds.forEach(remind -> {
                QueryWrapper<UserMatter> userMatterQueryWrapper = new QueryWrapper<>();
                userMatterQueryWrapper.eq("MATTER_ID", remind.getMatterId());
                userMatterQueryWrapper.eq("FINISH", 0);
                userMatterQueryWrapper.eq("IS_REMIND", 1);
                matterService.findUserMatters(userMatterQueryWrapper);
            });
        }
    }

    /**
     * 结束时间
     * 结束时间提前15天设为紧急
     *
     * @throws ParseException
     */
    @Scheduled(cron = "0 30 09 * * ?")
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
     * 个人周期循环 月
     */
    @Scheduled(cron = "0 30 09 * * ?")
    public void forEachOne() throws ParseException {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -1);
        //往前推一个月
        Date before = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = simpleDateFormat.format(before);
        Matter matter = new Matter();
        matter.setMatterOpenStr(dateStr);
        matter.setForEach(1);
        matter.setIsOpen(0);
        matter.setIsPatriarch(0);
        //
        List<Matter> list = matterService.findMatters(matter);
        if (list.size() > 0) {
            list.forEach(dao -> {
                dao.setForEach(0);
                System.err.println("定时任务的dao:" + dao);
                Long userId = Long.valueOf(dao.getUserId());
                Long oldId = dao.getMatterId();
                matterService.saveOrUpdate(dao);
                dao.setMatterId(null);
                dao.setMatterOpen(now);
                calendar.setTime(dao.getEnd());
                calendar.add(Calendar.MONTH, +1);
                dao.setEnd(calendar.getTime());
                dao.setForEach(1);
                matterService.saveOrUpdate(dao);
                Long newId = matterService.maxMatterId();
                matterService.userMatter(oldId, newId, userId);
            });
        }
    }


    /**
     * 周期定时
     */
    @Scheduled(cron = "0 30 09 * * ?")
    public void cycleMatter() {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String str = simpleDateFormat.format(now);
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeLeft("OPEN_STR", str);
        List<Period> periods = iPeriodService.list(queryWrapper);
        if (periods.size() > 0) {
            periods.forEach(period -> {
                QueryWrapper<UserDataPermission> queryWrapperU = new QueryWrapper<>();
                queryWrapperU.eq("PERIOD_ID", period.getPeriodId());
                List<UserDataPermission> list = iUserDataPermissionService.list(queryWrapperU);
                if (list.size() > 0) {
                    list.forEach(dao -> {
                        //得到matter
                        Matter matter = matterService.getById(dao.getMatterId());
                        if (matter != null && matter.getIsOpen() == 0) {
                            Long oldId = matter.getMatterId();
                            matter.setIsPatriarch(0);
                            matter.setPatriarchId(matter.getMatterId());
                            matter.setMatterOpen(new Date());
                            try {
                                matter.setEnd(getEndTime(period));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            matter.setCreateTime(new Date());
                            matter.setCycleId(period.getPeriodId());
                            matter.setForEach(null);
                            matter.setPeriod(null);
                            matter.setMatterId(null);
                            //存储
                            matterService.saveOrUpdate(matter);
                            Long newId = matterService.maxMatterId();
                            //用户事项映射
                            matterService.copyUserMatter(oldId, newId);
                            //提醒时间
                            matterService.copyReminds(period, newId);
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
        calendar.add(calendar.DATE, addDay);
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!::" + addDay);
        date = calendar.getTime();
        String end = simpleDateFormat.format(date);
        return end;
    }

    /**
     * 结束时间
     *
     * @param period
     * @return
     * @throws ParseException
     */
    private Date getEndTime(Period period) throws ParseException {
        SimpleDateFormat MMdd = new SimpleDateFormat("MM-dd");
        SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
        String year = yyyy.format(new Date());
        String mw = MMdd.format(period.getPeriodEnd());
        String endStr = year + "-" + mw;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date end = simpleDateFormat.parse(endStr);
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
        /*selectMatterRemind();
        selectMatterRemindByOne();
        endTimeRemind();
        forEachOne();
        cycleMatter();*/
    }
}
