package cc.mrbird.febs.system.service;


import cc.mrbird.febs.common.entity.DeptTree;
import cc.mrbird.febs.system.entity.Dept;
import cc.mrbird.febs.system.entity.Period;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 */
public interface IPeriodService extends IService<Period> {

    List<DeptTree<Period>> findPeriods(Period period);


}
