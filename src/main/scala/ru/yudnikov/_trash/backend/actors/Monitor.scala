package ru.yudnikov._trash.backend.actors

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

class Monitor extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive = {
    case MemberUp(member) =>
      println(s"Member up ${member.address}")
    case UnreachableMember(member) =>
      println(s"Member is unreachable ${member.address}")
    case MemberRemoved(member, previousStatus) =>
      println(s"Member removed ${member.address} after $previousStatus")
    case _ =>
  }
}

