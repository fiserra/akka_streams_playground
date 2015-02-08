package com.fiser.akka.stream.fibobacci

import java.math.BigInteger

import akka.actor.ActorLogging
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.actor.ActorSubscriberMessage.{OnError, OnComplete, OnNext}
import akka.stream.actor.{MaxInFlightRequestStrategy, ActorPublisher, ActorSubscriber}

import scala.collection.mutable

class DoublingProcessor extends ActorSubscriber with ActorPublisher[BigInteger] with ActorLogging {
  val dos = BigInteger.valueOf(2l)
  val doubledQueue = mutable.Queue[BigInteger]()

  def receive = {
    case OnNext(biggie: BigInteger) =>
      doubledQueue.enqueue(biggie.multiply(dos))
      sendDoubled()
    case OnError(err: Exception) =>
      onError(err)
      context.stop(self)
    case OnComplete =>
      onComplete()
      context.stop(self)
    case Request(cnt) =>
      sendDoubled()
    case Cancel =>
      cancel()
      context.stop(self)
    case _ =>
  }

  def sendDoubled() {
    while (isActive && totalDemand > 0 && doubledQueue.nonEmpty) {
      onNext(doubledQueue.dequeue())
    }
  }

  val requestStrategy = new MaxInFlightRequestStrategy(50) {
    def inFlightInternally(): Int = {
      doubledQueue.size
    }
  }

}
