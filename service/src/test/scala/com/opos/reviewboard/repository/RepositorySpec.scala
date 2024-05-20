package com.opos.reviewboard.repository

import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import zio.*
import zio.test.*

trait RepositorySpec {

  val initScript: String

  private def createContainer = {
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres")
        .withInitScript(initScript)

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
