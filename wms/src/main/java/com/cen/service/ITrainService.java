package com.cen.service;

import com.cen.entity.Train;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.common.Result;

/**
 * <p>
 *  列车服务类
 * </p>
 *
 * @author volcano
 * 
 */
public interface ITrainService extends IService<Train> {
    Result saveTrain(Train train);
    
    /**
     * 生成列车路线
     * @param start 起始站点（可选）
     * @param end 终点站点（可选）
     * @return 结果
     * @throws Exception
     */
    Result generateTrainRoute(String start, String end) throws Exception;
} 