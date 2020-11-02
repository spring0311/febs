package cc.mrbird.febs.system.service.impl;

import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import cc.mrbird.febs.system.entity.*;
import cc.mrbird.febs.system.mapper.*;
import cc.mrbird.febs.system.service.IMatterService;
import cc.mrbird.febs.system.service.IPeriodService;
import cc.mrbird.febs.system.service.IRemindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
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
 * @author MrBird
 * @date 2020-07-16 14:57:57
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class MatterServiceImpl extends ServiceImpl<MatterMapper, Matter> implements IMatterService {


    private final MatterMapper matterMapper;
    private final UserMatterMapper userMatterMapper;
    private final TeamMapper teamMapper;
    private final UserDataPermissionMapper userDataPermissionMapper;
    private final IRemindService remindService;
    private final IPeriodService periodService;

    @Override
    public IPage<Matter> findMatters(QueryRequest request, Matter matter) {
        if (StringUtils.isNotBlank(matter.getCreateTimeFrom()) &&
                StringUtils.equals(matter.getCreateTimeFrom(), matter.getCreateTimeTo())) {
            matter.setCreateTimeFrom(matter.getCreateTimeFrom() + " 00:00:00");
            matter.setCreateTimeTo(matter.getCreateTimeTo() + " 23:59:59");
        }
        // TODO 设置查询条件
        Page<Matter> page = new Page<>(request.getPageNum(), request.getPageSize());
        List<Matter> list = baseMapper.findMatterDetail(matter);
        page.setTotal(list.size());
        SortUtil.handlePageSort(request, page, "matterId", FebsConstant.ORDER_ASC, false);
        IPage<Matter> iPage = this.baseMapper.findMatterDetailPage(page, matter);
        List<Matter> matters = iPage.getRecords();

        if (matters.size() > 0) {
            matters.forEach(matterDao -> {

                if (matterDao.getName() != null) {
                    matterDao.setName(removeMove(matterDao.getName()));
                }
                matterDao.setColor(color(matterDao));
            });
        }
        iPage.setRecords(matters);
        return iPage;
    }

    @Override
    public IPage<Matter> findMatterOut(QueryRequest request, Matter matter) {
        System.err.println(matter);
        QueryWrapper<Matter> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(matter);
        List<Matter> list = matterMapper.selectList(queryWrapper);
        Page<Matter> page = new Page<>(request.getPageNum(), request.getPageSize());
        QueryWrapper<Matter> matterQueryWrapper = new QueryWrapper<>();
        if (matter.getDeptId() == null) {
            matterQueryWrapper.eq("PATRIARCH_ID", matter.getPatriarchId());
            matterQueryWrapper.eq("IS_PATRIARCH", matter.getIsPatriarch());
        } else {
            matterQueryWrapper.setEntity(matter);
        }
        SortUtil.handlePageSort(request, page, null, FebsConstant.ORDER_ASC, false);
        Page<Matter> iPage = this.baseMapper.selectPage(page, matterQueryWrapper);
        List<Matter> matters = iPage.getRecords();
        //放入完成人数
        //未完成
        QueryWrapper<UserMatter> noOver = new QueryWrapper<>();
        //已完成
        QueryWrapper<UserMatter> over = new QueryWrapper<>();
        //
        Matter select = new Matter();
        if (matters.size() > 0) {
            matters.forEach(dao -> {
                //完成未完成处理
                noOver.eq("FINISH", 0);
                over.eq("FINISH", 1);
                noOver.eq("MATTER_ID", dao.getMatterId());
                over.eq("MATTER_ID", dao.getMatterId());
                List<UserMatter> noOverNum = userMatterMapper.selectList(noOver);
                List<UserMatter> overNum = userMatterMapper.selectList(over);
                noOver.clear();
                over.clear();
                dao.setNoOver(noOverNum.size());
                if (noOverNum.size() > 0) {
                    dao.setNoOverName(findOverOrNoOver(dao.getMatterId(), 0));
                }
                dao.setOver(overNum.size());
                if (overNum.size() > 0) {
                    dao.setOverName(findOverOrNoOver(dao.getMatterId(), 1));
                }
                select.setMatterId(dao.getPatriarchId());
                dao.setName(removeMove(baseMapper.selectName(dao)));
            });
        }
        iPage.setRecords(matters);
        return iPage;
    }

    private String findOverOrNoOver(Long matterId, Integer finish) {
        UserMatter userMatter = new UserMatter();
        userMatter.setMatterId(matterId);
        userMatter.setFinish(finish);
        String name = "";
        name = userMatterMapper.selectName(userMatter);
        return name;
    }

    /**
     * 1 重要紧急 2重要不紧急 3不重要紧急 4不重要不紧急
     *
     * @param matter
     * @return
     */
    private Integer color(Matter matter) {
        Integer color = 0;
        if (matter.getImportant() == 1 && matter.getUrgent() == 1) {
            color = 1;
        } else if (matter.getImportant() == 1 && matter.getUrgent() == 0) {
            color = 2;
        } else if (matter.getImportant() == 0 && matter.getUrgent() == 1) {
            color = 3;
        } else {
            color = 4;
        }
        return color;
    }

    private Integer colorOne(Matter matter) {
        Integer color = 0;
        if (matter.getImportantOne() == 1 && matter.getUrgentOne() == 1) {
            color = 1;
        } else if (matter.getImportantOne() == 1 && matter.getUrgentOne() == 0) {
            color = 2;
        } else if (matter.getImportantOne() == 0 && matter.getUrgentOne() == 1) {
            color = 3;
        } else {
            color = 4;
        }
        return color;
    }

    private String removeMove(String name) {
        String ret = "";
        if (name.length() == 0) {
            return null;
        } else {
            String[] strings = name.split(",");
            ArrayList list = new ArrayList();
            for (int i = 0; i < strings.length; i++) {
                if (!list.contains(strings[i]))
                    list.add(strings[i]);
            }
            for (int i = 0; i < list.size(); i++) {
                ret = ret + list.get(i) + ",";
            }
            ret = ret.substring(0, ret.length() - 1);
            return ret;
        }
    }

    @Override
    public IPage<Matter> findMattersForOne(QueryRequest request, Matter matter) {
        if (StringUtils.isNotBlank(matter.getCreateTimeFrom()) &&
                StringUtils.equals(matter.getCreateTimeFrom(), matter.getCreateTimeTo())) {
            matter.setCreateTimeFrom(matter.getCreateTimeFrom() + " 00:00:00");
            matter.setCreateTimeTo(matter.getCreateTimeTo() + " 23:59:59");
        }
        // TODO 设置查询条件
        Page<Matter> page = new Page<>(request.getPageNum(), request.getPageSize());
        List<Matter> matters = baseMapper.findMatterDetail(matter);
        matters.forEach(dao -> {
            dao.setColor(color(dao));
        });
        //page.setTotal(baseMapper.countMatterDetail(matter));
        page.setTotal(matters.size());
        //page.setTotal(baseMapper.countMatterDetailForOne(matter));
        //System.err.println("MatterService:" + matter);
        SortUtil.handlePageSort(request, page, "matterId", FebsConstant.ORDER_ASC, false);
        IPage<Matter> iPage = this.baseMapper.findMatterDetailPage(page, matter);
        List<Matter> list = iPage.getRecords();
        list.forEach(dao -> {
            dao.setColor(colorOne(dao));
            dao.setName(removeMove(baseMapper.selectName(dao)));
        });
        iPage.setRecords(list);
        return iPage;
    }

    @Override
    public List<Matter> findMatters(Matter matter) {
        LambdaQueryWrapper<Matter> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        List<Matter> matters = matterMapper.findMatterDetail(matter);
        return matterMapper.findMatterDetail(matter);
    }

    @Override
    public void deleteMatterById(Long matterId) {
        // TODO Auto-generated method stub
        //System.err.println("MatterService:deleteMatterById"+matterId);
        deleteAllByMatterId(matterId);
        QueryWrapper<Matter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PATRIARCH_ID", matterId);
        List<Matter> matters = matterMapper.selectList(queryWrapper);
        if (matters.size() > 0) {
            matters.forEach(dao -> {
                deleteAllByMatterId(dao.getMatterId());
            });
        }
    }

    private void deleteAllByMatterId(Long matterId) {
        Map<String, Object> map = new HashMap<>();
        map.put("MATTER_ID", matterId);
        userDataPermissionMapper.deleteByMap(map);
        userMatterMapper.deleteByMap(map);
        remindService.removeByMap(map);
        matterMapper.deleteById(matterId);
    }

    @Override
    public void updateMatterUrgent(Long matterId) {
        //修改t_matter的urgent
        matterMapper.updateMatterUrgent(matterId);
        //修改t_user_matter的urgent_one
        //非空条件字段
        UserMatter userMatter = new UserMatter();
        userMatter.setUrgentOne(1);
        //查询条件
        UserMatter userMatter1 = new UserMatter();
        userMatter1.setMatterId(matterId);
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(userMatter1);
        userMatterMapper.update(userMatter, queryWrapper);
    }

    @Override
    public List<Long> selectMatterIdsByEndTime(String date) {
        return matterMapper.findIdsByEndTime(date);
    }

    @Override
    public void selectMatterIdsByForEach(Integer forEach, String date) {
        matterMapper.updateIsOpenByEach(forEach, date);
    }


    @Override
    public void updateIsOpen(String date) {
        matterMapper.updateIsOpen(date);
    }

    @Override
    public void finishMatter(String value) {
        String[] ids = value.split(",");
        Long matterId = Long.valueOf(ids[0]);
        Long userId = Long.valueOf(ids[1]);
        Map<String, Object> map = new HashMap<>();
        map.put("MATTER_ID", matterId);
        map.put("USER_ID", userId);
        UserMatter userMatter = userMatterMapper.selectByMap(map).get(0);
        userMatter.setFinish(1);
        userMatter.setActuallyTime(new Date());
        System.err.println("finishMatter:" + userMatter);
        userMatterMapper.updateById(userMatter);
    }

    @Override
    public void finishMatterNo(String value) {
        String[] ids = value.split(",");
        Long matterId = Long.valueOf(ids[0]);
        Long userId = Long.valueOf(ids[1]);
        Map<String, Object> map = new HashMap<>();
        map.put("MATTER_ID", matterId);
        map.put("USER_ID", userId);
        UserMatter userMatter = userMatterMapper.selectByMap(map).get(0);
        userMatter.setFinish(0);
        System.err.println("finishMatter:" + userMatter);
        userMatterMapper.updateById(userMatter);
    }

    @Override
    public void changeFinish(List<Long> matterIds) {
        UserMatter userMatter = new UserMatter();
        matterIds.forEach(matterId -> {
            UserMatter userMatterDao = new UserMatter();
            userMatterDao.setFinish(0);
            QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("MATTER_ID", matterId);
            this.userMatterMapper.update(userMatterDao, queryWrapper);
        });
    }

    @Override
    public void copyUserMatter(Long oldMatterId, Long newMatterId) {
        Map<String, Object> map = new HashMap<>();
        map.put("MATTER_ID", oldMatterId);
        //根据老ID查询结果
        List<UserMatter> userMatters = userMatterMapper.selectByMap(map);
        if (userMatters.size() > 0) {
            userMatters.forEach(userMatter -> {
                //id为空 未完成 新matterId
                userMatter.setTUserMatterId(null);
                userMatter.setMatterId(newMatterId);
                userMatter.setFinish(0);
                //插入
                userMatterMapper.insert(userMatter);
            });
        }
    }

    @Override
    public Long maxMatterId() {
        return matterMapper.findMaxId();
    }

    @Override
    public List<UserMatter> findUserMatters(QueryWrapper<UserMatter> userMatterQueryWrapper) {
        List<UserMatter> list = userMatterMapper.selectList(userMatterQueryWrapper);
        if (list.size() > 0) {
            list.forEach(dao -> {
                dao.setUrgentOne(1);
                userMatterMapper.updateById(dao);
            });
        }
        return userMatterMapper.selectList(userMatterQueryWrapper);
    }

    @Override
    public void copyReminds(Period period, Long newId) {
        QueryWrapper<Remind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PERIOD_ID", period.getPeriodId());
        List<Remind> reminds = remindService.list(queryWrapper);
        if (reminds.size() > 0) {
            reminds.forEach(remind -> {
                remind.setMatterId(newId);
                remind.setIsActivate(1);
                remind.setPeriodId(null);
                remind.setRemindId(null);
                remindService.saveOrUpdate(remind);
            });
        }
    }

    @Override
    public void userMatter(Long oldId, Long matterId, Long userId) {
        System.err.println("oldId:" + oldId);
        System.err.println("matterId:" + matterId);
        System.err.println("userId:" + userId);
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MATTER_ID", oldId);
        queryWrapper.eq("USER_ID", userId);
        UserMatter userMatter = userMatterMapper.selectList(queryWrapper).get(0);
        System.err.println("usermatter:" + userMatter);
        userMatter.setTUserMatterId(null);
        userMatter.setMatterId(matterId);
        userMatter.setFinish(0);
        userMatter.setActuallyTime(null);
        userMatterMapper.insert(userMatter);
    }

    /**
     * 管理员创建新事项
     *
     * @param matter matter
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMatter(Matter matter) throws ParseException {
        if ("".equals(matter.getUserId())) {
            matter.setUserId("0");
        }
        matter.setCreateTime(new Date());
        System.err.println(matter);
        //先行得到用户ID字符串
        String[] userIds = null;
        System.err.println("createMatter:matter:" + matter);
        if (!"".equals(matter.getTeamId())) {
            userIds = getUserIds(matter);
        } else {
            String userId = matter.getUserId();
            userIds = userId.split(",");
        }
        //!!!!!!!!
        String[] ids = matter.getCycleIdStr().split(",");
        Period period = periodService.getById(Long.valueOf(ids[0]));
        Period dao = periodService.getById(period.getParentId());
        matter.setPeriod(dao.getPeriodName());
        matter.setCycleId(dao.getPeriodId());
        matterMapper.insert(matter);
        //查询到插入后的matterId
        Long matterId = matterMapper.findMaxId();
        //周期映射插入
        insertInto(matterId, matter.getCycleIdStr());
        matter.setMatterId(matterId);
        //映射对象类 赋值存储
        UserMatter userMatter = new UserMatter();
        userMatter.setMatterId(matter.getMatterId());
        userMatter.setImportantOne(matter.getImportant());
        userMatter.setUrgentOne(matter.getUrgent());
        for (String userId : userIds
        ) {
            Long userLongId = Long.parseLong(userId);
            userMatter.setUserId(userLongId);
            //System.err.println("userMatter:" + userMatter);
            userMatterMapper.insert(userMatter);
        }
    }

    @Override
    public void createMatterOne(Matter matter) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        matter.setMatterOpen(simpleDateFormat.parse(matter.getMatterOpenStr()));
        matter.setEnd(simpleDateFormat.parse(matter.getEndStr()));
        matterMapper.insert(matter);
        Long matterId = matterMapper.findMaxId();
        UserMatter userMatter = new UserMatter();
        userMatter.setMatterId(matterId);
        userMatter.setUserId(matter.getLongUserId());
        userMatter.setImportantOne(matter.getImportant());
        userMatter.setUrgentOne(matter.getUrgent());
        userMatter.setIsRemind(1);
        userMatterMapper.insert(userMatter);
    }

    private void insertInto(Long matterId, String ids) {
        String[] strings = ids.split(",");
        UserDataPermission userDataPermission = new UserDataPermission();
        userDataPermission.setMatterId(matterId);
       /* for (String periodId : strings) {
            userDataPermission.setDeptId(System.currentTimeMillis());
            userDataPermission.setUserId(System.currentTimeMillis());
            userDataPermission.setPeriodId(Long.valueOf(periodId));
            System.err.println("insertInto:" + userDataPermission);
            userDataPermissionMapper.insert(userDataPermission);
        }*/
        Long time = System.currentTimeMillis();
        for (int i = 0; i < strings.length; i++) {
            time += 1;
            userDataPermission.setDeptId(time);
            userDataPermission.setUserId(time);
            userDataPermission.setPeriodId(Long.valueOf(strings[i]));
            userDataPermissionMapper.insert(userDataPermission);
        }
    }

    private String[] getUserIds(Matter matter) {
        String userIds = matter.getUserId();
        String teamIds = matter.getTeamId();
        String[] teamIdS = teamIds.split(",");
        for (String teamId : teamIdS
        ) {
            Team team = new Team();
            Long id = Long.valueOf(teamId);
            team.setTeamId(id);
            team = teamMapper.findTeams(team).get(0);
            userIds = userIds + "," + team.getUserId();
        }
        //得到用户Id去重复 转换为Set
        String[] userIdS = userIds.split(",");
        List<String> list = Arrays.asList(userIdS);
        Set<String> set = new HashSet(list);
        String[] ret = (String[]) set.toArray(new String[0]);
        return ret;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMatter(Matter matter) {
        if ("".equals(matter.getUserId())) {
            matter.setUserId("0");
        }
        System.err.println("updateMatter!!!!");
        UserMatter userMatter = new UserMatter();
        String userIds = matter.getUserId();
        //删除修改前映射数据
        Map<String, Object> map = new HashMap<>();
        map.put("MATTER_ID", matter.getMatterId());
        userMatterMapper.deleteByMap(map);
        //添加修改后映射数据
        userMatter.setImportantOne(matter.getImportant());
        userMatter.setUrgentOne(matter.getUrgent());
        userMatter.setMatterId(matter.getMatterId());
        String[] userIdss = null;
        if ("1".equals(matter.getIsOpen()) || matter.getIsOpen() == 1) {
            System.err.println("updateMatter:修改完成状态");
            userMatter.setFinish(0);
        }
        if (!"".equals(matter.getTeamId()) && matter.getTeamId() != null) {
            userIdss = getUserIds(matter);
        } else {
            String userId = matter.getUserId();
            userIdss = userId.split(",");
        }
        for (String userId : userIdss
        ) {
            Long userLongId = Long.parseLong(userId);
            userMatter.setUserId(userLongId);
            userMatterMapper.insert(userMatter);
        }
        this.saveOrUpdate(matter);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMatter(Matter matter) {
        LambdaQueryWrapper<Matter> wrapper = new LambdaQueryWrapper<>();
        // TODO 设置删除条件
        this.remove(wrapper);
    }

    /**
     * 找不同
     *
     * @param t1
     * @param t2
     * @param <T>
     * @return
     */
    public static <T> List<T> compare(T[] t1, T[] t2) {
        List<T> list1 = Arrays.asList(t1); //将t1数组转成list数组
        List<T> list2 = new ArrayList<T>();//用来存放2个数组中不相同的元素
        for (T t : t2) {
            if (!list1.contains(t)) {
                list2.add(t);
            }
        }
        return list2;
    }

}
