package com.opos.reviewboard

import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase

object Repository {

  def quillLayer =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  def dataSourceLayer =
    Quill.DataSource.fromPrefix("reviewboard.db")

  val dataLayer = dataSourceLayer >>> quillLayer
}
