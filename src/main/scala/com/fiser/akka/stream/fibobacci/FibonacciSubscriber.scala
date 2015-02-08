package com.fiser.akka.stream.fibobacci

import java.math.BigInteger

import akka.actor.ActorLogging
import akka.stream.actor.{WatermarkRequestStrategy, RequestStrategy, ActorSubscriber}
import akka.stream.actor.ActorSubscriberMessage.{OnError, OnComplete, OnNext}

class FibonacciSubscriber(delay: Long) extends ActorSubscriber with ActorLogging {


  override protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(50)

  def receive = {
    case OnNext(fib: BigInteger) =>
      log.debug("[FibonacciSubscriber] Received Fibonacci Number: {}", fib)
      Thread.sleep(delay)
    case OnError(err: Exception) =>                                                      // 4
      log.error(err, "[FibonacciSubscriber] Receieved Exception in Fibonacci Stream")
      context.stop(self)
    case OnComplete =>                                                                   // 5
      log.info("[FibonacciSubscriber] Fibonacci Stream Completed!")
      context.stop(self)
    case _ =>
  }
}
