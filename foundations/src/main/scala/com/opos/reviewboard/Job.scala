package com.opos.zrp

import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

case class Job(id: Long, title: String, url: String, Company: String)

object Job {
  implicit val jobCodec: JsonCodec[Job] = DeriveJsonCodec.gen[Job]
}
