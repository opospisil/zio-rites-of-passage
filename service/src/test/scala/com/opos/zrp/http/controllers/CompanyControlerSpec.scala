package com.opos.zrp.http.controllers

import zio.test.*
import zio.*
import zio.json.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.generic.auto
import sttp.client3.testing.SttpBackendStub
import sttp.client3.*
import sttp.monad.MonadError
import sttp.tapir.ztapir.RIOMonadError
import com.opos.zrp.http.requests.CreateCompanyRequest
import com.opos.zrp.domain.data.Company
import com.opos.zrp.syntax.*
import sttp.tapir.server.ServerEndpoint

object CompanyControlerSpec extends ZIOSpecDefault {

  private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO(endpointFn: CompanyController => ServerEndpoint[Any, Task]) = for {
    // create the controller
    controllerZIO <- CompanyController.makeZIO
    // build tapir backend
    backendStub <- ZIO.succeed {
                     TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
                       .whenServerEndpointRunLogic(endpointFn(controllerZIO))
                       .backend()
                   }
  } yield backendStub

  override def spec =
    suite("CompanyControlerSpec")(
      test("simple test") {
        assertZIO(ZIO.succeed(1 + 1))(
          Assertion.assertion("basic math")(_ == 2)
        )
      },
      test("post company") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
                        .post(uri"/companies")
                        .body(CreateCompanyRequest("Test Company inc.", "test.com").toJson)
                        .send(backendStub)

        } yield response.body

        // inspect the response
        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption) // Option[Company]
            .contains(Company(1, "test-company-inc.", "Test Company inc.", "test.com"))
        }
      },
      test("get all compaines") {
        val program = for {
          backendStub <- backendStubZIO(_.getAll)
          response <- basicRequest
                        .get(uri"/companies")
                        .send(backendStub)

        } yield response.body
        // inspect the response
        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[List[Company]].toOption) // Option[Company]
            .contains(List.empty)
        }
      }
    )
}
