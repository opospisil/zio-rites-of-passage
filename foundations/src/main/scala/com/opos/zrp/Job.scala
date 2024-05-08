package com.opos.zrp

import zio.json.JsonCodec
import zio.json.DeriveJsonCodec

case class Job(id: Long, title: String, url: String, Company: String)

object Job {
  implicit val jobCodec: JsonCodec[Job] = DeriveJsonCodec.gen[Job]
}
