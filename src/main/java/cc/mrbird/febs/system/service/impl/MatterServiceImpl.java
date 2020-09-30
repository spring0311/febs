package cc.mrbird.febs.system.service.impl;

import cc.mrbird.febs.common.entity.FebsConstant;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import cc.mrbird.febs.system.entity.Matter;
import cc.mrbird.febs.system.entity.Team;
import cc.mrbird.febs.system.entity.User;
import cc.mrbird.febs.system.entity.UserMatter;
import cc.mrbird.febs.system.mapper.DeptMapper;
import cc.mrbird.febs.system.mapper.MatterMapper;
import cc.mrbird.febs.system.mapper.TeamMapper;
import cc.mrbird.febs.system.mapper.UserMatterMapper;
import cc.mrbird.febs.system.service.IMatterService;
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

import javax.jws.soap.SOAPBinding;
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
    private final DeptMapper deptMapper;
    private final UserMatterMapper userMatterMapper;
    private final TeamMapper teamMapper;


    @Override
    public IPage<Matter> findMatters(QueryRequest request, Matter matter) {
        //System.err.println("MatterService:start...");
        //LambdaQueryWrapper<Matter> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(matter.getCreateTimeFrom()) &&
                StringUtils.equals(matter.getCreateTimeFrom(), matter.getCreateTimeTo())) {
            matter.setCreateTimeFrom(matter.getCreateTimeFrom() + " 00:00:00");
            matter.setCreateTimeTo(matter.getCreateTimeTo() + " 23:59:59");
        }
        // TODO 设置查询条件
        Page<Matter> page = new Page<>(request.getPageNum(), request.getPageSize());
        page.setSearchCount(false);
        List<Matter> matters = baseMapper.findMatterDetail(matter);
        //放入完成人数
        //未完成
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        //已完成
        QueryWrapper<UserMatter> queryWrapper1 = new QueryWrapper<>();
        if (matters.size() > 0) {
            matters.forEach(matterDao -> {
                queryWrapper.eq("FINISH", 0);
                queryWrapper1.eq("FINISH", 1);
                queryWrapper.eq("MATTER_ID", matterDao.getMatterId());
                queryWrapper1.eq("MATTER_ID", matterDao.getMatterId());
                List<UserMatter> noOver = userMatterMapper.selectList(queryWrapper);
                List<UserMatter> over = userMatterMapper.selectList(queryWrapper1);
                queryWrapper.clear();
                queryWrapper1.clear();
                matterDao.setOver(over.size());
                matterDao.setNoOver(noOver.size());
                System.err.println(matterDao);
            });
        }
        //page.setTotal(baseMapper.countMatterDetail(matter));
        page.setTotal(matters.size());
        //System.err.println("MatterService:" + matter);
        SortUtil.handlePageSort(request, page, "matterId", FebsConstant.ORDER_ASC, false);
        return this.baseMapper.findMatterDetailPage(page, matter);
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
        page.setSearchCount(false);
        List<Matter> matters = baseMapper.findMatterDetail(matter);
        //page.setTotal(baseMapper.countMatterDetail(matter));
        page.setTotal(matters.size());
        //page.setTotal(baseMapper.countMatterDetailForOne(matter));
        //System.err.println("MatterService:" + matter);
        SortUtil.handlePageSort(request, page, "matterId", FebsConstant.ORDER_ASC, false);
        return this.baseMapper.findMatterDetailPage(page, matter);
    }

    /*@Override
    public IPage<Matter> findMatters(QueryRequest request, Matter matter) {
        LambdaQueryWrapper<Matter> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<Matter> page = new Page<>(request.getPageNum(), request.getPageSize());
        List<Matter> matters = this.baseMapper.selectList(queryWrapper);
        for (Matter matter_ : matters
        ) {
            Dept dept = deptMapper.selectById(matter_.getDeptId());
            matter_.setDeptName(dept.getDeptName());
        }
        return this.page(page, queryWrapper);
    }*/

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
        Map<String, Object> map = new HashMap<>();
        map.put("matter_id", matterId);
        userMatterMapper.deleteByMap(map);
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

    /**
     * 管理员创建新事项
     *
     * @param matter matter
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMatter(Matter matter) throws ParseException {
        Date date = new Date();
        matter.setCreateTime(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        matter.setEnd(simpleDateFormat.parse(matter.getEndStr()));
        matter.setMatterOpen(simpleDateFormat.parse(matter.getMatterOpenStr()));
        //先行得到用户ID字符串
        String[] userIds = null;
        System.err.println("createMatter:matter:" + matter);
        if (!"".equals(matter.getTeamId())) {
            System.err.println("If判断开始");
            userIds = getUserIds(matter);
            System.err.println("If判断结束");
        } else {
            String userId = matter.getUserId();
            userIds = userId.split(",");
        }
        matterMapper.insert(matter);
        //查询到插入后的matterId
        Long matterId = matterMapper.findMaxId();
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
        String[] userIdss = userIds.split(",");
        if ("1".equals(matter.getIsOpen()) || matter.getIsOpen() == 1) {
            System.err.println("updateMatter:修改完成状态");
            userMatter.setFinish(0);
        }
        for (String userId : userIdss
        ) {
            Long userLongId = Long.parseLong(userId);
            userMatter.setUserId(userLongId);
            userMatterMapper.insert(userMatter);
        }
        this.saveOrUpdate(matter);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMatter1(Matter matter) {
        //查询条件
        Matter select = new Matter();
        select.setMatterId(matter.getMatterId());
        //数据库映射数据
        //Matter dao = matterMapper.findMatterDetail(select).get(0);
        //String userIdDao = dao.getUserId();//数据库用户映射
        //String[] userIdsDao = userIdDao.split(",");
        //String teamIdDao = dao.getTeamId();//数据库组映射
        //String[] teamIdsDao = teamIdDao.split(",");
        //修改的数据
        //String[] userIds = matter.getUserId().split(",");
        String[] userIds = null;
        String[] teamIds = matter.getTeamId().split(",");
        //String[] teamIds = null;
        //找不同
        //List<String> userId = compare(userIdsDao, userIds);
        //List<String> teamId = compare(teamIdsDao, teamIds);
        //判断
        if (!"".equals(matter.getTeamId())) {
            System.err.println("If判断开始");
            userIds = getUserIds(matter);
            System.err.println("If判断结束");
        } else {
            String userId = matter.getUserId();
            userIds = userId.split(",");
        }
        List<String> listUserId = Arrays.asList(userIds);
        QueryWrapper<UserMatter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MATTER_ID", matter.getMatterId());
        List<UserMatter> userMatters = userMatterMapper.selectList(queryWrapper);
        List<Long> ids = new ArrayList<>();
        if (userMatters.size() > 0) {
            userMatters.forEach(userMatter -> {
                listUserId.forEach(l -> {
                    Long id = Long.valueOf(l);
                    if (userMatter.getUserId() == id) {
                        ids.add(userMatter.getUserId());
                    }
                });
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMatter2(Matter matter) {
        //查询条件
        Matter select = new Matter();
        select.setMatterId(matter.getMatterId());
        //查询结果
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
        String[] userIdss = userIds.split(",");
        if ("1".equals(matter.getIsOpen()) || matter.getIsOpen() == 1) {
            System.err.println("updateMatter:修改完成状态");
            userMatter.setFinish(0);
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
