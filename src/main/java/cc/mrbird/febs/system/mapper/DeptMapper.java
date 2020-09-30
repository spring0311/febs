package cc.mrbird.febs.system.mapper;

import cc.mrbird.febs.system.entity.Dept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author MrBird
 */
public interface DeptMapper extends BaseMapper<Dept> {

    List<Dept> findDeptByDept(@Param("dept") Dept dept);
}
