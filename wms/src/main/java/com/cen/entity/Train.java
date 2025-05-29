package com.cen.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 列车实体类
 * @author volcano
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_train")
public class Train implements Serializable {

    private static final long serialVersionUID = 1L;

    // id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 车次编号
    private String code;

    // 始发站
    private String fromStation;

    // 终点站
    private String toStation;

    // 列车类型
    private String type;

    // 总里程(km)
    private Integer distance;

    // 运行时间(秒)
    private Integer duration;

    // 最高速度(km/h)
    private Integer maxSpeed;

    // 列车颜色
    private String color;
} 