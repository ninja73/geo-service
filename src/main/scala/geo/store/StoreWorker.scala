package geo.store

import java.nio.file.StandardOpenOption.{APPEND, WRITE}
import java.nio.file.{OpenOption, Paths}

import akka.NotUsed
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import geo.Message
import geo.util.Transformer

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

class StoreWorker[T <: Message : Transformer, I](fileName: String, chunkSize: Int, calculateIndex: I ⇒ Int)
                                             (implicit materializer: Materializer, dispatcher: ExecutionContext) {

  val Eol = "\n"
  val lineDelimiter: Flow[ByteString, ByteString, NotUsed] = Framing.delimiter(ByteString("\n"), chunkSize, allowTruncation = true)

  val transformer: Transformer[T] = implicitly[Transformer[T]]

  private def append(options: Set[OpenOption], position: Long): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(str ⇒ ByteString(s"$str$Eol"))
      .toMat(FileIO.toPath(Paths.get(fileName), options, position))(Keep.right)

  private def updateRow(index: Long, objStr: String)
                       (implicit materializer: Materializer, dispatcher: ExecutionContext): Future[IOResult] = {
    Source[String](Seq(objStr)).runWith(append(Set(WRITE), index * chunkSize))
  }

  def writeRow(obj: T): Future[IOResult] =
    Source[String](Seq(transformer.storeFormat(obj))).runWith(append(Set(WRITE, APPEND), 0))

  def readRow[R](index: I)(f: Option[T] ⇒ R): Future[Option[R]] =
    FileIO.fromPath(Paths.get(fileName), chunkSize, calculateIndex(index) * chunkSize)
      .runWith(Sink.headOption)
      .map(_.map(bString ⇒ f(transformer.readToObj(bString.utf8String))))

  def deleteRow(index: I)
               (implicit materializer: Materializer, dispatcher: ExecutionContext): Future[IOResult] =
    updateRow(calculateIndex(index), transformer.empty)

  def updateRow(index: I, obj: T)
               (implicit materializer: Materializer, dispatcher: ExecutionContext): Future[IOResult] = {
    updateRow(calculateIndex(index), transformer.storeFormat(obj))
  }

}
