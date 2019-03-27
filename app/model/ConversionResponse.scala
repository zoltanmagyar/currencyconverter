package model

import play.api.libs.json.{Format, Json}

/**
  * Response payload
  */
case class ConversionResponse(exchange: BigDecimal, amount: BigDecimal, original: BigDecimal)

object ConversionResponse {
  implicit val conversionResponseFormat: Format[ConversionResponse] = Json.format[ConversionResponse]
}
