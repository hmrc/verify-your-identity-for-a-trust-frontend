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

package controllers.testOnlyDoNotUseInAppConf

import com.google.inject.Inject
import config.FrontendAppConfig
import models.IsUTR
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

class RelationshipEstablishmentConnector @Inject()(val httpClient: HttpClientV2,
                                                   config: FrontendAppConfig)
                                                  (implicit val ec: ExecutionContext) {

  import RelationshipHttpReads.httpReads

  private val relationshipEstablishmentPostUrl: String = s"${config.relationshipEstablishmentBaseUrl}/relationship-establishment/relationship/"

  private def newRelationship(credId: String, identifier: String): Relationship = {
    if(IsUTR(identifier)) {
      Relationship(config.relationshipName, Set(BusinessKey(config.relationshipTaxableIdentifier, identifier)), credId)
    } else {
      Relationship(config.relationshipName, Set(BusinessKey(config.relationshipNonTaxableIdentifier, identifier)), credId)
    }
  }

  def createRelationship(credId: String, utr: String)(implicit headerCarrier: HeaderCarrier): Future[RelationshipResponse] = {
    val ttl = config.relationshipTTL
    val relationshipJson = RelationshipJson(newRelationship(credId, utr), ttlSeconds = ttl)

    httpClient.post(url"$relationshipEstablishmentPostUrl")
      .withBody(Json.toJson(relationshipJson))
      .execute[RelationshipResponse]
  }

}
