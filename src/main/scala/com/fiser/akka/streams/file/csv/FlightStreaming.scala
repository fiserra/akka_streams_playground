package com.fiser.akka.streams.file.csv

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.Try

object FlightStreaming extends App {
  implicit val system = ActorSystem("sys")
  implicit val materializer = ActorMaterializer()

  val flightDelayLines: Iterator[String] = scala.io.Source.fromFile("/home/rfiser/Dev/2008.csv", "utf-8").getLines()

  // immutable flow step to split apart our csv into a string array and transform each array into a FlightEvent
  val csvToFlightEvent: Flow[String, FlightEvent, Unit] = Flow[String]
    .map(_.split(",").map(_.trim)) // we now have our columns split by ","
    .map(stringArrayToFlightEvent) // we convert an array of columns to a FlightEvent

  // string array to FlightEvent
  def stringArrayToFlightEvent(cols: Array[String]): FlightEvent = new FlightEvent(cols(0), cols(1), cols(2), cols(3), cols(4), cols(5), cols(6), cols(7), cols(8), cols(9), cols(10), cols(11), cols(12), cols(13), cols(14), cols(15), cols(16), cols(17), cols(18), cols(19), cols(20), cols(21), cols(22), cols(23), cols(24), cols(25), cols(26), cols(27), cols(28))

  val filterAndConvert: Flow[FlightEvent, FlightDelayRecord, Unit] =
    Flow[FlightEvent].filter(r => Try(r.arrDelayMins.toInt).getOrElse(-1) > 0)
      .mapAsyncUnordered(parallelism = 2) {
        r: FlightEvent => Future(new FlightDelayRecord(r.year, r.month, r.dayOfMonth, r.flightNum, r.uniqueCarrier, r.arrDelayMins))
      }

  val averageCarrierDelay: Flow[FlightDelayRecord, (String, Int, Int), Unit] = Flow[FlightDelayRecord]
    .groupBy(30, _.uniqueCarrier)
    .fold(("", 0, 0)) {
      (x: (String, Int, Int), y: FlightDelayRecord) =>
        val count = x._2 + 1
        val totalMinutes = x._3 + Try(y.arrDelayMins.toInt).getOrElse(0)
        (y.uniqueCarrier, count, totalMinutes)
    }.mergeSubstreams

  def averageSink[A](a: A): Unit = {
    a match {
      case (a: String, b: Int, c: Int) => println(s"Delays for carrier $a: ${Try(c / b).getOrElse(0)} average mins, $b delayed flights")
      case x => println("no idea what " + x + "is!")
    }
  }

  val g: RunnableGraph[_] = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder =>

      // Source
      val A: Outlet[String] = builder.add(Source.fromIterator(() => flightDelayLines)).out

      //Flows
      val B: FlowShape[String, FlightEvent] = builder.add(csvToFlightEvent)
      val C: FlowShape[FlightEvent, FlightDelayRecord] = builder.add(filterAndConvert)
      val D: UniformFanOutShape[FlightDelayRecord, FlightDelayRecord] = builder.add(Broadcast[FlightDelayRecord](2))
      val F: FlowShape[FlightDelayRecord, (String, Int, Int)] = builder.add(averageCarrierDelay)

      //Sinks
      val E: Inlet[Any] = builder.add(Sink.ignore).in
      val G: Inlet[Any] = builder.add(Sink.foreach(averageSink)).in


      import GraphDSL.Implicits._
      // allows us to build our graph using ~> combinators

      // Graph
      A ~> B ~> C ~> D
      E <~ D
      G <~ F <~ D
      ClosedShape // defines this as a "closed" graph, not exposing any inlets or outlets
  })

  g.run() // materializes and executes the blueprint
}


case class FlightEvent(year: String,
                       month: String,
                       dayOfMonth: String,
                       dayOfWeek: String,
                       depTime: String,
                       scheduledDepTime: String,
                       arrTime: String,
                       scheduledArrTime: String,
                       uniqueCarrier: String,
                       flightNum: String,
                       tailNum: String,
                       actualElapsedMins: String,
                       crsElapsedMins: String,
                       airMins: String,
                       arrDelayMins: String,
                       depDelayMins: String,
                       originAirportCode: String,
                       destinationAirportCode: String,
                       distanceInMiles: String,
                       taxiInTimeMins: String,
                       taxiOutTimeMins: String,
                       flightCancelled: String,
                       cancellationCode: String, // (A = carrier, B = weather, C = NAS, D = security)
                       diverted: String, // 1 = yes, 0 = no
                       carrierDelayMins: String,
                       weatherDelayMins: String,
                       nasDelayMins: String,
                       securityDelayMins: String,
                       lateAircraftDelayMins: String)

case class FlightDelayRecord(year: String,
                             month: String,
                             dayOfMonth: String,
                             flightNum: String,
                             uniqueCarrier: String,
                             arrDelayMins: String) {
  override def toString = s"$year/$month/$dayOfMonth - $uniqueCarrier $flightNum - $arrDelayMins"
}
