/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import config.FrontendAppConfig

import javax.inject.Inject
import models.RelationshipEstablishmentStatus.RelationshipEstablishmentStatus
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class RelationshipEstablishmentConnector @Inject()(http: HttpClientV2, config: FrontendAppConfig) {

  def journeyId(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RelationshipEstablishmentStatus] = {
    val url = s"${config.relationshipEstablishmentUrl}/relationship-establishment/journey-failure/$id"
    http.get(url"$url")
      .execute[RelationshipEstablishmentStatus]
  }
}
