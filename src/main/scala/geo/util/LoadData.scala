package geo.util

import java.nio.file.Paths

import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString
import akka.{Done, NotUsed}
import geo.entity.Entity

import scala.concurrent.{ExecutionContext, Future}

class LoadData[T <: Entity](fileName: String)
                           (implicit materializer: Materializer, dispatcher: ExecutionContext) {
  self: BaseParsers[T] ⇒

  val lineDelimiter: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), 9000)

  def load(f: T ⇒ Unit): Future[Done] = {
    FileIO.fromPath(Paths.get(fileName))
      .via(lineDelimiter)
      .filter(_.nonEmpty)
      .mapAsync(4)(b ⇒ Future.successful(parseRoot(b.utf8String)))
      .collect { case Some(t) ⇒ t }
      .runWith(Sink.foreachParallel(4)(f))
  }
}