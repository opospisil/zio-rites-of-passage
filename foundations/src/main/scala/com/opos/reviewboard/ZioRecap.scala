package com.opos.zrp

import scala.io.StdIn
import zio._

object ZioRecap extends ZIOAppDefault {

  val meaningOfLife = ZIO.succeed(42)

  val aSusp: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife)

  val interact = for {
    _         <- Console.printLine("Henlo, say something!")
    something <- ZIO.succeed(StdIn.readLine())
    _         <- Console.printLine(s" tady je neco: ${something}")
  } yield ()

  //error handling
  val anAttempt = ZIO.attempt {
    println("Try something")
    val string: String = null
    string.length()
  }.catchSome {
    case e: RuntimeException => ZIO.succeed(s"Got runtime exception: ${e}")
    case _                   => ZIO.succeed("Ignoring everything else")
  }

  // fibers
  val delayedVal: ZIO[Any, Nothing, Int] = ZIO.sleep(1.second) *> Random.nextIntBetween(1, 100)

  val aPair = for {
    a <- delayedVal
    b <- delayedVal
  } yield (a, b) // this takes 2 seconds

  val aPairPair = for {
    fibA <- delayedVal.fork // returns a fiber effect
    fibB <- delayedVal.fork
    a    <- fibA.join
    b    <- fibB.join
  } yield (a, b) // this takes 1 second

  val interruptedFiber = for {
    fib <- delayedVal.onInterrupt(ZIO.succeed(println("I'm interrupted!"))).fork
    _   <- ZIO.sleep(500.millis) *> Console.printLine("Cancelling fiber") *> fib.interrupt
    _   <- fib.join
  } yield ()

  val ignoredInterruption = for {
    fib <- ZIO.uninterruptible(delayedVal.map(println).onInterrupt(ZIO.succeed(println("I'm interrupted!")))).fork
    _   <- ZIO.sleep(500.millis) *> Console.printLine("Cancelling fiber") *> fib.interrupt
    _   <- fib.join
  } yield ()

  val randomx10 = ZIO.collectAllPar((1 to 10).map(_ => delayedVal.map(println)))

  // dependencies
  case class User(name: String, email: String)
  class UserSubscriptionService(emailService: EmailService, userDatabase: UserdatabaseService) {
    def subcribeUser(user: User): Task[Unit] = for {
      _ <- emailService.sendEmail(user)
      _ <- userDatabase.saveUser(user)
      _ <- ZIO.succeed((s"Subscribed user ${user}"))
    } yield ()
  }

  object UserSubscriptionService {
    val live = ZLayer.fromFunction((emailS, userDS) => new UserSubscriptionService(emailS, userDS))
  }

  class EmailService {
    def sendEmail(user: User): Task[Unit] = ZIO.succeed(s"Sending email to ${user}")
  }

  object EmailService {
    val live = ZLayer.succeed(new EmailService)
  }

  class UserdatabaseService(connectionPool: ConnectionPool) {
    def saveUser(user: User): Task[Unit] = ZIO.succeed(s"Saving user ${user}")
  }

  object UserdatabaseService {
    val live = ZLayer.fromFunction((connectionPool: ConnectionPool) => new UserdatabaseService(connectionPool))
  }

  class ConnectionPool(nConnection: Int) {
    def getConnection: Task[Connection] = ZIO.succeed(Connection())
  }

  object ConnectionPool {
    def live(nConns: Int) = ZLayer.succeed(new ConnectionPool(nConns))
  }

  case class Connection()

  def subcribeUser(user: User): ZIO[UserSubscriptionService, Throwable, Unit] = for {
    sub <- ZIO.service[UserSubscriptionService]
    _   <- sub.subcribeUser(user)
  } yield ()

  val program = for {
    _ <- subcribeUser(User("John", "doe@mail.com"))
    _ <- subcribeUser(User("Jane", "moe@mail.com"))
  } yield ()

  override def run = program.provide(
    ConnectionPool.live(10),
    UserdatabaseService.live,
    EmailService.live,
    UserSubscriptionService.live
  )

}
