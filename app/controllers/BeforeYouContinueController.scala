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

package controllers

import config.FrontendAppConfig
import connectors.TrustsStoreConnector
import controllers.actions._
import javax.inject.Inject
import models.TrustsStoreRequest
import pages.{IsAgentManagingTrustPage, IdentifierPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{RelationshipEstablishment, RelationshipFound, RelationshipNotFound}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
import views.html.BeforeYouContinueView

import scala.concurrent.{ExecutionContext, Future}

class BeforeYouContinueController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       relationship: RelationshipEstablishment,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: BeforeYouContinueView,
                                       connector: TrustsStoreConnector
                                     )(implicit ec: ExecutionContext,
                                       config: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport
    with AuthPartialFunctions
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

        request.userAnswers.get(IdentifierPage) map { utr =>

        def body = {
            Future.successful(Ok(view(utr)))
        }

        relationship.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            logger.info(s"[Verifying][Session ID: ${Session.id(hc)}]" +
              s" relationship is already established in IV for utr $utr sending user to successfully verified")

            Future.successful(Redirect(controllers.routes.IvSuccessController.onPageLoad()))
          case RelationshipNotFound =>
            body
        }

      } getOrElse {
          logger.error(s"[Verifying][Session ID: ${Session.id(hc)}]" +
            s" no utr available in user answers, cannot continue with verifying the user")

          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      (for {
        utr <- request.userAnswers.get(IdentifierPage)
        isManagedByAgent <- request.userAnswers.get(IsAgentManagingTrustPage)
      } yield {

        def onRelationshipNotFound =  {

          val returningSuccessRedirect = config.relationshipEstablishmentSuccessUrl
          val returningFailureRedirect = config.relationshipEstablishmentFailureUrl

          val host = config.relationshipEstablishmentFrontendtUrl(utr)

          val queryString: Map[String, Seq[String]] = Map(
            "success" -> Seq(returningSuccessRedirect),
            "failure" -> Seq(returningFailureRedirect)
          )

          connector.claim(TrustsStoreRequest(request.internalId, utr, isManagedByAgent, trustLocked = false)) map { _ =>
            logger.info(s"[Verifying][Session ID: ${Session.id(hc)}]" +
              s" saved users utr $utr in trusts-store so they can be identified when they return from Trust IV." +
              s" Sending the user into Trust IV to answer questions")

            Redirect(host, queryString)
          }

        }

        relationship.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            logger.info(s"[Verifying][Session ID: ${Session.id(hc)}]" +
              s" relationship is already established in IV for utr $utr sending user to successfully verified")

            Future.successful(Redirect(controllers.routes.IvSuccessController.onPageLoad()))
          case RelationshipNotFound =>
            onRelationshipNotFound
        }
      }) getOrElse {
        logger.error(s"[Verifying][Session ID: ${Session.id(hc)}]" +
          s" no utr available in user answers, cannot continue with verifying the user")

        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
  }
}
