package ru.yudnikov.examples.postgres.actors

import akka.actor.Actor
import ru.yudnikov.examples.postgres.PostgresBackend
import ru.yudnikov.examples.postgres.actors.Spammer.Spam

import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class Spammer extends Actor {

  private val r = new Random(System.nanoTime())

  private val scheduler = context.system.scheduler
  private val spamTask = scheduler.schedule(0 second, 3 second, self, Spam)

  override def preStart(): Unit = {
    PostgresBackend.execute("create table if not exists products (id BIGINT, name VARCHAR(255));")
    //PostgresBackend.execute("TRUNCATE TABLE products;")
  }

  override def postStop(): Unit = {
    spamTask.cancel()
  }

  override def receive = {
    case Spam =>
      val i = r.nextInt(1000)
      PostgresBackend.execute(s"INSERT INTO products (id, name) VALUES ($i, 'Product $i');")
    case _ =>
  }
}

object Spammer {
  case object Spam
}
