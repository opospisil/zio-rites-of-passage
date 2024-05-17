package com.opos.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoint {

  val healthEndpoint = endpoint
    .tag("health")
    .name("health")
    .description("healtcheck endpoint")
    .get
    .in("health")
    .out(plainBody[String])

}
