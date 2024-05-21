package com.opos.reviewboard.http.endpoints

import com.opos.reviewboard.domain.errors.HttpError
import sttp.tapir.*

trait BaseEndpoint {

  val baseEndpoint =
    endpoint
      .errorOut(statusCode and plainBody[String]) //(StatusCode, String)
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)

}
