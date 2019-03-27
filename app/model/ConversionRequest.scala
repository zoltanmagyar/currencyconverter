package model

import Formats._
import org.joda.money.CurrencyUnit
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
  * Request payload to convert currencies
  */
case class ConversionRequest(fromCurrency: CurrencyUnit, toCurrency: CurrencyUnit, amount: BigDecimal)

object ConversionRequest {
  implicit val conversionRequestReads: Reads[ConversionRequest] = (
    (JsPath \ "fromCurrency").read[CurrencyUnit] and
      (JsPath \ "toCurrency").read[CurrencyUnit] and
      (JsPath \ "amount").read[BigDecimal]
    )(ConversionRequest.apply _)
  implicit val conversionRequestWrites: Writes[ConversionRequest] = Json.writes[ConversionRequest]
}
