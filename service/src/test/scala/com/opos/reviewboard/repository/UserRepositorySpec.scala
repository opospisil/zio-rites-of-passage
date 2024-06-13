package com.opos.reviewboard.repository

import com.opos.reviewboard.Repository
import com.opos.reviewboard.domain.data.User
import com.opos.reviewboard.syntax.*
import zio.*
import zio.internal.stacktracer.SourceLocation
import zio.test.*

object UserRepositorySpec extends ZIOSpecDefault with RepositorySpec {

  override val initScript = "sql/users.sql"

  val goodUser = User(
    id = 1L,
    email = "usr@email.com",
    hashPassword = "hash"
  )

  override def spec =
    suite("UserRepositorySpec")(
      test("create user") {
        for {
          repo <- ZIO.service[UserRepository]
          user <- repo.create(goodUser)
        } yield assertTrue(
          user.email == goodUser.email &&
            user.hashPassword == goodUser.hashPassword
        )
      },
      test("Get by id") {
        for {
          repo <- ZIO.service[UserRepository]
          user <- repo.getById(goodUser.id)
        } yield assertTrue(
          user.isDefined &&
            user.get.email == goodUser.email &&
            user.get.hashPassword == goodUser.hashPassword
        )
      },
      test("Get by email") {
        for {
          repo <- ZIO.service[UserRepository]
          user <- repo.getByEmail(goodUser.email)
        } yield assertTrue(
          true
        )
      }
    ).provide(
      UserRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )

}
