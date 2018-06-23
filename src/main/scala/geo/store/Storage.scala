package geo.store

import java.nio.file.StandardOpenOption.{APPEND, WRITE}
import java.nio.file.{OpenOption, Paths}
import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import geo.Message
import geo.util.{BaseParsers, Transformer}

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

class Storage[T <: Message, I](fileName: String,
                               chunkSize: Int,
                               lastIndex: Int,
                               calculateIndex: I ⇒ Int)(implicit materializer: Materializer, dispatcher: ExecutionContext) {
  self: Transformer[T] with BaseParsers[T] ⇒

  val lineDelimiter: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), 9000)
  val currentLastIndex = new AtomicInteger(lastIndex)

  private def append(options: Set[OpenOption], position: Long): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(str ⇒ ByteString(s"$str$Eol"))
      .toMat(FileIO.toPath(Paths.get(fileName), options, position))(Keep.right)

  private def updateRow(index: Long, objStr: String): Future[IOResult] = {
    Source[String](Seq(objStr)).runWith(append(Set(WRITE), index * chunkSize))
  }

  def insertRow(f: Int ⇒ T): Future[IOResult] = {
    val id = currentLastIndex.getAndIncrement()
    Source[String](Seq(toStorage(f(id)))).runWith(append(Set(WRITE), id))
  }

  def readRow(index: I): Future[Option[T]] =
    FileIO.fromPath(Paths.get(fileName), chunkSize, calculateIndex(index) * chunkSize)
      .runWith(Sink.headOption)
      .map(_.flatMap(bString ⇒ readObj(bString.utf8String)))

  def deleteRow(index: I): Future[IOResult] =
    updateRow(calculateIndex(index), empty)

  def updateRow(index: I, obj: T): Future[IOResult] = {
    updateRow(calculateIndex(index), toStorage(obj))
  }

  def searchByValue(f: T ⇒ Boolean): Future[Int] =
    FileIO.fromPath(Paths.get(fileName))
      .via(lineDelimiter)
      .mapAsync(4)(b ⇒ Future(readObj(b.utf8String)))
      .filter(_.forall(f))
      .runWith(Sink.foldAsync(0) { case (acc, _) ⇒ Future(acc + 1) })

}
