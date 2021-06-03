package me.zeab.tvtuner.features.video

import akka.Done
import akka.actor.Actor
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import me.zeab.tvtuner.shared.Get

import java.io.File
import java.util.UUID
import scala.concurrent.ExecutionContext

//so this is the main feed per channel
//this is where we update

class ChannelFeed(mode: String)(implicit mat: Materializer) extends Actor {

  implicit val ec: ExecutionContext = context.system.dispatcher

  //based on the channel choose while files to use
  val filesToUse: String = {
    if (mode == "test") "C:\\Users\\pyros\\Desktop\\archive pictures\\tina.test.pattern.ts"
    else if (mode == "1") "C:\\Users\\pyros\\Desktop\\archive pictures\\tina.test.pattern.ts"
    else "C:\\Users\\pyros\\Desktop\\archive pictures\\tina.test.pattern.ts"
  }

  //when im asked for the current byte string i should give it back
  //i also need to set an expiration for the current set and drop it
  def receive: Receive = feed(List.empty)

  def feed(byteStrings: Seq[ByteString]): Receive = {
    case byteString: ByteString =>
      context.become(feed(byteStrings ++ Seq(byteString)))
    case Done => println("done")
    case Get =>
      sender() ! byteStrings
  }

  override def preStart(): Unit = {
    //pick a random file from the above list and then open it
    //then set a scheduler to pick another random file from the list and then load it in
    FileIO
      .fromPath(new File(filesToUse).toPath)
      .runForeach { byteString: ByteString => self ! byteString }
      .onComplete(_ => self ! Done)
  }

}
