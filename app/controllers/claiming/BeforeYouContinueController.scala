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

package controllers.claiming

import config.FrontendAppConfig
import connectors.TrustsStoreConnector
import controllers.actions._
import javax.inject.Inject
import models.TrustsStoreRequest
import pages.{IsAgentManagingTrustPage, IsClaimedPage, UtrPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{RelationshipEstablishment, RelationshipFound, RelationshipNotFound}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.claiming

import scala.concurrent.{ExecutionContext, Future}

class BeforeYouContinueController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             relationship: RelationshipEstablishment,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: claiming.BeforeYouContinueView,
                                             connector: TrustsStoreConnector
                                           )(implicit ec: ExecutionContext,
                                             config: FrontendAppConfig) extends FrontendBaseController with I18nSupport with AuthPartialFunctions {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(UtrPage) map { utr =>

        def body = {
          Future.successful(Ok(view(utr)))
        }

        relationship.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            Future.successful(Redirect(controllers.claiming.routes.IvSuccessController.onPageLoad()))
          case RelationshipNotFound =>
            body
        }

      } getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      (for {
        utr <- request.userAnswers.get(UtrPage)
        isManagedByAgent <- request.userAnswers.get(IsAgentManagingTrustPage)
      } yield {

        def onRelationshipNotFound = {

          val claimingSuccessRedirect = config.claimingSuccessUrl
          val claimingFailureRedirect = config.claimingFailureUrl

          val host = config.relationshipEstablishmentFrontendtUrl(utr)

          val queryString: Map[String, Seq[String]] = Map(
            "success" -> Seq(claimingSuccessRedirect),
            "failure" -> Seq(claimingFailureRedirect)
          )

          connector.claim(TrustsStoreRequest(request.internalId, utr, isManagedByAgent, trustLocked = false)) map { _ =>
            Redirect(host, queryString)
          }

        }

        relationship.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            Future.successful(Redirect(controllers.claiming.routes.IvSuccessController.onPageLoad()))
          case RelationshipNotFound =>
            onRelationshipNotFound
        }
      }) getOrElse Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
  }
}
