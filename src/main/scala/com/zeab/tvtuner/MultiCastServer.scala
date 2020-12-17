package com.zeab.tvtuner

import java.net.{DatagramPacket, InetAddress, InetSocketAddress, MulticastSocket, ServerSocket}

import akka.actor.Actor

import scala.util.{Failure, Success, Try}

class MultiCastServer extends Actor{

  def receive: Receive = disconnected

  def disconnected: Receive = {
    case Connect =>
      println("attempting to connect multicast")
      val socket: MulticastSocket = MultiCastServer.create()
      context.become(connected(socket))
      self ! CheckMessages
  }

  def connected(socket: MulticastSocket): Receive = {
    case CheckMessages =>
      val buffer: Array[Byte] = Array.ofDim[Byte](1024)
      val data: DatagramPacket = new DatagramPacket(buffer, buffer.length)
      socket.receive(data)
      Try {
        socket.receive(data)
        new String(data.getData).trim
      } match {
        case Failure(exception: Throwable) => println(exception.toString)
        case Success(datagram: String) =>
          //println(datagram)
          self ! CheckMessages
      }
  }

  override def preStart(): Unit = {
    self ! Connect
  }

  case object Connect

  case object CheckMessages

}

object MultiCastServer {

  def create(host: String = "239.255.255.250", port: Int = 1900): MulticastSocket ={
    val socket: MulticastSocket = new MulticastSocket(port)
    val multicastAddressGroup: InetAddress = InetAddress.getByName(host)
    socket.joinGroup(multicastAddressGroup)
    val serverSocket: ServerSocket = new ServerSocket()
    serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost, 0))
    socket
  }

}
