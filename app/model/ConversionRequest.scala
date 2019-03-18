package model

import Formats._
import org.joda.money.CurrencyUnit
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
  * Request payload to convert currencies
  */
case class ConversionRequest(fromCurrency: CurrencyUnit, toCurrency: CurrencyUnit, amount: Double)

object ConversionRequest {
  implicit val conversionRequestReads: Reads[ConversionRequest] = (
    (JsPath \ "fromCurrency").read[CurrencyUnit] and
      (JsPath \ "toCurrency").read[CurrencyUnit] and
      (JsPath \ "amount").read[Double](min(0.0))
    )(ConversionRequest.apply _)
  implicit val conversionRequestWrites: Writes[ConversionRequest] = Json.writes[ConversionRequest]
}
