package com.opos.reviewboard.services

import com.opos.reviewboard.domain.data.*
import com.opos.reviewboard.repository.UserRepository
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import zio.*

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User]
  def deleteUser(email: String, password: String): Task[User]
  def generateJwtToken(email: String, password: String): Task[Option[UserToken]]
}

class UserServiceLive private (userRepo: UserRepository, jwt: JwtService) extends UserService {
  override def registerUser(email: String, password: String): Task[User] =
    userRepo.create(
      User(
        id = -1L, // will be generated by the database
        email = email,
        hashPassword = UserServiceLive.Hasher.generateHash(password)
      )
    )

  override def verifyPassword(email: String, password: String): Task[Boolean] =
    for {
      existingUser <- userRepo.getByEmail(email)
      result <- existingUser match {
                  case Some(user) =>
                    ZIO
                      .attempt(UserServiceLive.Hasher.validateHash(password, user.hashPassword))
                      .orElseSucceed(false) // if exception occurs, return false
                  case None => ZIO.succeed(false)
                }
    } yield result

  override def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User] =
    for {
      existingUser <- userRepo.getByEmail(email).someOrFail(new RuntimeException(s"User not found for ${email}"))
      verified <- ZIO.attempt {
                    UserServiceLive.Hasher.validateHash(oldPassword, existingUser.hashPassword)
                  }
      updatedUser <- userRepo
                       .update(
                         existingUser.id,
                         user => existingUser.copy(hashPassword = UserServiceLive.Hasher.generateHash(newPassword))
                       )
                       .when(verified)
                       .someOrFail(new RuntimeException(s"Cannot update password for ${email}"))
    } yield updatedUser

  override def deleteUser(email: String, password: String): Task[User] =
    for {
      existingUser <- userRepo.getByEmail(email).someOrFail(new RuntimeException(s"User not found for ${email}"))
      verified <- ZIO.attempt {
                    UserServiceLive.Hasher.validateHash(password, existingUser.hashPassword)
                  }
      deletedUser <- userRepo
                       .delete(existingUser.id)
                       .when(verified)
                       .someOrFail(new RuntimeException(s"Cannot delete user for ${email}"))
    } yield deletedUser

  override def generateJwtToken(email: String, password: String): Task[Option[UserToken]] =
    for {
      existingUser <- userRepo
                        .getByEmail(email)
                        .someOrFail(new RuntimeException(s"Cannot verify user for email: ${email}"))
      verified <- verifyPassword(email, password)
      token    <- jwt.createToken(existingUser).when(verified)
    } yield token

}

object UserServiceLive {
  val layer = ZLayer {
    for {
      userRepo <- ZIO.service[UserRepository]
      jwt      <- ZIO.service[JwtService]
    } yield new UserServiceLive(userRepo, jwt)
  }

  object Hasher {

    private val PBKDF2_ALGORITHM    = "PBKDF2WithHmacSHA512"
    private val PBKDF2_ITERATIONS   = 10000
    private val SALT_BYTE_SIZE: Int = 24
    private val HASBYTE_SIZE: Int   = 24
    private val skf                 = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

    private def pbkdf2(password: Array[Char], salt: Array[Byte], nIterations: Int, nBytes: Int): Array[Byte] =
      val spec = new PBEKeySpec(password, salt, nIterations, nBytes * 8)
      skf.generateSecret(spec).getEncoded()

    private def toHex(bytes: Array[Byte]): String =
      bytes.map(b => "%02x".format(b)).mkString

    private def fromHex(hex: String): Array[Byte] =
      hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)

    // a(i) ^ b(i) for every i
    private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean =
      val range = 0 until math.min(a.length, b.length)
      val diff = range.foldLeft(a.length ^ b.length) { case (acc, i) =>
        acc | a(i) ^ b(i)
      }
      diff == 0

    // string + salt + nIterations
    // 1000:AAAAAAAAAA:BBBBBBBB"
    def generateHash(password: String): String =
      val rng  = new SecureRandom()
      val salt = new Array[Byte](SALT_BYTE_SIZE)
      rng.nextBytes(salt)
      val hashBytes = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASBYTE_SIZE)
      s"${PBKDF2_ITERATIONS}:${toHex(salt)}:${toHex(hashBytes)}"

    def validateHash(password: String, hash: String): Boolean =
      val parts      = hash.split(":")
      val iterations = parts(0).toInt
      val salt       = fromHex(parts(1))
      val hashBytes  = fromHex(parts(2))
      val testHash   = pbkdf2(password.toCharArray(), salt, iterations, hashBytes.length)
      compareBytes(hashBytes, testHash)
  }
}

object UserServiceDemo extends App {
  val pwd = "password"
  println("Plain password: " + pwd)
  val generatedHash = UserServiceLive.Hasher.generateHash(pwd)
  println("Hashed password: " + generatedHash)
  println("Validating password: " + UserServiceLive.Hasher.validateHash("password", generatedHash))
  val manualHash =
    "10000:edb9735e284aa95abcbb73afedae6edd9d4f7bc1b941b6df:6e1871239f3d695f728f5ffcac4116e113a98fc9d4a0ac5c"
  println("Validating manual hash: " + UserServiceLive.Hasher.validateHash("passwordaah", manualHash))
}
