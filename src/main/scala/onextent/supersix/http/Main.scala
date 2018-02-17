package onextent.supersix.http

import Conf._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.http.http.ObservationRoute
import onextent.supersix.http.http.functions.HttpSupport

object Main extends App with LazyLogging with HttpSupport with Directives {

  val route: Route =
    HealthCheck ~
      logRequest(urlpath) {
        handleErrors {
          cors(corsSettings) {
            ObservationRoute()
          }
        }
      }

  Http().bindAndHandle(route, "0.0.0.0", port)

}
