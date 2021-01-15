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
import controllers.actions._
import javax.inject.Inject
import models.NormalMode
import pages.{IsAgentManagingTrustPage, UtrPage}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{RelationshipEstablishment, RelationshipFound, RelationshipNotFound}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.Session
import views.html.{IvSuccessView, IvSuccessWithoutPlaybackView}

import scala.concurrent.{ExecutionContext, Future}

class IvSuccessController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     val controllerComponents: MessagesControllerComponents,
                                     relationshipEstablishment: RelationshipEstablishment,
                                     withPlaybackView: IvSuccessView,
                                     withoutPlaybackView: IvSuccessWithoutPlaybackView
                                   )(implicit ec: ExecutionContext,
                                     val config: FrontendAppConfig)
  extends FrontendBaseController
    with I18nSupport
    with AuthPartialFunctions {

  private val logger = Logger(getClass)

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(UtrPage).map { utr =>

        def onRelationshipFound = {

            val isAgentManagingTrust = request.userAnswers.get(IsAgentManagingTrustPage) match {
              case None => false
              case Some(value) => value
            }

            logger.info(s"[Verifying][Session ID: ${Session.id(hc)}] user successfully passed Trust IV questions for utr $utr, user can contiue to maintain the trust")

            if (config.playbackEnabled) {
              Future.successful(Ok(withPlaybackView(isAgentManagingTrust, utr)))
            } else {
              Future.successful(Ok(withoutPlaybackView(isAgentManagingTrust, utr)))
            }
        }

        lazy val onRelationshipNotFound = Future.successful(Redirect(controllers.routes.IsAgentManagingTrustController.onPageLoad(NormalMode)))

        relationshipEstablishment.check(request.internalId, utr) flatMap {
          case RelationshipFound => {
            onRelationshipFound
          }
          case RelationshipNotFound => {
            logger.warn(s"[Verifying][Session ID: ${Session.id(hc)}] no relationship found in Trust IV, cannot continue with maintaing the trust, sending user back to the start of Trust IV")
            onRelationshipNotFound
          }
        }
        
      } getOrElse {
        logger.warn(s"[Verifying][Session ID: ${Session.id(hc)}] no utr found in user answers, unable to continue with verifying the user")
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }

  }
}
