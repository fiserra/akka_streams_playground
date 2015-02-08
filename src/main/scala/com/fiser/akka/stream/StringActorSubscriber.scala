package com.fiser.akka.stream

import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}

class StringActorSubscriber extends ActorSubscriber {
  override protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(10)

  def processElement(element: String) = ???
  def handleError(ex: Throwable) = ???
  def streamFinished() = ???

  def receive = {
    case OnNext(element) =>
      processElement(element.asInstanceOf[String])
    case OnError(ex) =>
      handleError(ex)
    case OnComplete =>
      streamFinished()
  }
}
