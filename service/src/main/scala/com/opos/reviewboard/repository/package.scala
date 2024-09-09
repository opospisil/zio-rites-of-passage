package com.opos.reviewboard

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

object Repository {

  def quillLayer =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  def dataSourceLayer =
    Quill.DataSource.fromPrefix("reviewboard.db")

  val dataLayer = dataSourceLayer >>> quillLayer
}
