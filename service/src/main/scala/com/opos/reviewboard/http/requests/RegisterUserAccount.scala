package com.opos.reviewboard.http.requests

import zio.json.JsonCodec

final case class RegisterUserRequest(
  email: String,
  password: String
) derives JsonCodec
