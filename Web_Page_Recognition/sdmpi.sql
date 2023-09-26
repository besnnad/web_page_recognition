/*
 Navicat Premium Data Transfer

 Source Server         : mysql80
 Source Server Type    : MySQL
 Source Server Version : 80020
 Source Host           : localhost:3306
 Source Schema         : wpi

 Target Server Type    : MySQL
 Target Server Version : 80020
 File Encoding         : 65001

 Date: 02/06/2021 12:23:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `css_file`;
CREATE TABLE `css_file`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `size` int NULL DEFAULT NULL,
  `data` mediumblob NULL,
  `hash` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `hash`(`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `fingerprint`;
CREATE TABLE `fingerprint`  (
  `page_id` int UNSIGNED NOT NULL,
  `fpdata` blob NOT NULL,
  `last_update` datetime(0) NOT NULL,
  PRIMARY KEY (`page_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for inverted_index
-- ----------------------------
DROP TABLE IF EXISTS `inverted_index`;
CREATE TABLE `inverted_index`  (
  `word` bigint NOT NULL,
  `page_id` int NOT NULL,
  `word_index` int NULL DEFAULT NULL,
  `frequency` int NULL DEFAULT NULL,
  INDEX `word`(`word`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ip_host
-- ----------------------------
DROP TABLE IF EXISTS `ip_host`;
CREATE TABLE `ip_host`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1012693 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for js_file
-- ----------------------------
DROP TABLE IF EXISTS `js_file`;
CREATE TABLE `js_file`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `size` int NOT NULL,
  `data` mediumblob NOT NULL,
  `hash` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `hash`(`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for match_record
-- ----------------------------
DROP TABLE IF EXISTS `match_record`;
CREATE TABLE `match_record`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `client_ip` int NOT NULL,
  `client_port` int NOT NULL,
  `server_ip` int NOT NULL,
  `server_port` int NOT NULL,
  `create_time` datetime(0) NOT NULL,
  `page_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;



-- ----------------------------
-- Table structure for page_url
-- ----------------------------
DROP TABLE IF EXISTS `page_url`;
CREATE TABLE `page_url`  (
  `page_id` int NOT NULL,
  `url` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`page_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for static_file
-- ----------------------------
DROP TABLE IF EXISTS `static_file`;
CREATE TABLE `static_file`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `size` int NULL DEFAULT NULL,
  `data` mediumblob NULL,
  `hash` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `hash`(`hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
