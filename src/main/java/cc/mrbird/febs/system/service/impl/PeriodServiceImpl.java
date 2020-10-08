package cc.mrbird.febs.system.service.impl;

import cc.mrbird.febs.common.entity.DeptTree;
import cc.mrbird.febs.common.utils.TreeUtil;
import cc.mrbird.febs.system.entity.Dept;
import cc.mrbird.febs.system.entity.Period;
import cc.mrbird.febs.system.mapper.PeriodMapper;
import cc.mrbird.febs.system.service.IPeriodService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: weiZiHao
 * @create: 2020-09-10 15:31
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class PeriodServiceImpl extends ServiceImpl<PeriodMapper, Period> implements IPeriodService {


    @Override
    public List<DeptTree<Period>> findPeriods(Period period) {
        QueryWrapper<Period> queryWrapper = new QueryWrapper<>();
        //List<Dept> depts = this.baseMapper.selectList(queryWrapper);
        queryWrapper.setEntity(period);
        List<Period> periods = this.baseMapper.selectList(queryWrapper);
        List<DeptTree<Period>> trees = this.convertPeriods(periods);
        return TreeUtil.buildDeptTree(trees);
    }

    private List<DeptTree<Period>> convertPeriods(List<Period> periods) {
        List<DeptTree<Period>> trees = new ArrayList<>();
        periods.forEach(period -> {
            DeptTree<Period> tree = new DeptTree<>();
            tree.setId(String.valueOf(period.getPeriodId()));
            tree.setParentId(String.valueOf(period.getParentId()));
            tree.setName(period.getPeriodName());
            tree.setData(period);
            trees.add(tree);
        });
        return trees;
    }
}
