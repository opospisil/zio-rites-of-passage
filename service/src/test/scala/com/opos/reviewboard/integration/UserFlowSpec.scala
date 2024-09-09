package com.opos.reviewboard.integration

import com.opos.reviewboard.Repository
import com.opos.reviewboard.Repository.dataSourceLayer
import com.opos.reviewboard.Repository.quillLayer
import com.opos.reviewboard.domain.data.*
import com.opos.reviewboard.http.controllers.*
import com.opos.reviewboard.http.requests.*
import com.opos.reviewboard.http.responses.UserResponse
import com.opos.reviewboard.repository.RepositorySpec
import com.opos.reviewboard.repository.UserRepositoryLive
import com.opos.reviewboard.services.*
import com.opos.reviewboard.syntax.*
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method
import sttp.monad.MonadError
import sttp.tapir.generic.auto
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.json.*
import zio.test.*

object UserFlowSpec extends ZIOSpecDefault with RepositorySpec {

  // http controller
  // service
  // repository
  // test container for
  //
  override val initScript: String = "sql/integration.sql"

  private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO = for {
    // create the controller
    controllerZIO <- UserController.makeZIO
    // build tapir backend
    backendStub <- ZIO.succeed {
                     TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
                       .whenServerEndpointsRunLogic(controllerZIO.routes)
                       .backend()
                   }
  } yield backendStub

  extension [A: JsonCodec](backend: SttpBackend[Task, Nothing]) {
    def sendRequest[B: JsonCodec](
      method: Method,
      path: String,
      payload: A,
      maybeToken: Option[String] = None
    ): Task[Option[B]] =
      basicRequest
        .method(method, uri"${path}")
        .body(payload.toJson)
        .auth
        .bearer(maybeToken.getOrElse(""))
        .send(backend)
        .map(_.body.toOption.flatMap(_.fromJson[B].toOption))

    def post[B: JsonCodec](path: String, payload: A, maybeToken: Option[String] = None): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, maybeToken)

    def postAuth[B: JsonCodec](path: String, payload: A, maybeToken: String): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, Some(maybeToken))

    def put[B: JsonCodec](path: String, payload: A, maybeToken: Option[String] = None): Task[Option[B]] =
      sendRequest(Method.PUT, path, payload, maybeToken)

    def putAuth[B: JsonCodec](path: String, payload: A, maybeToken: String): Task[Option[B]] =
      sendRequest(Method.PUT, path, payload, Some(maybeToken))
  }

  override def spec =
    suite("UserFlowSpec")(
      test("create user") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub
                             .post[UserResponse](
                               "/users",
                               RegisterUserRequest("user1@email.com", "password")
                             )
          _ <- Console.printLine(maybeResponse)
        } yield assertTrue(
          maybeResponse.nonEmpty &&
            maybeResponse.get.email == "user1@email.com"
        )
      },
      test("create and log in") {
        for {
          backendStub <- backendStubZIO
          createdUser <- backendStub
                           .post[UserResponse]("/users", RegisterUserRequest("user@email.com", "password"))
          maybeToken <- backendStub
                          .post[UserToken]("users/login", LoginRequest("user@email.com", "password"))
        } yield assertTrue(
          maybeToken.filter(_.email == createdUser.get.email).nonEmpty
        )
      },
      test("change password") {
        for {
          backendStub <- backendStubZIO
          createdUser <- backendStub.post[UserResponse]("/users", RegisterUserRequest("user@email.com", "password"))
          token <- backendStub
                     .post[UserToken]("users/login", LoginRequest("user@email.com", "password"))
                     .someOrFail(new RuntimeException("Token not found"))
          up <- backendStub.putAuth[UserResponse](
                  "/users/password",
                  UpdatePasswordRequest("user@email.com", "password", "newpassword"),
                  token.token
                )
          maybeOldToken <- backendStub.post[UserToken]("users/login", LoginRequest("user@email.com", "password"))
          maybeNewToken <- backendStub.post[UserToken]("users/login", LoginRequest("user@email.com", "newpassword"))
        } yield assertTrue(
          up.get.email == createdUser.get.email &&
            maybeOldToken.isEmpty &&
            maybeNewToken.nonEmpty
        )
      }
    ).provide(
      UserServiceLive.layer,
      JwtServiceLive.configuredLayer,
      UserRepositoryLive.layer,
      quillLayer,
      dataSourceLayer,
      Scope.default
    )
}
