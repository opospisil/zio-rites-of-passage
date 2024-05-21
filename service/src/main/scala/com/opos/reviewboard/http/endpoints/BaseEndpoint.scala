package com.opos.reviewboard.http.endpoints

import sttp.tapir.*
import com.opos.reviewboard.domain.errors.HttpError

trait BaseEndpoint {

  val baseEndpoint =
    endpoint
      .errorOut(statusCode and plainBody[String]) //(StatusCode, String)
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)

}
