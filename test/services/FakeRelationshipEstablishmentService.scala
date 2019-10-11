/*
 * Copyright 2019 HM Revenue & Customs
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

package services

import controllers.actions.FakeAuthConnector
import play.api.mvc.{AnyContent, Request, Result, Results}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class FakeRelationshipEstablishmentService extends RelationshipEstablishment {

  val success = Future.successful(Results.Ok)

  override def authConnector: AuthConnector = new FakeAuthConnector(Future.successful())

  override def check(internalId: String, utr: String, success: Future[Result], failure: Future[Result])
                    (implicit request: Request[AnyContent]): Future[Result] = success

}
