package ricky.io

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, UdpConnected}
import akka.util.ByteString

/**
  * Created by fbrktr on 02/04/2016.
  */

class RickyRemoteController(brain: ActorRef) extends Actor {

  import context.system

  def receive = {
    case remote: InetAddress =>
      println(s"Opening connection to Ricky @ $remote")
      IO(UdpConnected) ! UdpConnected.Connect(self, new InetSocketAddress(remote,9999))
    case UdpConnected.Connected =>
      println(s"Opened connection to Ricky")
      context.become(ready(sender()))
      brain ! RickyStatus("RickyReady")
  }

  def ready(connection: ActorRef): Receive = {
    case msg: String =>
      println(s"--> $msg")
      connection ! UdpConnected.Send(ByteString(msg+'\0'))
    case UdpConnected.Disconnect =>
      println("Disconnect from Ricky")
      connection ! UdpConnected.Disconnect
    case UdpConnected.Disconnected =>
      println("Disconnected from Ricky")
      context.stop(self)
  }

}