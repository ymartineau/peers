CREATE TABLE `peers` (
  `email` varchar(50) collate latin1_general_ci NOT NULL,
  `ipaddress` varchar(15) collate latin1_general_ci default NULL,
  `port` smallint(5) unsigned default NULL,
  KEY `email` (`email`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

CREATE TABLE `peers_assoc` (
  `id` bigint(20) NOT NULL auto_increment,
  `left` varchar(50) collate latin1_general_ci NOT NULL,
  `right` varchar(50) collate latin1_general_ci NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci AUTO_INCREMENT=12 ;
