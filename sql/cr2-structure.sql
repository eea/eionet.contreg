-- MySQL dump 10.9
--
-- Host: localhost    Database: cr2
-- ------------------------------------------------------
-- Server version	4.1.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `harvest`
--

DROP TABLE IF EXISTS `HARVEST`;
CREATE TABLE `HARVEST` (
  `HARVEST_ID` int(10) unsigned NOT NULL auto_increment,
  `HARVEST_SOURCE_ID` int(10) unsigned NOT NULL default '0',
  `TYPE` varchar(20) NOT NULL default '',
  `USER` varchar(45) default NULL,
  `STATUS` varchar(10) NOT NULL default '',
  `STARTED` timestamp NULL default NULL,
  `FINISHED` timestamp NULL default NULL,
  `TOT_STATEMENTS` int(10) unsigned default NULL,
  `LIT_STATEMENTS` int(10) unsigned default NULL,
  `RES_STATEMENTS` int(10) unsigned default NULL,
  `ENC_SCHEMES` int(10) unsigned default NULL,
  `TOT_RESOURCES` int(10) unsigned default NULL,
  PRIMARY KEY  (`HARVEST_ID`),
  UNIQUE KEY `SOURCE_ID_STARTED` (`HARVEST_SOURCE_ID`,`STARTED`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `harvest_message`
--

DROP TABLE IF EXISTS `HARVEST_MESSAGE`;
CREATE TABLE `HARVEST_MESSAGE` (
  `HARVEST_ID` int(10) unsigned NOT NULL default '0',
  `TYPE` varchar(3) NOT NULL default '',
  `MESSAGE` varchar(255) NOT NULL default '',
  `STACK_TRACE` text,
  `HARVEST_MESSAGE_ID` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`HARVEST_MESSAGE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `harvest_queue`
--

DROP TABLE IF EXISTS `HARVEST_QUEUE`;
CREATE TABLE `HARVEST_QUEUE` (
  `URL` varchar(255) NOT NULL default '',
  `PRIORITY` enum('normal','urgent') NOT NULL default 'normal',
  `TIMESTAMP` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `PUSHED_CONTENT` text
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `harvest_source`
--

DROP TABLE IF EXISTS `HARVEST_SOURCE`;
CREATE TABLE `HARVEST_SOURCE` (
  `HARVEST_SOURCE_ID` int(10) unsigned NOT NULL auto_increment,
  `NAME` varchar(255) default NULL,
  `URL` varchar(255) default NULL,
  `TYPE` varchar(30) NOT NULL default '',
  `EMAILS` varchar(255) default NULL,
  `DATE_CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATOR` varchar(45) NOT NULL default '',
  `STATEMENTS` int(10) unsigned default NULL,
  `RESOURCES` int(10) unsigned default NULL,
  `COUNT_UNAVAIL` int(10) unsigned NOT NULL default '0',
  `SCHEDULE_CRON` varchar(255) default NULL,
  PRIMARY KEY  (`HARVEST_SOURCE_ID`),
  UNIQUE KEY `URL` USING BTREE (`URL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

