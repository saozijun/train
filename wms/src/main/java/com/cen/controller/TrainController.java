package com.cen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cen.common.Result;
import com.cen.entity.Train;
import com.cen.entity.Station;
import com.cen.service.ITrainService;
import com.cen.service.IStationService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 列车控制器
 *
 * @author volcano
 */
@RestController
@RequestMapping("/train")
public class TrainController {

    @Resource
    private ITrainService trainService;
    
    @Resource
    private IStationService stationService;

    // 新增或修改列车
    @PostMapping("/save")
    public Result save(@RequestBody Train train) {
        return trainService.saveTrain(train);
    }

    // 删除列车
    @PostMapping("/delete")
    public Result delete(@RequestBody Train train) {
        return Result.success(trainService.removeById(train.getId()));
    }

    // 批量删除列车
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(trainService.removeBatchByIds(ids));
    }

    // 根据id获取列车
    @GetMapping("/getById")
    public Result findOne(@RequestParam Long id) {
        return Result.success(trainService.getById(id));
    }

    // 获取列车及其站点
    @GetMapping("/getWithStations")
    public Result getWithStations(@RequestParam Long id) {
        Train train = trainService.getById(id);
        if (train == null) {
            return Result.error(500,"列车不存在");
        }
        
        List<Station> stations = stationService.getStationsByTrainId(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("train", train);
        result.put("stations", stations);
        
        return Result.success(result);
    }

    // 获取所有列车
    @GetMapping("/list")
    public Result list() {
        return Result.success(trainService.list());
    }

    // 分页查询列车
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(defaultValue = "") String code,
                           @RequestParam(defaultValue = "") String type) {
        QueryWrapper<Train> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(Strings.isNotEmpty(code), "code", code);
        queryWrapper.like(Strings.isNotEmpty(type), "type", type);
        queryWrapper.orderByDesc("id");
        return Result.success(trainService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
    
    // 使用AI生成列车路线
    @PostMapping("/generate")
    public Result generateRoute() {
        try {
            return trainService.generateTrainRoute(null, null);
        } catch (Exception e) {
            return Result.error(500, "AI生成列车路线失败: " + e.getMessage());
        }
    }
    
    // 使用AI生成指定起点和终点的列车路线
    @PostMapping("/generateWithLocations")
    public Result generateRouteWithLocations(@RequestBody Map<String, String> params) {
        try {
            String start = params.get("start");
            String end = params.get("end");
            
            System.out.println("接收到生成请求 - 起点: " + start + ", 终点: " + end);
            
            return trainService.generateTrainRoute(start, end);
        } catch (Exception e) {
            return Result.error(500, "AI生成列车路线失败: " + e.getMessage());
        }
    }
} 