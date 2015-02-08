package com.fiser.akka.stream.fibobacci

import java.math.BigInteger

import akka.actor.ActorLogging
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

class FibonacciPublisher extends ActorPublisher[BigInteger] with ActorLogging {
  var prev = BigInteger.ZERO
  var curr = BigInteger.ZERO

  def nextFib(): BigInteger = {
    if (curr == BigInteger.ZERO) {
      curr = BigInteger.ONE
    } else {
      val temp = prev.add(curr)
      prev = curr
      curr = temp
    }
    curr
  }

  def sendFibs(): Unit = {
    while (isActive && totalDemand > 0) {
      onNext(nextFib())
    }
  }

  def receive = {
    case Request(cnt) =>
      log.debug("[FibonacciPublisher] Received Request ({}) from Subscriber", cnt)
      sendFibs()
    case Cancel => // 3
      log.info("[FibonacciPublisher] Cancel Message Received -- Stopping")
      context.stop(self)
    case _ =>
  }
}
