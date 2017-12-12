package ru.yudnikov.examples.postgres.actors

import akka.actor.Actor
import org.postgresql.PGNotification
import ru.yudnikov.examples.postgres.PostgresBackend
import ru.yudnikov.examples.postgres.actors.Poller.Poll

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class Poller extends Actor {

  private val scheduler = context.system.scheduler
  private val checkNotificationTask = scheduler.schedule(1 second, 5 second, self, Poll)

  override def preStart(): Unit = {
    PostgresBackend.execute("LISTEN events;")
  }

  override def postStop(): Unit = {
    checkNotificationTask.cancel()
  }

  override def receive = {
    case Poll =>
      val notifications = Option(PostgresBackend.connection.getNotifications).getOrElse(Array[PGNotification]())
      notifications foreach { notification =>
        println(notification.getParameter)
      }
    case _ =>
  }
}

object Poller {
  case object Poll
}
