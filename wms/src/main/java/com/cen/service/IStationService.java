package com.cen.service;

import com.cen.entity.Station;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.common.Result;

import java.util.List;

/**
 * <p>
 *  站点服务类
 * </p>
 *
 * @author volcano
 * 
 */
public interface IStationService extends IService<Station> {
    Result saveStation(Station station);
    
    List<Station> getStationsByTrainId(Long trainId);
    
    Result saveStationBatch(List<Station> stations);
} 