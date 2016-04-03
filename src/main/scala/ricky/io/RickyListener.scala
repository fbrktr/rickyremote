package ricky.io

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}

/**
  * Created by fbrktr on 02/04/2016.
  */

class RickyListener(brain:ActorRef, port:Int) extends Actor {

  import context.system
  val localAddress = InetAddress.getLocalHost.getHostAddress

  println(s"Binding to $localAddress:$port")
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(localAddress, port))

  def receive = {
    case Udp.Bound(local) =>
      println(s"Listening to $localAddress:$port")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    //case msg:String => socket ! Udp.Send(ByteString(msg,"UTF-8"), rickySocket)
    case Udp.Received(data, remote) =>
      //println(data)
      //socket ! Udp.Send(ByteString("Bisou\n","UTF-8"), remote)
      brain ! new RickyMessage(data.decodeString("UTF-8"))
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }

}
