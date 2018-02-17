package onextent.supersix.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.kafka.ProducerSettings
import akka.pattern.AskTimeoutException
import akka.serialization.SerializationExtension
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.ExecutionContextExecutor

object Conf extends Conf with LazyLogging {

  implicit val actorSystem: ActorSystem = ActorSystem(appName, conf)
  SerializationExtension(actorSystem)

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  val decider: Supervision.Decider = {

    case _: AskTimeoutException =>
      // might want to try harder, retry w/backoff if the actor is really supposed to be there
      logger.warn(s"decider discarding message to resume processing")
      Supervision.Resume

    case e: java.text.ParseException =>
      logger.warn(
        s"decider discarding unparseable message to resume processing: $e")
      Supervision.Resume

    case e: Throwable =>
      logger.error(s"decider can not decide: $e", e)
      Supervision.Stop

    case e =>
      logger.error(s"decider can not decide: $e")
      Supervision.Stop

  }

  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))

  val producerSettings: ProducerSettings[Array[Byte], String] =
    ProducerSettings(actorSystem, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(bootstrap)

  import scala.collection.JavaConverters._
  val corsOriginList: List[HttpOrigin] = conf
    .getStringList("main.corsOrigin")
    .asScala
    .iterator
    .toList
    .map(origin => HttpOrigin(origin))
  val urlpath: String = conf.getString("main.path")
  val port: Int = conf.getInt("main.port")
}

trait Conf {

  val conf: Config = ConfigFactory.load()

  val appName: String = conf.getString("main.appName")

  val bootstrap: String = conf.getString("kafka.bootstrap")

  val observationsTopic: String = conf.getString("kafka.topics.observations")

}
