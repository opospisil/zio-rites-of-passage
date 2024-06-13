package com.opos.reviewboard.http.endpoints

import com.opos.reviewboard.domain.data.*
import com.opos.reviewboard.http.requests.*
import com.opos.reviewboard.http.responses.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait UserEndpoints extends BaseEndpoint {

  // POST /users { email, password } => { email }
  val createUserEndpoint =
    baseEndpoint
      .tag("Users")
      .name("register")
      .description("Register a new user with email and password")
      .in("users")
      .post
      .in(jsonBody[RegisterUserRequest])
      .out(jsonBody[UserResponse])

  // PUT /users/password { email, oldPassword, newPassword } => { email }
  // TODO - should be an authorized enpoint
  val updatePasswordEndpoint =
    securedEndpoint
      .tag("Users")
      .name("updatePassword")
      .description("Update the password of a user")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[UserResponse])

  // DELETE /users { email, password } => { email }
  val deleteUserEndpoint =
    securedEndpoint
      .tag("Users")
      .name("delete")
      .description("Delete an user")
      .in("users")
      .delete
      .in(jsonBody[DeleteUserRequest])
      .out(jsonBody[UserResponse])

  // POST /users/login { email, password } => { email,,jwtToken, expirationDate }    
  val generateTokenEndpoint =
    baseEndpoint
      .tag("Users")
      .name("generateToken")
      .description("Generate a token for a user")
      .in("users" / "login")
      .post
      .in(jsonBody[LoginRequest])
      .out(jsonBody[UserToken])

}
