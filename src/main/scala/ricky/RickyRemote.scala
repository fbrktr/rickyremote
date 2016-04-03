package ricky

import java.net.InetAddress

import akka.actor.{Actor, ActorSystem, Props}
import processing.core.PApplet
import ricky.io.{RickyListener, RickyMessage, RickyRemoteController, RickyStatus}
import ricky.particle.ParticleStatus
import ricky.ui.{MoveRequest, RickyState}

/**
  * Created by fbrktr on 02/04/2016.
  */

object RickyRemote {

  val system: ActorSystem = ActorSystem("RickySystem")

  val brain = system.actorOf(Props(new Brain()), name = "brain")

  val statusChecker = system.actorOf(Props(new ParticleStatus(brain)), name = "statusChecker")
  val rickyListener = system.actorOf(Props(new RickyListener(brain, 9999)), name = "rickyListener")
  val rickyRemoteController = system.actorOf(Props(new RickyRemoteController(brain)), name = "rickyRemote")

  def main(args:Array[String]):Unit = {
    PApplet.main( Array("ricky.ui.RickyUI"))
  }

  class Brain() extends Actor {

    def receive = {
      case rickyIP: InetAddress => rickyRemoteController ! rickyIP
      case RickyStatus(status) =>
        println("Link to Ricky up")
        context.become(ready())
        rickyRemoteController ! "Hey !"
    }

    def ready(): Receive = {
      case RickyMessage("Yo!") => RickyState.heartBeat()
      case RickyMessage(msg) => println(s"[Ricky] $msg")
      case MoveRequest(dir) => rickyRemoteController ! dir
      case msg:String => println(msg)
    }
  }
}
