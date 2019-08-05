/*
Navicat MySQL Data Transfer

Source Server         : mysql
Source Server Version : 50018
Source Host           : localhost:3306
Source Database       : db_my_blog

Target Server Type    : MYSQL
Target Server Version : 50018
File Encoding         : 65001

Date: 2018-08-06 19:47:52
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_attach
-- ----------------------------
DROP TABLE IF EXISTS `t_attach`;
CREATE TABLE `t_attach` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `fname` varchar(100) NOT NULL default '',
  `ftype` varchar(50) default '',
  `fkey` varchar(100) NOT NULL default '',
  `author_id` int(10) default NULL,
  `created` int(10) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_attach
-- ----------------------------

-- ----------------------------
-- Table structure for t_comments
-- ----------------------------
DROP TABLE IF EXISTS `t_comments`;
CREATE TABLE `t_comments` (
  `coid` int(10) unsigned NOT NULL auto_increment,
  `cid` int(10) unsigned default '0',
  `created` int(10) unsigned default '0',
  `author` varchar(200) default NULL,
  `author_id` int(10) unsigned default '0',
  `owner_id` int(10) unsigned default '0',
  `mail` varchar(200) default NULL,
  `url` varchar(200) default NULL,
  `ip` varchar(64) default NULL,
  `agent` varchar(200) default NULL,
  `content` text,
  `type` varchar(16) default 'comment',
  `status` varchar(16) default 'approved',
  `parent` int(10) unsigned default '0',
  PRIMARY KEY  (`coid`),
  KEY `cid` (`cid`),
  KEY `created` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_comments
-- ----------------------------

-- ----------------------------
-- Table structure for t_contents
-- ----------------------------
DROP TABLE IF EXISTS `t_contents`;
CREATE TABLE `t_contents` (
  `cid` int(10) unsigned NOT NULL auto_increment,
  `title` varchar(200) default NULL,
  `slug` varchar(200) default NULL,
  `created` int(10) unsigned default '0',
  `modified` int(10) unsigned default '0',
  `content` text COMMENT '内容文字',
  `author_id` int(10) unsigned default '0',
  `type` varchar(16) default 'post',
  `status` varchar(16) default 'publish',
  `tags` varchar(200) default NULL,
  `categories` varchar(200) default NULL,
  `hits` int(10) unsigned default '0',
  `comments_num` int(10) unsigned default '0',
  `allow_comment` tinyint(1) default '1',
  `allow_ping` tinyint(1) default '1',
  `allow_feed` tinyint(1) default '1',
  PRIMARY KEY  (`cid`),
  UNIQUE KEY `slug` (`slug`),
  KEY `created` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_contents
-- ----------------------------
INSERT INTO `t_contents` VALUES ('12', 'my blog', null, '1533481081', '1533481081', '    **Wellcome my blog**\r\n    这个是从[github][1]上的[My-Blog][2]项目下载下来，原本是springboot项目被我改成了ssm项目。功能没有完善，代码阅读了大部分，个人感觉是一个不错的项目，可以学习一下。\r\n    同时，欢迎进入我的[github][3]查看源码\r\n\r\n\r\n  [1]: https://github.com/ZHENFENG13\r\n  [2]: https://github.com/ZHENFENG13/My-Blog\r\n  [3]: https://github.com/LowellZhao', '1', 'post', 'publish', 'default', '默认', '0', '0', '1', '1', '1');
INSERT INTO `t_contents` VALUES ('13', '页面', 'mypage', '1533482257', '1533482257', '啧啧啧啧啧啧', '1', 'page', 'publish', null, null, '0', '0', '1', '1', '1');

-- ----------------------------
-- Table structure for t_logs
-- ----------------------------
DROP TABLE IF EXISTS `t_logs`;
CREATE TABLE `t_logs` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `action` varchar(100) default NULL,
  `data` varchar(2000) default NULL,
  `author_id` int(10) default NULL,
  `ip` varchar(20) default NULL,
  `created` int(10) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_logs
-- ----------------------------
INSERT INTO `t_logs` VALUES ('67', '登录后台', null, '1', '127.0.0.1', '1533480569');
INSERT INTO `t_logs` VALUES ('68', '保存系统设置', '{\"site_record\":\"\",\"site_description\":\"L Blog\",\"site_title\":\"L Blog\",\"site_theme\":\"default\",\"allow_install\":\"\"}', '1', '127.0.0.1', '1533481412');
INSERT INTO `t_logs` VALUES ('69', '登录后台', null, '1', '127.0.0.1', '1533482215');
INSERT INTO `t_logs` VALUES ('70', '登录后台', null, '1', '127.0.0.1', '1533482530');

-- ----------------------------
-- Table structure for t_metas
-- ----------------------------
DROP TABLE IF EXISTS `t_metas`;
CREATE TABLE `t_metas` (
  `mid` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(200) default NULL,
  `slug` varchar(200) default NULL,
  `type` varchar(32) NOT NULL default '',
  `description` varchar(200) default NULL,
  `sort` int(10) unsigned default '0',
  `parent` int(10) unsigned default '0',
  PRIMARY KEY  (`mid`),
  KEY `slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_metas
-- ----------------------------
INSERT INTO `t_metas` VALUES ('1', '默认', null, 'category', null, '0', '0');
INSERT INTO `t_metas` VALUES ('13', 'default', 'default', 'tag', null, '0', '0');
INSERT INTO `t_metas` VALUES ('14', 'lwz的Github', 'https://github.com/LowellZhao', 'link', '', '0', '0');
INSERT INTO `t_metas` VALUES ('15', '不错的github', 'https://github.com/ZHENFENG13', 'link', '', '0', '0');

-- ----------------------------
-- Table structure for t_options
-- ----------------------------
DROP TABLE IF EXISTS `t_options`;
CREATE TABLE `t_options` (
  `name` varchar(32) NOT NULL default '',
  `value` varchar(1000) default '',
  `description` varchar(200) default NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_options
-- ----------------------------
INSERT INTO `t_options` VALUES ('allow_install', '', null);
INSERT INTO `t_options` VALUES ('site_description', 'L Blog', null);
INSERT INTO `t_options` VALUES ('site_keywords', '13 Blog', null);
INSERT INTO `t_options` VALUES ('site_record', '', '备案号');
INSERT INTO `t_options` VALUES ('site_theme', 'default', null);
INSERT INTO `t_options` VALUES ('site_title', 'L Blog', '');
INSERT INTO `t_options` VALUES ('social_github', '', null);
INSERT INTO `t_options` VALUES ('social_twitter', '', null);
INSERT INTO `t_options` VALUES ('social_weibo', '', null);
INSERT INTO `t_options` VALUES ('social_zhihu', '', null);

-- ----------------------------
-- Table structure for t_relationships
-- ----------------------------
DROP TABLE IF EXISTS `t_relationships`;
CREATE TABLE `t_relationships` (
  `cid` int(10) unsigned NOT NULL,
  `mid` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`cid`,`mid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_relationships
-- ----------------------------
INSERT INTO `t_relationships` VALUES ('12', '1');
INSERT INTO `t_relationships` VALUES ('12', '13');

-- ----------------------------
-- Table structure for t_users
-- ----------------------------
DROP TABLE IF EXISTS `t_users`;
CREATE TABLE `t_users` (
  `uid` int(10) unsigned NOT NULL auto_increment,
  `username` varchar(32) default NULL,
  `password` varchar(64) default NULL,
  `email` varchar(200) default NULL,
  `home_url` varchar(200) default NULL,
  `screen_name` varchar(32) default NULL,
  `created` int(10) unsigned default '0',
  `activated` int(10) unsigned default '0',
  `logged` int(10) unsigned default '0',
  `group_name` varchar(16) default 'visitor',
  PRIMARY KEY  (`uid`),
  UNIQUE KEY `name` (`username`),
  UNIQUE KEY `mail` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_users
-- ----------------------------
INSERT INTO `t_users` VALUES ('1', 'admin', 'a66abb5684c45962d887564f08346e8d', '1043257024@qq.com', null, '管理员', '1490756162', '0', '0', 'visitor');
