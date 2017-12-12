package ru.yudnikov.superkassa

import akka.actor.{Actor, ActorSystem, Props, RootActorPath}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import com.typesafe.config.{Config, ConfigFactory}
import ru.yudnikov.superkassa.Messages.{JobRequest, RegisterBackend, UnregisterBackend}

class Backend extends Actor {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberUp], classOf[UnreachableMember], classOf[MemberRemoved])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive = {
    case MemberUp(member) =>
      register(member)
    case UnreachableMember(member) =>
      unregister(member)
    case MemberRemoved(member, _) =>
      unregister(member)
    case JobRequest(count) =>
      println(s"Working on $count...")
  }

  def register(member: Member): Unit = {
    context.actorSelection(RootActorPath(member.address) / "user" / "frontend") ! RegisterBackend
  }

  def unregister(member: Member): Unit = {
    context.actorSelection(RootActorPath(member.address) / "user" / "frontend") ! UnregisterBackend
  }
}

object Backend extends App {
  val actorSystemName = "ClusterSystem"
  val config = args match {
    case Array(hostname) =>
      ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname = ${"\"" + hostname + "\""}")
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
        .withFallback(ConfigFactory.load())
    case Array(hostname, port) =>
      ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port")
        .withFallback(ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname = ${"\"" + hostname + "\""}"))
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
        .withFallback(ConfigFactory.load())
  }
  val actorSystem: ActorSystem = ActorSystem(actorSystemName, config)
  actorSystem.actorOf(Props(classOf[Backend]), name = "backend")
  actorSystem.actorOf(Props(classOf[Monitor]))
}