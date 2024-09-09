package com.opos.reviewboard.services

import collection.mutable
import com.opos.reviewboard.domain.data.*
import com.opos.reviewboard.repository.UserRepository
import zio.*
import zio.test.*

object UserServiceSpec extends ZIOSpecDefault {

  private val user1 = User(
    1L,
    "user1@email.com",
    // password: password
    "10000:edb9735e284aa95abcbb73afedae6edd9d4f7bc1b941b6df:6e1871239f3d695f728f5ffcac4116e113a98fc9d4a0ac5c"
  )

  private val user2 = User(
    2L,
    "user2@email.com",
    "10000:edb9735e284aa95abcbb73afedae6edd9d4f7bc1b941b6df:6e1871239f3d695f728f5ffcac4116e113a98fc9d4a0ac5c"
  )

  val stubRepoLayer = ZLayer.succeed {
    new UserRepository {
      val db = mutable.Map[Long, User](user1.id -> user1)

      override def create(user: User): Task[User] =
        ZIO.succeed {
          db += (user.id -> user)
          user
        }

      override def getById(id: Long): Task[Option[User]] = ZIO.succeed(db.get(id))

      override def getByEmail(email: String): Task[Option[User]] = ZIO.succeed(db.values.find(_.email == email))

      override def update(id: Long, op: User => User): Task[User] = ZIO.attempt {
        val user    = db(id)
        val updated = op(user)
        db += (id -> updated)
        updated
      }

      override def delete(id: Long): Task[User] = ZIO.attempt {
        val user = db(id)
        db -= id
        user
      }
    }
  }

  val stubJwtLayer = ZLayer.succeed {
    new JwtService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(user1.email, "BIG ACCESS", Long.MaxValue))

      override def verifyToken(token: String): Task[UserId] = ZIO.succeed(UserId(user1.id, user1.email))
    }
  }

  override def spec =
    suite("UserServiceSpec")(
      test("create and validate user") {
        for {
          service <- ZIO.service[UserService]
          user    <- service.registerUser(user2.email, "password")
          valid   <- service.verifyPassword(user.email, "password")
        } yield assertTrue(
          user.email == user2.email
        )
      },
      test("validate correct credentials") {
        for {
          service <- ZIO.service[UserService]
          valid   <- service.verifyPassword(user1.email, "password")
        } yield assertTrue(valid)
      },
      test("validate incorrect password") {
        for {
          service <- ZIO.service[UserService]
          valid   <- service.verifyPassword(user1.email, "wrongpassword")
        } yield assertTrue(!valid)
      },
      test("update password") {
        for {
          service <- ZIO.service[UserService]
          user    <- service.updatePassword(user1.email, "password", "newpassword")
          valid   <- service.verifyPassword(user1.email, "newpassword")
        } yield assertTrue(valid)
      },
      test("update non-existent user") {
        for {
          service <- ZIO.service[UserService]
          err     <- service.updatePassword("nobody@email.com", "password", "newpassword").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete user") {
        for {
          service <- ZIO.service[UserService]
          deleted <- service.deleteUser(user1.email, "password")
        } yield assertTrue(deleted.email == user1.email)
      }
    ).provide(
      UserServiceLive.layer,
      stubRepoLayer,
      stubJwtLayer
    )

}
