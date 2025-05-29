/*
 Navicat Premium Data Transfer

 Source Server         : l2
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : train

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 29/05/2025 02:05:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件名称',
  `url` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '下载链接',
  `type` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件类型',
  `md5` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件md5',
  `size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `enable` tinyint NULL DEFAULT 1 COMMENT '是否禁用(1-启用, 1-禁用)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 183 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '文件上传的列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '名称',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '描述',
  `flag` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '唯一标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', '管理员', 'admin');
INSERT INTO `sys_role` VALUES (2, '用户', '用户', 'user');

-- ----------------------------
-- Table structure for sys_station
-- ----------------------------
DROP TABLE IF EXISTS `sys_station`;
CREATE TABLE `sys_station`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `train_id` bigint NOT NULL COMMENT '关联的列车ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '站点名称',
  `arrive_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '到达时间',
  `depart_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发车时间',
  `stop` int NULL DEFAULT 0 COMMENT '停留时间(分钟)',
  `distance` int NULL DEFAULT 0 COMMENT '距离起点站的距离(km)',
  `longitude` double NULL DEFAULT NULL COMMENT '站点经度',
  `latitude` double NULL DEFAULT NULL COMMENT '站点纬度',
  `sequence` int NULL DEFAULT 0 COMMENT '站点序号(用于排序)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_train_id`(`train_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站点信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_station
-- ----------------------------
INSERT INTO `sys_station` VALUES (1, 1, '上海虹桥', NULL, '08:00', 0, 0, 121.32114, 31.195343, 0);
INSERT INTO `sys_station` VALUES (2, 1, '苏州北', '08:24', '08:27', 3, 84, 120.633574, 31.410186, 1);
INSERT INTO `sys_station` VALUES (3, 1, '南京南', '09:01', '09:05', 4, 295, 118.798767, 31.968577, 2);
INSERT INTO `sys_station` VALUES (4, 1, '徐州东', '10:12', '10:16', 4, 578, 117.766531, 34.263114, 3);
INSERT INTO `sys_station` VALUES (5, 1, '济南西', '11:10', '11:14', 4, 830, 116.851673, 36.651858, 4);
INSERT INTO `sys_station` VALUES (6, 1, '天津南', '12:00', '12:04', 4, 1079, 117.049239, 39.067506, 5);
INSERT INTO `sys_station` VALUES (7, 1, '北京南', '12:30', NULL, 0, 1318, 116.385896, 39.865196, 6);
INSERT INTO `sys_station` VALUES (8, 2, '北京南', NULL, '08:02', 0, 0, 116.385896, 39.865196, 0);
INSERT INTO `sys_station` VALUES (9, 2, '天津南', '08:30', '08:34', 4, 239, 117.049239, 39.067506, 1);
INSERT INTO `sys_station` VALUES (10, 2, '济南西', '09:20', '09:24', 4, 488, 116.912649, 36.744652, 2);
INSERT INTO `sys_station` VALUES (11, 2, '徐州东', '10:23', '10:27', 4, 740, 117.766531, 34.263114, 3);
INSERT INTO `sys_station` VALUES (12, 2, '南京南', '11:32', '11:36', 4, 1023, 118.798767, 31.968577, 4);
INSERT INTO `sys_station` VALUES (13, 2, '苏州北', '12:10', '12:14', 4, 1234, 120.633574, 31.410186, 5);
INSERT INTO `sys_station` VALUES (14, 2, '上海虹桥', '12:30', NULL, 0, 1318, 121.32114, 31.195343, 6);
INSERT INTO `sys_station` VALUES (15, 3, '北京南', '', '08:00', 0, 0, 116.397229, 39.8647, 0);
INSERT INTO `sys_station` VALUES (16, 3, '天津西', '08:38', '08:40', 2, 137, 117.2007, 39.1475, 1);
INSERT INTO `sys_station` VALUES (17, 3, '济南西', '10:05', '10:07', 2, 406, 116.9833, 36.6167, 2);
INSERT INTO `sys_station` VALUES (18, 3, '南京南', '12:15', '12:17', 2, 986, 118.7881, 32.0584, 3);
INSERT INTO `sys_station` VALUES (19, 3, '上海虹桥', '13:20', '', 0, 1225, 121.27, 31.1942, 4);
INSERT INTO `sys_station` VALUES (20, 4, '北京南', '', '08:00', 0, 0, 116.397229, 39.894889, 0);
INSERT INTO `sys_station` VALUES (21, 4, '天津西', '08:30', '08:32', 2, 137, 117.2075, 39.1497, 1);
INSERT INTO `sys_station` VALUES (22, 4, '济南西', '10:00', '10:02', 2, 406, 116.9934, 36.6212, 2);
INSERT INTO `sys_station` VALUES (23, 4, '南京南', '12:00', '12:02', 2, 935, 118.7948, 31.9587, 3);
INSERT INTO `sys_station` VALUES (24, 4, '上海虹桥', '13:00', '', 0, 1225, 121.2976, 31.1975, 4);
INSERT INTO `sys_station` VALUES (25, 5, '成都', '', '07:00', 0, 0, 104.0657, 30.6705, 0);
INSERT INTO `sys_station` VALUES (26, 5, '资阳', '07:35', '07:37', 2, 88, 105.2184, 30.1185, 1);
INSERT INTO `sys_station` VALUES (27, 5, '内江', '07:55', '07:57', 2, 145, 105.0668, 29.5852, 2);
INSERT INTO `sys_station` VALUES (28, 5, '永川', '08:15', '08:17', 2, 210, 105.9246, 29.3242, 3);
INSERT INTO `sys_station` VALUES (29, 5, '重庆', '08:40', '', 0, 313, 106.5515, 29.5619, 4);
INSERT INTO `sys_station` VALUES (30, 6, '南宁东', '', '08:00', 0, 0, 108.3144, 22.8089, 0);
INSERT INTO `sys_station` VALUES (31, 6, '贵港', '08:35', '08:37', 2, 152, 109.5912, 23.1085, 1);
INSERT INTO `sys_station` VALUES (32, 6, '玉林', '09:05', '09:07', 2, 258, 110.1672, 22.6437, 2);
INSERT INTO `sys_station` VALUES (33, 6, '茂名', '09:40', '09:42', 2, 389, 110.9337, 21.6627, 3);
INSERT INTO `sys_station` VALUES (34, 6, '阳江', '09:55', '09:57', 2, 445, 111.9775, 21.8622, 4);
INSERT INTO `sys_station` VALUES (35, 6, '广州南', '10:30', '', 0, 570, 113.3393, 23.0022, 5);
INSERT INTO `sys_station` VALUES (36, 7, '南宁东', '', '08:00', 0, 0, 108.3017, 22.8089, 0);
INSERT INTO `sys_station` VALUES (37, 7, '柳州', '08:45', '08:47', 2, 222, 109.4169, 26.0747, 1);
INSERT INTO `sys_station` VALUES (38, 7, '桂林北', '09:25', '09:27', 2, 376, 110.2856, 25.8875, 2);
INSERT INTO `sys_station` VALUES (39, 7, '贺州', '10:10', '10:12', 2, 498, 111.5578, 24.4192, 3);
INSERT INTO `sys_station` VALUES (40, 7, '广州南', '11:00', '', 0, 574, 113.3382, 23.0435, 4);

-- ----------------------------
-- Table structure for sys_train
-- ----------------------------
DROP TABLE IF EXISTS `sys_train`;
CREATE TABLE `sys_train`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次编号',
  `from_station` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '始发站',
  `to_station` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '终点站',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列车类型',
  `distance` int NULL DEFAULT NULL COMMENT '总里程(km)',
  `duration` int NULL DEFAULT NULL COMMENT '运行时间(秒)',
  `max_speed` int NULL DEFAULT NULL COMMENT '最高速度(km/h)',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列车颜色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '列车信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_train
-- ----------------------------
INSERT INTO `sys_train` VALUES (1, 'G1次', '上海', '北京', '复兴号', 1318, 16200, 350, '#1890ff');
INSERT INTO `sys_train` VALUES (2, 'G2次', '北京', '上海', '复兴号', 1318, 16200, 350, '#ff0000');
INSERT INTO `sys_train` VALUES (4, 'G5566', '北京南', '上海虹桥', '复兴号', 1225, 18000, 350, '#00A0E2');
INSERT INTO `sys_train` VALUES (5, 'D6578', '成都', '重庆', '动车组', 313, 15600, 250, '#00A859');
INSERT INTO `sys_train` VALUES (7, 'D6890', '南宁东', '广州南', '动车组', 574, 10800, 200, '#00A859');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '密码',
  `nickname` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar_url` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '头像',
  `email` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role_id` int NULL DEFAULT NULL COMMENT '角色  0超级管理员  1管理员 2普通账号',
  `status` tinyint NULL DEFAULT 1 COMMENT '是否有效 1有效 0无效',
  `role` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'admin', '管理员', NULL, '', 1, 1, 'admin');
INSERT INTO `sys_user` VALUES (2, 'user', 'user', '用户', NULL, '', 2, 1, 'user');

SET FOREIGN_KEY_CHECKS = 1;
