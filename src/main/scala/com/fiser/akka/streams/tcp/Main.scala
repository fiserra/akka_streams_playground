/*
 * Copyright 2003-2015 Monitise Group Limited. All Rights Reserved.
 *
 * Save to the extent permitted by law, you may not use, copy, modify,
 * distribute or create derivative works of this material or any part
 * of it without the prior written consent of Monitise Group Limited.
 * Any reproduction of this material must contain this notice.
 */
package com.fiser.akka.streams.tcp

object Main extends App {
  import akka.stream.scaladsl._
  import akka.stream.ActorMaterializer
  import akka.actor.ActorSystem

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val numberReverserFlow: Flow[Int, String, Unit] = Flow[Int].map(_.toString.reverse)

  numberReverserFlow.runWith(Source(100 to 200), Sink.foreach(println))

  Source(100 to 200).via(numberReverserFlow).to(Sink.foreach(println)).run()

  //The disadvantage of this approach is that the transformation logic cannot be re-used.
  Source(100 to 200).map(_.toString.reverse).to(Sink.foreach(println)).run()
}
