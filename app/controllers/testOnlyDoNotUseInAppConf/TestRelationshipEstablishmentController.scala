/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.actions.IdentifierAction
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

/**
 * Test controller and connector to relationship-establishment to set a relationship for a given UTR.
 * This will then enable the service to "succeed" and "fail" an IV check without having to go into TrustIV.
 */
class TestRelationshipEstablishmentController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         relationshipEstablishmentConnector: RelationshipEstablishmentConnector,
                                                         identify: IdentifierAction)
                                                       (implicit ec : ExecutionContext)
  extends FrontendBaseController
    with Logging {

  def check(utr: String) = identify.async {
    implicit request =>

      logger.warn("[TestRelationshipEstablishmentController] TrustIV is using a test route, you don't want this in production.")

      val succeedRegex = "(1\\d{9})".r
      val failRegex = "(2\\d{9})".r

      utr match {
        case "4381028111" | "5000000000" =>
          establishRelationshipForUtr(request, utr)
        case succeedRegex(_) =>
          establishRelationshipForUtr(request, utr)
        case failRegex(_) =>
          Future.successful(Redirect(controllers.routes.IvFailureController.onTrustIvFailure()))
        case _ =>
          Future.successful(Redirect(controllers.routes.IvFailureController.onTrustIvFailure()))
      }
  }

  private def establishRelationshipForUtr(request: IdentifierRequest[AnyContent], utr: String)(implicit hc: HeaderCarrier) = {
    relationshipEstablishmentConnector.createRelationship(request.credentials.providerId, utr) map {
      _ =>
        Redirect(controllers.routes.IvSuccessController.onPageLoad())
    }
  }
}
