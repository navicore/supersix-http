package onextent.supersix.http.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.http.http.functions.{HttpSupport, KafkaProducerDirective}
import onextent.supersix.http.models.functions.JsonSupport
import onextent.supersix.http.models.{MkClick, ObservationRequest}
import spray.json._
import onextent.supersix.http.Conf._

object ObservationRoute
    extends JsonSupport
    with LazyLogging
    with Directives
    with KafkaProducerDirective
    with HttpSupport {

  def apply(): Route =
    path(urlpath / "observation") {
      post {
        decodeRequest {
          entity(as[ObservationRequest]) { clickReq =>
            val click = MkClick(clickReq)
            write(click.toJson.compactPrint, click.who.toString) { f =>
              onSuccess(f) {
                complete(StatusCodes.Accepted)
              }
            }
          }
        }
      }
    }

}
