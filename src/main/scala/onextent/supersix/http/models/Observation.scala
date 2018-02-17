package onextent.supersix.http.models

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID

final case class Observation(kind: String,
                             who: UUID,
                             value: String,
                             datetime: ZonedDateTime =
                               ZonedDateTime.now(ZoneOffset.UTC),
                             id: UUID = UUID.randomUUID())

object MkClick {

  def apply(req: ObservationRequest): Observation = {
    Observation(req.kind, req.who, req.value)
  }

}

final case class ObservationRequest(kind: String, who: UUID, value: String)
