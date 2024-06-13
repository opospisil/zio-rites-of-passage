package com.opos.reviewboard.http.controllers

import collection.mutable
import com.opos.reviewboard.domain.data.UserId
import com.opos.reviewboard.domain.errors.UnauthorizedException
import com.opos.reviewboard.http.endpoints.UserEndpoints
import com.opos.reviewboard.http.responses.UserResponse
import com.opos.reviewboard.services.JwtService
import com.opos.reviewboard.services.UserService
import sttp.tapir.auth.*
import sttp.tapir.server.ServerEndpoint
import zio.*

class UserController private (userService: UserService, jwtService: JwtService)
    extends BaseController
    with UserEndpoints {

  val create: ServerEndpoint[Any, Task] = createUserEndpoint.serverLogic { req =>
    userService
      .registerUser(req.email, req.password)
      .map(u => UserResponse(u.email))
      .either
  }

  val login: ServerEndpoint[Any, Task] = generateTokenEndpoint.serverLogic { req =>
    userService
      .generateJwtToken(req.email, req.password)
      .someOrFail(UnauthorizedException)
      .either
  }

  val updatePassword: ServerEndpoint[Any, Task] =
    updatePasswordEndpoint
      .serverSecurityLogic[UserId, Task](jwtService.verifyToken(_).either)
      .serverLogic { userId => req =>
        userService
          .updatePassword(req.email, req.oldPassword, req.newPassword)
          .map(u => UserResponse(u.email))
          .either
      }

  val delete: ServerEndpoint[Any, Task] =
    deleteUserEndpoint
      .serverSecurityLogic[UserId, Task](jwtService.verifyToken(_).either)
      .serverLogic { userId => req =>
        userService
          .deleteUser(req.email, req.password)
          .map(u => UserResponse(u.email))
          .either
      }

  override def routes: List[ServerEndpoint[Any, Task]] = List(
    create,
    login,
    updatePassword,
    delete
  )

}

object UserController {
  def makeZIO = for {
    service <- ZIO.service[UserService]
    jwt     <- ZIO.service[JwtService]
  } yield new UserController(service, jwt)
}
