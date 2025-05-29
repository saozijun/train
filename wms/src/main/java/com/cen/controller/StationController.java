package com.cen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cen.common.Result;
import com.cen.entity.Station;
import com.cen.service.IStationService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 站点控制器
 *
 * @author volcano
 */
@RestController
@RequestMapping("/station")
public class StationController {

    @Resource
    private IStationService stationService;

    // 新增或修改站点
    @PostMapping("/save")
    public Result save(@RequestBody Station station) {
        return stationService.saveStation(station);
    }

    // 批量保存站点
    @PostMapping("/saveBatch")
    public Result saveBatch(@RequestBody List<Station> stations) {
        return stationService.saveStationBatch(stations);
    }

    // 删除站点
    @PostMapping("/delete")
    public Result delete(@RequestBody Station station) {
        return Result.success(stationService.removeById(station.getId()));
    }

    // 批量删除站点
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(stationService.removeBatchByIds(ids));
    }

    // 根据id获取站点
    @GetMapping("/getById")
    public Result findOne(@RequestParam Long id) {
        return Result.success(stationService.getById(id));
    }

    // 根据列车id获取站点
    @GetMapping("/getByTrainId")
    public Result getByTrainId(@RequestParam Long trainId) {
        List<Station> stations = stationService.getStationsByTrainId(trainId);
        return Result.success(stations);
    }

    // 分页查询站点
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(value = "trainId", required = false) Long trainId) {
        QueryWrapper<Station> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(Strings.isNotEmpty(name), "name", name);
        if (trainId != null) {
            queryWrapper.eq("train_id", trainId);
        }
        queryWrapper.orderByAsc("sequence");
        return Result.success(stationService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
} 