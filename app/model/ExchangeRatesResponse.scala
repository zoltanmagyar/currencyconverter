package model

import Formats._
import org.joda.money.CurrencyUnit
import play.api.libs.json.{Format, Json}

/**
  * Represents the response payload from the Exchange Rates API
  */
case class ExchangeRatesResponse(base: CurrencyUnit, rates: Map[CurrencyUnit, Double])

object ExchangeRatesResponse {
  implicit val exchangeRatesResponseFormat: Format[ExchangeRatesResponse] = Json.format[ExchangeRatesResponse]
}
