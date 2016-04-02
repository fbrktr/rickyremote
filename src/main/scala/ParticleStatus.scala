import java.net.InetAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ParticleStatus(brain:ActorRef) extends Actor
  with ActorLogging {

  import context.dispatcher
  val http = Http(context.system)

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val particleURL = "https://api.particle.io/v1"

  val particleToken = sys.env.get("PARTICLE_TOKEN").get
  val rickyId = sys.env.get("PARTICLE_DEVICE_ID").get

  val particleTokenSuffix = s"?access_token=$particleToken&format=raw"

  val rickyLocalIpStatusURL = s"$particleURL/devices/$rickyId/localip$particleTokenSuffix"

  var particleStatus = ""

  override def preStart() = {
    //println(context.system.settings.config.getString("particleToken"))
    updateParticleStatus()
  }

  def receive = {

    case HttpResponse(StatusCodes.OK, headers, entity, toto) =>
      val resp: Future[ByteString] = entity.withContentType(ContentTypes.`application/json`).dataBytes.runFold(ByteString(""))(_ ++ _)
      resp onComplete {
        case Success(bs) => handleParticleStatus(bs)
        case Failure(e) => println(e)
      }

    case HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)

  }

  def updateParticleStatus(): Unit ={
    http.singleRequest(HttpRequest(uri = rickyLocalIpStatusURL ))
      .pipeTo(self)
  }

  def handleParticleStatus(bs:ByteString): Unit ={
    val rickyIP = bs.decodeString(HttpCharsets.`UTF-8`.value)
    println(s"Got Ricky's local IP from Particle Cloud : $rickyIP")
    brain ! InetAddress.getByName(rickyIP)
  }

}