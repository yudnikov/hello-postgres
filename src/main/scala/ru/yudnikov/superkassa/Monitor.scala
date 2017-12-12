package ru.yudnikov.superkassa

import akka.actor.{Actor, ActorRef}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._

class Monitor extends Actor {

  private val cluster = Cluster(context.system)

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
