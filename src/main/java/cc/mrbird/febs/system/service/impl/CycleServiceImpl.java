package cc.mrbird.febs.system.service.impl;

import cc.mrbird.febs.system.entity.Cycle;
import cc.mrbird.febs.system.mapper.CycleMapper;
import cc.mrbird.febs.system.service.ICycleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: weiZiHao
 * @create: 2020-09-10 15:29
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class CycleServiceImpl extends ServiceImpl<CycleMapper, Cycle> implements ICycleService {

}
