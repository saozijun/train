package com.cen.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 站点实体类
 * @author volcano
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_station")
public class Station implements Serializable {

    private static final long serialVersionUID = 1L;

    // id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 关联的列车ID
    private Long trainId;

    // 站点名称
    private String name;

    // 到达时间
    private String arriveTime;

    // 发车时间
    private String departTime;

    // 停留时间(分钟)
    private Integer stop;

    // 距离起点站的距离(km)
    private Integer distance;

    // 站点经度
    private Double longitude;

    // 站点纬度
    private Double latitude;

    // 站点序号(用于排序)
    private Integer sequence;
} 