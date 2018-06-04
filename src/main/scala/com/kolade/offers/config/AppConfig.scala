package com.kolade.offers.config

import com.typesafe.config.{Config, ConfigFactory}

trait AppConfig {
  val config: Config = ConfigFactory.load

  val ApiPort: Int = config.getInt("port")
  val ApiHost: String = config.getString("host")

  val OfferCacheCapacity: Long = config.getLong("cache.capacity")

  private val validationConfig = config.getConfig("offer.validation")
  val MinAllowedCost: Int = validationConfig.getInt("minAllowedCost")
  val MinAllowedDescriptionLength: Int = validationConfig.getInt("minAllowedDescriptionLength")
  val MinAllowedOfferIdLength: Int = validationConfig.getInt("minAllowedOfferIdLength")
  val MaxDaysAgoAllowedForOfferStartDate: Int = validationConfig.getInt("maxDaysAgoAllowedForOfferStartDate")
}
