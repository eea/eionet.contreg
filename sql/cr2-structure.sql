-- MySQL dump 10.11
--
-- Host: localhost    Database: cr2
-- ------------------------------------------------------
-- Server version	5.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `HARVEST`
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
  UNIQUE KEY `SOURCE_ID_STARTED` (`HARVEST_SOURCE_ID`,`STARTED`),
  KEY `HARVEST_SOURCE_ID` (`HARVEST_SOURCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `HARVEST_MESSAGE`
--

DROP TABLE IF EXISTS `HARVEST_MESSAGE`;
CREATE TABLE `HARVEST_MESSAGE` (
  `HARVEST_ID` int(10) unsigned NOT NULL default '0',
  `TYPE` varchar(3) NOT NULL default '',
  `MESSAGE` varchar(255) NOT NULL default '',
  `STACK_TRACE` text,
  `HARVEST_MESSAGE_ID` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`HARVEST_MESSAGE_ID`),
  KEY `HARVEST_ID` (`HARVEST_ID`),
  KEY `HARVEST_ID_2` (`HARVEST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `HARVEST_SOURCE`
--

DROP TABLE IF EXISTS `HARVEST_SOURCE`;
CREATE TABLE `HARVEST_SOURCE` (
  `HARVEST_SOURCE_ID` int(10) unsigned NOT NULL auto_increment,
  `NAME` varchar(255) default NULL,
  `URL` varchar(255) default NULL,
  `TYPE` varchar(30) NOT NULL default '',
  `EMAILS` varchar(255) default NULL,
  `TIME_CREATED` timestamp NOT NULL default '0000-00-00 00:00:00',
  `CREATOR` varchar(45) NOT NULL default '',
  `STATEMENTS` int(10) unsigned default NULL,
  `RESOURCES` int(10) unsigned default NULL,
  `COUNT_UNAVAIL` int(10) unsigned NOT NULL default '0',
  `LAST_HARVEST` timestamp NULL default NULL,
  `INTERVAL_MINUTES` int(10) unsigned NOT NULL default '0',
  `SOURCE` bigint(20) NOT NULL default '0',
  `GEN_TIME` bigint(20) NOT NULL default '0',
  PRIMARY KEY  (`HARVEST_SOURCE_ID`),
  UNIQUE KEY `URL` USING BTREE (`URL`),
  KEY `TYPE` (`TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `RESOURCE`
--

DROP TABLE IF EXISTS `RESOURCE`;
CREATE TABLE `RESOURCE` (
  `URI` text NOT NULL,
  `URI_HASH` bigint(20) NOT NULL default '0',
  `FIRSTSEEN_SOURCE` bigint(20) NOT NULL default '0',
  `FIRSTSEEN_TIME` bigint(20) NOT NULL default '0',
  PRIMARY KEY  (`URI_HASH`),
  KEY `FIRSTSEEN_SOURCE` (`FIRSTSEEN_SOURCE`),
  KEY `FIRSTSEEN_TIME` (`FIRSTSEEN_TIME`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `RESOURCE_TEMP`
--

DROP TABLE IF EXISTS `RESOURCE_TEMP`;
CREATE TABLE `RESOURCE_TEMP` (
  `URI` text NOT NULL,
  `URI_HASH` bigint(20) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `SPO`
--

DROP TABLE IF EXISTS `SPO`;
CREATE TABLE `SPO` (
  `SUBJECT` bigint(20) NOT NULL default '0',
  `PREDICATE` bigint(20) NOT NULL default '0',
  `OBJECT` text NOT NULL,
  `OBJECT_HASH` bigint(20) NOT NULL default '0',
  `OBJECT_DOUBLE` double default NULL,
  `ANON_SUBJ` enum('Y','N') NOT NULL default 'N',
  `ANON_OBJ` enum('Y','N') NOT NULL default 'N',
  `LIT_OBJ` enum('Y','N') NOT NULL default 'Y',
  `OBJ_LANG` varchar(10) NOT NULL default '',
  `OBJ_DERIV_SOURCE` bigint(20) NOT NULL default '0',
  `OBJ_DERIV_SOURCE_GEN_TIME` bigint(20) NOT NULL default '0',
  `OBJ_SOURCE_OBJECT` bigint(20) NOT NULL default '0',
  `SOURCE` bigint(20) NOT NULL default '0',
  `GEN_TIME` bigint(20) NOT NULL default '0',
  KEY `PREDICATE` (`PREDICATE`),
  KEY `OBJECT_HASH` (`OBJECT_HASH`),
  KEY `SOURCE` (`SOURCE`),
  KEY `GEN_TIME` (`GEN_TIME`),
  KEY `SUBJECT` (`SUBJECT`),
  KEY `OBJECT_DOUBLE` (`OBJECT_DOUBLE`),
  FULLTEXT KEY `OBJECT` (`OBJECT`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `SPO_TEMP`
--

DROP TABLE IF EXISTS `SPO_TEMP`;
CREATE TABLE `SPO_TEMP` (
  `SUBJECT` bigint(20) NOT NULL default '0',
  `PREDICATE` bigint(20) NOT NULL default '0',
  `OBJECT` text NOT NULL,
  `OBJECT_HASH` bigint(20) NOT NULL default '0',
  `OBJECT_DOUBLE` double default NULL,
  `ANON_SUBJ` enum('Y','N') NOT NULL default 'N',
  `ANON_OBJ` enum('Y','N') NOT NULL default 'N',
  `LIT_OBJ` enum('Y','N') NOT NULL default 'Y',
  `OBJ_LANG` varchar(10) NOT NULL default '',
  `OBJ_DERIV_SOURCE` bigint(20) NOT NULL default '0',
  `OBJ_DERIV_SOURCE_GEN_TIME` bigint(20) NOT NULL default '0',
  `OBJ_SOURCE_OBJECT` bigint(20) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `UNFINISHED_HARVEST`
--

DROP TABLE IF EXISTS `UNFINISHED_HARVEST`;
CREATE TABLE `UNFINISHED_HARVEST` (
  `SOURCE` bigint(20) NOT NULL default '0',
  `GEN_TIME` bigint(20) NOT NULL default '0',
  UNIQUE KEY `SOURCE` (`SOURCE`,`GEN_TIME`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `URGENT_HARVEST_QUEUE`
--

DROP TABLE IF EXISTS `URGENT_HARVEST_QUEUE`;
CREATE TABLE `URGENT_HARVEST_QUEUE` (
  `URL` varchar(255) NOT NULL default '',
  `TIMESTAMP` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `PUSHED_CONTENT` text
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-08-13 11:34:25
