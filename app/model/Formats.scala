package model

import org.joda.money.CurrencyUnit
import play.api.libs.json._

import scala.util.{Success, Try}

object Formats {
  implicit val currencyUnitReads: Reads[CurrencyUnit] = {
    case JsString(s) => Try(CurrencyUnit.of(s.toUpperCase)).fold(
      { _ => JsError("error.expected.currencyunit") },
      { JsSuccess(_) }
    )
    case _ => JsError("error.expected.currencyunit")
  }
  implicit val currencyUnitWrites: Writes[CurrencyUnit] = (currencyUnit: CurrencyUnit) => JsString(currencyUnit.getCode)

  implicit val ratesMapReads: Reads[Map[CurrencyUnit, Double]] = {
    case JsObject(kvs) =>
      val validKvs = kvs.map {
        case (k, v) => Try(CurrencyUnit.of(k) -> v.as[Double])
      }.collect {
        // drop the invalid ones but could fail the whole if any fails
        case Success(values) => values
      }.toMap
      JsSuccess(validKvs)
    case _ => JsError("Expected JSON object")
  }
}
