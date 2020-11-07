package cc.mrbird.febs.system.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: weiZiHao
 * @create: 2020-11-05 16:37
 */
@Data
public class Echarts implements Serializable {

    private static final long serialVersionUID = 1968683148600337203L;

    private String name;

    private Integer value;
}
