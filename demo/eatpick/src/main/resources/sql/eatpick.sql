/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : 127.0.0.1:3306
 Source Schema         : eatpick

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 18/12/2024 15:08:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `banner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '记录上次修改时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '记录删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜谱分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, 'https://i2.chuimg.com/39fd20394a9d4ae781533b18d6575f6d_3024w_2419h.jpg', '下饭菜', '连米饭也一起吃光', '2024-12-18 12:00:04', '2024-12-18 12:00:04', NULL);

-- ----------------------------
-- Table structure for ingredient
-- ----------------------------
DROP TABLE IF EXISTS `ingredient`;
CREATE TABLE `ingredient`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '记录上次修改时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '记录删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '食材' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ingredient
-- ----------------------------
INSERT INTO `ingredient` VALUES (1, 'https://bkimg.cdn.bcebos.com/pic/09fa513d269759ee3d6dcb6dcea354166d224f4a7146', '笋', '笋一年四季皆有，但惟有春笋、冬笋味道最佳。烹调时无论是凉拌、煎炒还是熬汤，均鲜嫩清香，是人们喜欢的佳肴之一。', '2024-12-18 12:02:01', '2024-12-18 12:02:01', NULL);
INSERT INTO `ingredient` VALUES (2, 'https://pic.baike.soso.com/ugc/baikepic2/0/20220331113538-216509543_png_613_439_796943.jpg', '雪里红', '雪里红富含维生素 C、维生素 B1、维生素 B2 和磷、铁等微量元素及抗坏血酸。其性温，味甘辛，具有解毒消肿、开胃消食、温中利气、明目利膈等功效，对疮痈肿痛、咳嗽痰多、牙龈肿烂、便秘等有一定的食疗作用。小儿消化功能不全者不宜多食，且不宜与醋同食，会破坏胡萝卜素。', '2024-12-18 13:36:22', '2024-12-18 13:36:22', NULL);
INSERT INTO `ingredient` VALUES (3, 'https://bkimg.cdn.bcebos.com/pic/d50735fae6cd7b899e5168766b7355a7d933c995a0b5', '食用盐', '氯化钠，是人体不能缺乏的重要元素，有调节人体活动的作用。', '2024-12-18 13:39:37', '2024-12-18 13:39:37', NULL);
INSERT INTO `ingredient` VALUES (4, 'https://bkimg.cdn.bcebos.com/pic/bd315c6034a85edf8db15869b80c1e23dd54564edf7a', '白砂糖', '白砂糖是食糖的一种。其颗粒为结晶状，均匀，颜色洁白，甜味纯正，甜度稍低于红糖。烹调中常用。适当食用白糖有补中益气、和胃润肺、养阴止汗的功效。', '2024-12-18 13:40:55', '2024-12-18 13:40:55', NULL);
INSERT INTO `ingredient` VALUES (5, 'https://bkimg.cdn.bcebos.com/pic/574e9258d109b3deeabcf61ec5bf6c81800a4c40', '干辣椒', '干辣椒是红辣椒经过自然晾晒、人工脱水等过程而形成的辣椒产品，又称作辣椒干、干制辣椒，制干辣椒、加工辣椒、加工型辣椒等。它的特点是含水量低、适合长期保藏，但未密封包装或含水量高的干辣椒容易霉变。干辣椒的吃法主要是作为调味料食用。阴虚火旺及患咳嗽、目疾者忌服干辣椒。', '2024-12-18 13:42:30', '2024-12-18 13:42:30', NULL);
INSERT INTO `ingredient` VALUES (6, 'https://bkimg.cdn.bcebos.com/pic/eaf81a4c510fd9f9d72ad049a075c32a2834349b0ec0', '生抽', '生抽是以大豆或脱脂大豆或黑豆、小麦或面粉为主要原料，人工接入种曲，经天然露晒发酵而成的，颜色呈红褐色。生抽是用来做一般的烹调用的，酱香味浓。生抽主要是用来调味，颜色淡，故做一般的炒菜或者凉菜的时候用得比较多。', '2024-12-18 13:43:48', '2024-12-18 13:43:48', NULL);
INSERT INTO `ingredient` VALUES (7, 'https://bkimg.cdn.bcebos.com/pic/7c1ed21b0ef41bd5ad6ee182d18d96cb39dbb6fd9734', '蚝油', '蚝油是用蚝（牡蛎）熬制而成的调味料。蚝油是广东常用的传统的鲜味调料，也是调味汁类最正宗产品之一，它以素有“海底牛奶”之称的蚝（牡蛎）为原料，经煮熟取汁浓缩，加辅料精制而成。蚝油味道鲜美、蚝香浓郁，黏稠适度，营养价值高，亦是配制蚝油鲜菇牛肉、蚝油青菜、蚝油粉面等传统粤菜的主要配料。', '2024-12-18 13:44:55', '2024-12-18 13:44:55', NULL);
INSERT INTO `ingredient` VALUES (8, 'https://pic.baike.soso.com/ugc/baikepic2/0/ori-20230328110858-1075403596_jpeg_1848_1848_390468.jpg', '毛豆', '毛豆和黄豆是同一植物在不同时期的产物，毛豆长大后就变成了黄豆。', '2024-12-18 13:52:48', '2024-12-18 13:52:48', NULL);

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` char(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键，按时间生成',
  `customer_id` int UNSIGNED NOT NULL COMMENT '顾客 ID',
  `notes` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `status` tinyint UNSIGNED NOT NULL COMMENT '订单状态：0 取消；1 已下单；2 制作中；3 已完成；4 已评价',
  `review` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评论',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '记录上次修改时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '记录删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES ('1734502398682', 1, '多做点', 1, NULL, '2024-12-18 14:13:18', '2024-12-18 14:13:18', NULL);

-- ----------------------------
-- Table structure for order_recipe_bind
-- ----------------------------
DROP TABLE IF EXISTS `order_recipe_bind`;
CREATE TABLE `order_recipe_bind`  (
  `order_id` char(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单 ID',
  `recipe_id` int UNSIGNED NOT NULL COMMENT '菜谱 ID',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单菜谱关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_recipe_bind
-- ----------------------------
INSERT INTO `order_recipe_bind` VALUES ('1734502398682', 3);

-- ----------------------------
-- Table structure for recipe
-- ----------------------------
DROP TABLE IF EXISTS `recipe`;
CREATE TABLE `recipe`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  `taste` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '口味',
  `pungency_degree` tinyint UNSIGNED NOT NULL COMMENT '辣度',
  `cooking_time` tinyint UNSIGNED NOT NULL COMMENT '烹饪需要时间，单位：min',
  `difficulty` tinyint UNSIGNED NOT NULL COMMENT '难度',
  `display` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '记录上次修改时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '记录删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜谱' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe
-- ----------------------------
INSERT INTO `recipe` VALUES (3, 'https://i2.chuimg.com/39fd20394a9d4ae781533b18d6575f6d_3024w_2419h.jpg', '雪菜笋丝炒毛豆', '佐餐拌面下饭', '咸香', 0, 30, 2, 1, '2024-12-18 14:09:26', '2024-12-18 14:09:26', NULL);

-- ----------------------------
-- Table structure for recipe_category_bind
-- ----------------------------
DROP TABLE IF EXISTS `recipe_category_bind`;
CREATE TABLE `recipe_category_bind`  (
  `recipe_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '菜谱 ID',
  `category_id` int UNSIGNED NOT NULL COMMENT '分类 ID',
  PRIMARY KEY (`recipe_id`, `category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜谱分类关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe_category_bind
-- ----------------------------

-- ----------------------------
-- Table structure for recipe_ingredient_bind
-- ----------------------------
DROP TABLE IF EXISTS `recipe_ingredient_bind`;
CREATE TABLE `recipe_ingredient_bind`  (
  `recipe_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '菜谱 ID',
  `ingredient_id` int UNSIGNED NOT NULL COMMENT '食材 ID',
  `quantity` double(10, 2) UNSIGNED NOT NULL COMMENT '数量',
  `unit` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位，如克、毫升、个等',
  PRIMARY KEY (`recipe_id`, `ingredient_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜谱食材关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe_ingredient_bind
-- ----------------------------
INSERT INTO `recipe_ingredient_bind` VALUES (3, 1, 100.00, '克');
INSERT INTO `recipe_ingredient_bind` VALUES (3, 2, 300.00, '克');
INSERT INTO `recipe_ingredient_bind` VALUES (3, 3, 350.00, '克');
INSERT INTO `recipe_ingredient_bind` VALUES (3, 4, 20.00, '克');
INSERT INTO `recipe_ingredient_bind` VALUES (3, 5, 2.00, '个');

-- ----------------------------
-- Table structure for recipe_step
-- ----------------------------
DROP TABLE IF EXISTS `recipe_step`;
CREATE TABLE `recipe_step`  (
  `recipe_id` int UNSIGNED NOT NULL COMMENT '菜谱 ID',
  `order` tinyint UNSIGNED NOT NULL COMMENT '步骤序号',
  `desc` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  PRIMARY KEY (`recipe_id`, `order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜谱步骤' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe_step
-- ----------------------------
INSERT INTO `recipe_step` VALUES (3, 1, '准备食材；笋：去皮后切丝或粒，毛豆：剥开清洗干净，雪菜：清晰干净沥干。');
INSERT INTO `recipe_step` VALUES (3, 2, '爆香辣椒和姜片。');
INSERT INTO `recipe_step` VALUES (3, 3, '倒入毛豆翻炒，大火炒 30 秒。');
INSERT INTO `recipe_step` VALUES (3, 4, '放入高汤（鸡汤/排骨汤/热水），小火煮 5 分钟。');
INSERT INTO `recipe_step` VALUES (3, 5, '盖上锅盖焖煮五分钟直到毛豆酥烂。');
INSERT INTO `recipe_step` VALUES (3, 6, '毛豆有点脱壳后倒入雪菜和笋丝翻炒。');
INSERT INTO `recipe_step` VALUES (3, 7, '放少许生抽和糖，出锅。');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '头像',
  `role` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色：customer 顾客；chef 厨师；admin 管理员',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '记录创建时间',
  `update_time` datetime NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '记录上次修改时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '记录删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$hjvMz9SeWuXGcauqPm50L.fnBQWCH2sxMu8iRj/BcRApnSZji2R9m', '管理员', 'https://api.multiavatar.com/admin.png', 'admin', '2024-12-18 11:18:33', '2024-12-18 11:37:52', NULL);

SET FOREIGN_KEY_CHECKS = 1;
