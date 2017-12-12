package ru.yudnikov._trash.backend.actors

import akka.actor.Actor

class Worker extends Actor {
  override def receive = {
    case s: String => println(s)
  }
}
