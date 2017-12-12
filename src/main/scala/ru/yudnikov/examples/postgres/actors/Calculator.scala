package ru.yudnikov.examples.postgres.actors

import akka.actor.Actor
import akka.persistence.PersistentActor
import ru.yudnikov.examples.postgres.actors.Calculator.{Request, Response}

class Calculator extends Actor {
  override def receive = {
    case Request() =>
      println(s"calculating...")
      sender() ! Response()
  }
}

object Calculator {
  case class Request()
  case class Response()
}
