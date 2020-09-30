package cc.mrbird.febs.system.service;


import cc.mrbird.febs.system.entity.Cycle;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
public interface ICycleService extends IService<Cycle> {

}
