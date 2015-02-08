package com.fiser.akka.stream

import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request, SubscriptionTimeoutExceeded}

import scala.util.{Failure, Success, Try}

class StringActorPublisher extends ActorPublisher[String] {
  def generateElement(): Try[Option[String]] = ???

  def cleanupResources() = ???

  def receive = {
    case Request(n) =>
      while (isActive && totalDemand > 0) {
        generateElement() match {
          case Success(valueOpt) =>
            valueOpt
              .map(element => onNext(element))
              .getOrElse(onComplete())
          case Failure(ex) =>
            onError(ex)
        }
      }
    case Cancel =>
      cleanupResources()
    case SubscriptionTimeoutExceeded =>
      cleanupResources()
  }
}
