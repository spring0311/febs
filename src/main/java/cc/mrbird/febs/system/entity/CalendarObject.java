package cc.mrbird.febs.system.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 日历处理对象
 *
 * @author weiZiHao
 * @date 2020/9/18
 */
@Data
public class CalendarObject implements Serializable {
    private static final long serialVersionUID = -531572968450982996L;

    private String name;

    private String colour;
    /**
     * ['l1', 'l2', 'l3', 'l4']
     * 红     橙    蓝    绿
     */
    private Object[] value;

}
