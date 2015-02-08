package com.fiser.akka.stream

import akka.actor.{Props, ActorSystem}
import akka.stream.actor.{ActorPublisher, ActorSubscriber}
import org.reactivestreams.{Publisher, Subscriber}

object Main extends App {

  val system = ActorSystem()

  val subscriber: Subscriber[String] = ActorSubscriber[String](system.actorOf(
    Props(classOf[StringActorSubscriber])))

  val publisher: Publisher[String] = ActorPublisher[String](system.actorOf(
    Props(classOf[StringActorPublisher])))

}
