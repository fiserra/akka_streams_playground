package com.fiser.akka.stream.fibobacci

import java.math.BigInteger

import akka.actor.{Props, ActorSystem}
import akka.stream.actor.{ActorPublisher, ActorSubscriber}

object Main extends App {

  val system = ActorSystem()

  val pubRef = system.actorOf(Props[FibonacciPublisher])
  val publisher = ActorPublisher[BigInteger](pubRef)

  val subRef = system.actorOf(Props(new FibonacciSubscriber(500L))) // 500 ms delay
  val subscriber = ActorSubscriber[BigInteger](subRef)

  val procRef = system.actorOf(Props[DoublingProcessor])
  val procSubscriber = ActorSubscriber[BigInteger](procRef)
  val procPublisher = ActorPublisher[BigInteger](procRef)

  publisher.subscribe(procSubscriber)
  procPublisher.subscribe(subscriber)
}