package com.opos.reviewboard.repository

import java.sql.SQLException
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import zio.*
import zio.test.*

trait RepositorySpec {
  private def createContainer = {
    val container: PostgreSQLContainer[Nothing] = PostgreSQLContainer("postgres").withInitScript("sql/companies.sql")

    container.start()
    container
  }

  private def closeContainer(container: PostgreSQLContainer[Nothing]) =
    container.stop()

  private def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(container.getJdbcUrl())
    dataSource.setUser(container.getUsername())
    dataSource.setPassword(container.getPassword())
    dataSource
  }

  val dataSourceLayer = ZLayer {
    for {
      container <-
        ZIO.acquireRelease(ZIO.attempt(createContainer))(container => ZIO.attempt(container.stop()).ignoreLogged)
      dataSource <- ZIO.attempt(createDataSource(container))
    } yield dataSource
  }
}
