package com.kolade.offers.config

import com.typesafe.config.{Config, ConfigFactory}

trait AppConfig {
  val config: Config = ConfigFactory.load

  val ApiPort: Int = config.getInt("port")
  val ApiHost: String = config.getString("host")

  private val ApiDomain: String = config.getString("domain")
  val OfferLinkPrefix: String = s"http://$ApiDomain/offers"

  val OfferCacheCapacity: Long = config.getLong("cache.capacity")

  private val validationConfig = config.getConfig("offer.validation")

  private val TwoDecimalDigits = 2
  private val minAllowedAmountAsDouble: Double = validationConfig.getDouble("minAllowedAmount")
  val MinAllowedAmount: BigDecimal = BigDecimal(minAllowedAmountAsDouble).setScale(TwoDecimalDigits, BigDecimal.RoundingMode.HALF_UP)

  val MinAllowedDescriptionLength: Int = validationConfig.getInt("minAllowedDescriptionLength")
  val MinAllowedOfferIdLength: Int = validationConfig.getInt("minAllowedOfferIdLength")
  val MaxDaysAgoAllowedForOfferStartDate: Int = validationConfig.getInt("maxDaysAgoAllowedForOfferStartDate")
}
