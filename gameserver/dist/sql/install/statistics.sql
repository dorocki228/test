CREATE TABLE IF NOT EXISTS `statistics` (
  `dateTime` datetime NOT NULL,
  `source` VARCHAR(255) NOT NULL,

  `accountsTotalCount` BIGINT,
  `accountsLoggedInAtLeastOneTimeCount` BIGINT,
  `accountsLoggedInAtLeastThreeTimesCount` BIGINT,
  `donatorMailCount` BIGINT,

  `donateAmount` DECIMAL(15,2),

  `registeredToLoggedInRatio` DECIMAL(15,2),
  `activeToRegisteredRatio` DECIMAL(15,2),
  `activeToLoggedInRatio` DECIMAL(15,2),

  `donateOnRegisteredAmount` DECIMAL(15,2),
  `donateOnActiveAmount` DECIMAL(15,2),

  `donatorToActivePercentage` DECIMAL(15,2),
  `averageDonate` DECIMAL(15,2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
