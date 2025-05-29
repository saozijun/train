package com.cen.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cen.common.Constants;
import com.cen.common.Result;
import com.cen.entity.Station;
import com.cen.entity.Train;
import com.cen.exception.ServiceException;
import com.cen.mapper.TrainMapper;
import com.cen.service.IStationService;
import com.cen.service.ITrainService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <p>
 *  列车服务实现类
 * </p>
 *
 * @author volcano
 * 
 */
@Service
public class TrainServiceImpl extends ServiceImpl<TrainMapper, Train> implements ITrainService {

    @Value("${ai-api-key}")
    private String apiKey;
    
    @Resource
    private Generation generation;
    
    @Resource
    private IStationService stationService;

    @Override
    public Result saveTrain(Train train) {
        QueryWrapper<Train> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", train.getCode());
        Train one = getOne(queryWrapper);
        if (one == null) {
            return Result.success(saveOrUpdate(train));
        } else {
            if (train.getId() != null) {
                QueryWrapper<Train> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("id", train.getId());
                Train upOne = getOne(queryWrapper2);
                if (train.getCode().equals(upOne.getCode())) {
                    return Result.success(saveOrUpdate(train));
                } else {
                    throw new ServiceException(Constants.CODE_500, "修改失败,车次已存在");
                }
            } else {
                throw new ServiceException(Constants.CODE_500, "车次已存在");
            }
        }
    }
    
    @Override
    @Transactional
    public Result generateTrainRoute(String start, String end) throws Exception {
        // 输出接收到的参数
        System.out.println("生成路线 - 服务层接收参数 - 起点: " + start + ", 终点: " + end);
        
        // 1. 构建AI提示
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请为我生成一条中国铁路线路，包括列车信息和沿途站点详情。");
        
        // 添加多样性要求
        promptBuilder.append("请创造性地随机选择不同城市间的路线，不要总是选择北京到上海等热门路线。");
        promptBuilder.append("你可以考虑以下城市组合：成都-重庆、广州-深圳、西安-兰州、哈尔滨-长春、南京-杭州、武汉-长沙、乌鲁木齐-吐鲁番、拉萨-林芝等，或者其他有创意的组合。");
        promptBuilder.append("请随机选择列车类型（复兴号、和谐号、动车组、普通列车），随机设置合理的速度、距离和站点。");
        
        // 添加更多多样性要求
        int randomValue = new Random().nextInt(10);
        if (randomValue < 3) {
            promptBuilder.append("请选择中国西部城市之间的路线，如：西安、兰州、成都、重庆、昆明、拉萨等。");
        } else if (randomValue < 6) {
            promptBuilder.append("请选择中国南部城市之间的路线，如：广州、深圳、南宁、海口、福州、厦门等。");
        } else if (randomValue < 9) {
            promptBuilder.append("请选择中国东北城市之间的路线，如：哈尔滨、长春、沈阳、大连等。");
        } else {
            promptBuilder.append("请选择中国中部城市之间的路线，如：武汉、长沙、郑州、合肥等。");
        }
        
        // 如果提供了起始站和终点站，则添加到提示中
        boolean hasSpecifiedLocations = false;
        
        if (start != null && !start.trim().isEmpty()) {
            promptBuilder.append("【重要】起始站必须是：").append(start.trim()).append("。");
            hasSpecifiedLocations = true;
        }
        
        if (end != null && !end.trim().isEmpty()) {
            promptBuilder.append("【重要】终点站必须是：").append(end.trim()).append("。");
            hasSpecifiedLocations = true;
        }
        
        // 如果指定了站点，强调必须按照指定
        if (hasSpecifiedLocations) {
            promptBuilder.append("【请特别注意】必须严格按照上述指定的站点生成路线，这是最重要的要求。");
        }
        
        promptBuilder.append("请以JSON格式返回，不要包含任何解释，只返回JSON。JSON结构如下：\n" +
                "{\n" +
                "  \"train\": {\n" +
                "    \"code\": \"车次编号，例如G1234\",\n" +
                "    \"fromStation\": \"始发站名称\",\n" +
                "    \"toStation\": \"终点站名称\",\n" +
                "    \"type\": \"列车类型，只能是以下之一：复兴号、和谐号、动车组、普通列车\",\n" +
                "    \"distance\": 总里程，单位km，仅数字,\n" +
                "    \"duration\": 总运行时间，单位秒，仅数字（必须确保这个时间与站点间的出发和到达时间差一致）,\n" +
                "    \"maxSpeed\": 最高速度，单位km/h，仅数字,\n" +
                "    \"color\": \"列车颜色，例如#FF0000\"\n" +
                "  },\n" +
                "  \"stations\": [\n" +
                "    {\n" +
                "      \"name\": \"站点名称\",\n" +
                "      \"arriveTime\": \"到达时间，格式HH:MM，对于始发站可留空\",\n" +
                "      \"departTime\": \"发车时间，格式HH:MM，对于终点站可留空\",\n" +
                "      \"stop\": 停留时间，单位分钟，仅数字,\n" +
                "      \"distance\": 距离始发站距离，单位km，仅数字,\n" +
                "      \"longitude\": 经度，例如116.4074,\n" +
                "      \"latitude\": 纬度，例如39.9042\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n注意：列车类型必须是以下四种之一：复兴号、和谐号、动车组、普通列车。记住，只返回JSON格式，不要有任何其他文字。");
        
        String prompt = promptBuilder.toString();
        
        // 打印提示内容，方便调试
        System.out.println("AI提示内容: " + prompt);

        // 2. 调用AI接口
        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        // 生成一个随机种子，确保每次生成不同的结果
        Random random = new Random();
        
        GenerationParam param = GenerationParam.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(userMessage))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.9) // 增加topP参数，增加随机性
                .apiKey(apiKey)
                .enableSearch(true)
                .build();
                
        GenerationResult generationResult = generation.call(param);
        String aiResponse = generationResult.getOutput().getChoices().get(0).getMessage().getContent();
        
        // 打印AI返回的原始响应，帮助调试
        System.out.println("AI原始响应: " + aiResponse);
        
        // 尝试清理响应，去除可能的markdown代码块标记
        aiResponse = cleanJsonResponse(aiResponse);
        
        // 3. 解析AI返回的JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(aiResponse);
            
            // 验证JSON结构是否符合预期
            if (!rootNode.has("train") || !rootNode.has("stations")) {
                throw new ServiceException(Constants.CODE_500, "AI返回数据缺少必要字段: train或stations");
            }
            
        } catch (JsonProcessingException e) {
            System.err.println("JSON解析失败: " + e.getMessage());
            System.err.println("问题JSON: " + aiResponse);
            throw new ServiceException(Constants.CODE_500, "AI返回数据格式错误: " + e.getMessage());
        }
        
        try {
            // 4. 创建列车对象
            JsonNode trainNode = rootNode.get("train");
            Train train = new Train();
            train.setCode(trainNode.get("code").asText());
            train.setFromStation(trainNode.get("fromStation").asText());
            train.setToStation(trainNode.get("toStation").asText());
            
            // 验证列车类型是否在允许的列表中
            String trainType = trainNode.get("type").asText();
            List<String> allowedTypes = Arrays.asList("复兴号", "和谐号", "动车组", "普通列车");
            if (!allowedTypes.contains(trainType)) {
                throw new ServiceException(Constants.CODE_500, "无效的列车类型: " + trainType + "，只允许：" + String.join("、", allowedTypes));
            }
            train.setType(trainType);
            
            train.setDistance(trainNode.get("distance").asInt());
            train.setDuration(trainNode.get("duration").asInt());
            train.setMaxSpeed(trainNode.get("maxSpeed").asInt());
            train.setColor(trainNode.get("color").asText());
            
            // 5. 创建站点列表
            List<Station> stations = new ArrayList<>();
            JsonNode stationsNode = rootNode.get("stations");
            for (int i = 0; i < stationsNode.size(); i++) {
                JsonNode stationNode = stationsNode.get(i);
                
                Station station = new Station();
                station.setTrainId(train.getId());
                station.setName(stationNode.get("name").asText());
                station.setArriveTime(stationNode.get("arriveTime").asText());
                station.setDepartTime(stationNode.get("departTime").asText());
                station.setStop(stationNode.get("stop").asInt());
                station.setDistance(stationNode.get("distance").asInt());
                station.setLongitude(stationNode.get("longitude").asDouble());
                station.setLatitude(stationNode.get("latitude").asDouble());
                station.setSequence(i);
                
                stations.add(station);
            }
            
            // 验证列车持续时间与站点时间是否匹配
            if (!validateDuration(train, stations)) {
                System.out.println("警告：列车持续时间与站点时间不匹配，正在调整...");
                // 根据站点时间重新计算持续时间
                String firstDepartTime = stations.get(0).getDepartTime();
                String lastArriveTime = stations.get(stations.size() - 1).getArriveTime();
                
                int startSeconds = timeToSeconds(firstDepartTime);
                int endSeconds = timeToSeconds(lastArriveTime);
                
                if (endSeconds < startSeconds) {
                    endSeconds += 24 * 60 * 60;
                }
                
                train.setDuration(endSeconds - startSeconds);
            }
            
            // 保存列车信息
            boolean saved = save(train);
            if (!saved) {
                throw new ServiceException(Constants.CODE_500, "保存列车信息失败");
            }
            
            // 更新站点的trainId
            for (Station station : stations) {
                station.setTrainId(train.getId());
            }
            
            // 保存站点信息
            stationService.saveBatch(stations);
            
            return Result.success();
        } catch (Exception e) {
            System.err.println("处理AI数据时出错: " + e.getMessage());
            e.printStackTrace();
            throw new ServiceException(Constants.CODE_500, "处理AI数据时出错: " + e.getMessage());
        }
    }
    
    /**
     * 清理AI返回的JSON响应，移除可能的markdown代码块标记等
     */
    private String cleanJsonResponse(String response) {
        // 移除可能的markdown代码块
        response = response.replaceAll("```json", "").replaceAll("```", "");
        
        // 尝试查找第一个 { 和最后一个 } 来提取JSON
        int firstBrace = response.indexOf("{");
        int lastBrace = response.lastIndexOf("}");
        
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return response.substring(firstBrace, lastBrace + 1);
        }
        
        return response;
    }

    /**
     * 验证列车持续时间是否与站点到达和发车时间匹配
     * @param train 列车对象
     * @param stations 站点列表
     * @return 验证通过返回true，否则返回false
     */
    private boolean validateDuration(Train train, List<Station> stations) {
        if (stations.size() < 2) {
            return false;
        }
        
        try {
            // 获取第一个站点的发车时间
            String firstDepartTime = stations.get(0).getDepartTime();
            // 获取最后一个站点的到达时间
            String lastArriveTime = stations.get(stations.size() - 1).getArriveTime();
            
            // 转换为秒数
            int startSeconds = timeToSeconds(firstDepartTime);
            int endSeconds = timeToSeconds(lastArriveTime);
            
            // 如果终点时间小于起点时间，说明跨天了，加上24小时
            if (endSeconds < startSeconds) {
                endSeconds += 24 * 60 * 60;
            }
            
            // 计算实际持续时间
            int actualDuration = endSeconds - startSeconds;
            
            // 与列车duration比较，允许5分钟误差
            return Math.abs(actualDuration - train.getDuration()) <= 300;
        } catch (Exception e) {
            System.err.println("验证列车时间时出错: " + e.getMessage());
            return false;
        }
    }

    /**
     * 将HH:MM格式的时间转换为秒数
     */
    private int timeToSeconds(String time) {
        if (time == null || time.trim().isEmpty()) {
            return 0;
        }
        
        String[] parts = time.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("时间格式错误: " + time);
        }
        
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        
        return hours * 3600 + minutes * 60;
    }
} 