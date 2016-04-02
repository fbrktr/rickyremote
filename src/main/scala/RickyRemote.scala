import java.net.InetAddress

import akka.actor.{Actor, Props, ActorSystem}
import io.{RickyMessage, RickyStatus, RickyListener, RickyRemoteController}

/**
  * Created by fbrktr on 02/04/2016.
  */

object RickyRemote {


  val system: ActorSystem = ActorSystem("RickySystem")

  val brain = system.actorOf(Props(new Brain()), name = "brain")

  val statusChecker = system.actorOf(Props(new ParticleStatus(brain)), name = "statusChecker")
  val rickyListener = system.actorOf(Props(new RickyListener(brain, 9999)), name = "rickyListener")
  val rickyRemote = system.actorOf(Props(new RickyRemoteController(brain)), name = "rickyRemote")

  def main(args:Array[String]):Unit = {


  }

  class Brain() extends Actor {

    def receive = {
      case rickyIP: InetAddress => rickyRemote ! rickyIP
      case RickyStatus(status) =>
        println("Link to Ricky up")
        context.become(ready())
        rickyRemote ! "Hey !"
    }

    def ready(): Receive = {
      case RickyMessage(msg) => println(s"[Ricky] $msg")
    }
  }
}
