CREATE DATABASE IF NOT EXISTS `elastic-job-demo` default character set = 'utf8';

use elastic-job-demo;

CREATE TABLE IF NOT EXISTS `t_file_custom` (
  `id` INT(4) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) DEFAULT NULL,
  `content` VARCHAR(255) DEFAULT NULL,
  `type` VARCHAR(255) NOT NULL,
  `backedUp` INT(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件1', '内容1', 'text', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件2', '内容2', 'text', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件3', '内容4', 'text', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件4', '内容4', 'text', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件5', '内容5', 'image', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件6', '内容6', 'image', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件7', '内容7', 'image', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件8', '内容8', 'text', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件9', '内容9', 'radio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件10', '内容10', 'text', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件11', '内容11', 'radio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件12', '内容12', 'radio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件13', '内容13', 'radio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件14', '内容14', 'radio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件15', '内容15', 'vedio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件16', '内容16', 'vedio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件17', '内容17', 'vedio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件18', '内容18', 'image', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件19', '内容19', 'radio', 0);
into into t_file_custom(`name`, `content`, `type`, `backedUp`) values ('文件20', '内容20', 'vedio', 0);
