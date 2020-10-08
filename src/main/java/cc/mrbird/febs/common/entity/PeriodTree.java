package cc.mrbird.febs.common.entity;

import cc.mrbird.febs.common.annotation.Limit;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: weiZiHao
 * @create: 2020-10-07 11:32
 */
@Data
public class PeriodTree implements Serializable {


    private static final long serialVersionUID = -537664874697160000L;

    private String name;
    private String value;
    private List<PeriodTree> children;
}
