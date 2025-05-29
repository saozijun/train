package com.cen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cen.common.Constants;
import com.cen.common.Result;
import com.cen.entity.Station;
import com.cen.exception.ServiceException;
import com.cen.mapper.StationMapper;
import com.cen.service.IStationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  站点服务实现类
 * </p>
 *
 * @author volcano
 * 
 */
@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements IStationService {

    @Override
    public Result saveStation(Station station) {
        return Result.success(saveOrUpdate(station));
    }

    @Override
    public List<Station> getStationsByTrainId(Long trainId) {
        QueryWrapper<Station> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("train_id", trainId);
        queryWrapper.orderByAsc("sequence");
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public Result saveStationBatch(List<Station> stations) {
        if (stations == null || stations.isEmpty()) {
            throw new ServiceException(Constants.CODE_500, "站点数据不能为空");
        }
        
        // 获取列车ID
        Long trainId = stations.get(0).getTrainId();
        if (trainId == null) {
            throw new ServiceException(Constants.CODE_500, "列车ID不能为空");
        }
        
        // 删除该列车的所有站点
        QueryWrapper<Station> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("train_id", trainId);
        remove(queryWrapper);
        
        // 重新保存所有站点
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            station.setSequence(i);
            station.setTrainId(trainId);
        }
        
        return Result.success(saveBatch(stations));
    }
} 