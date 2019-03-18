package model

import play.api.libs.json.{Format, Json}

/**
  * Response payload
  */
case class ConversionResponse(exchange: Double, amount: Double, original: Double)

object ConversionResponse {
  implicit val conversionResponseFormat: Format[ConversionResponse] = Json.format[ConversionResponse]
}
