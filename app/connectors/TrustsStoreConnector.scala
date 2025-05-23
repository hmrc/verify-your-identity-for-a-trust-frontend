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
import models.{TrustsStoreRequest, TrustsStoreResponse}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

class TrustsStoreConnector @Inject()(http: HttpClientV2, config: FrontendAppConfig) {

  def claim(request: TrustsStoreRequest)(implicit hc: HeaderCarrier,
                                         ec: ExecutionContext,
                                         writes: Writes[TrustsStoreRequest]): Future[TrustsStoreResponse] = {
    val url: String = s"${config.trustsStoreUrl}/trusts-store/claim"
    http.post(url"$url")
    .withBody(Json.toJson(request))
    .execute[TrustsStoreResponse]
  }

}
