package com.kolade.offers

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{ActorSystem, Terminated}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.github.blemale.scaffeine.Scaffeine
import com.kolade.offers.config.AppConfig
import com.kolade.offers.model.Offer
import com.kolade.offers.repository.DefaultOfferRepository
import com.kolade.offers.route.Routes
import com.kolade.offers.service.{DefaultOfferService, OfferService}

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.language.postfixOps
import scala.util.{Failure, Success}

class Server(offerService: OfferService) {

  private val serverBinding = new AtomicReference[ServerBinding]()

  protected val systemReference = new AtomicReference[ActorSystem]()

  def startServer(host: String, port: Int, settings: ServerSettings, system: Option[ActorSystem]): Unit = {

    implicit val actorSystem: ActorSystem = system.getOrElse(ActorSystem(Logging.simpleName(this).replaceAll("\\$", "")))
    systemReference.set(actorSystem)
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(
      handler = Routes(offerService).route,
      interface = host,
      port = port,
      settings = settings)

    bindingFuture.onComplete {
      case Success(binding) =>
        serverBinding.set(binding)
        val serverAddress = binding.localAddress
        systemReference.get().log.info(s"Server online at http://${serverAddress.getHostName}:${serverAddress.getPort}/")
      case Failure(cause) =>
        systemReference.get().log.error(cause, s"Error starting the server ${cause.getMessage}")
    }

    sys.addShutdownHook {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => {
          systemReference.get().log.info("Shutting down the server")
          if (system.isEmpty)
            actorSystem.terminate()
          else
            systemReference.get().terminate()
        })
    }

    Await.ready(bindingFuture, Duration.Inf)

  }

  def terminate(): Future[Terminated] = systemReference.get.terminate()

}

object Server extends App with AppConfig {

  val actorSystem: ActorSystem = ActorSystem("offer-app")

  val cache = Scaffeine()
    .maximumSize(OfferCacheCapacity)
    .build[String, Offer]()

  val offerRepository = new DefaultOfferRepository(cache)
  val offerService = new DefaultOfferService(offerRepository)
  val server = new Server(offerService)

  server.startServer(ApiHost, ApiPort, ServerSettings(config), Option(actorSystem))

}


