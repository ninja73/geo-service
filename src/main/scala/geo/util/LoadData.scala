package geo.util

import java.nio.file.Paths

import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import akka.{Done, NotUsed}
import geo.entity.Entity
import scala.collection.immutable.Iterable

import scala.concurrent.{ExecutionContext, Future}

class LoadData[T <: Entity](fileName: String)
                           (implicit materializer: Materializer, dispatcher: ExecutionContext) {
  self: Transformer[T] with BaseParsers[T] ⇒

  val lineDelimiter: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), 9000)

  def load(f: T ⇒ Unit): Future[Done] = {
    FileIO.fromPath(Paths.get(fileName))
      .via(lineDelimiter)
      .filter(_.nonEmpty)
      .mapAsync(4)(b ⇒ Future.successful(readObj(b.utf8String)))
      .collect { case Some(t) ⇒ t }
      .runWith(Sink.foreachParallel(4)(f))
  }

  private def append: Sink[String, Future[IOResult]] =
    Flow[String]
      .map(str ⇒ ByteString(s"$str$Eol"))
      .toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)

  def save(m: Iterable[T]): Future[IOResult] = {
    Source[String](m.map(toStorage)).runWith(append)
  }

}