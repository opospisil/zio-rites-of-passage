package com.opos.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoint extends BaseEndpoint {

  val healthEndpoint = baseEndpoint
    .tag("health")
    .name("health")
    .description("healtcheck endpoint")
    .get
    .in("health")
    .out(plainBody[String])

  val errorEndpoint = baseEndpoint
    .tag("health")
    .name("error")
    .description("error endpoint for testing purposes")
    .get
    .in("health" / "error")
    .out(plainBody[String])

}
