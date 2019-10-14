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

package controllers

import connectors.TaxEnrolmentsConnector
import controllers.actions._
import javax.inject.Inject
import models.TaxEnrolmentsRequest
import pages.{IsAgentManagingTrustPage, UtrPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result, Results}
import services.RelationshipEstablishment
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.IvSuccessView

import scala.concurrent.{ExecutionContext, Future}

class IvSuccessController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     val controllerComponents: MessagesControllerComponents,
                                     relationshipEstablishment: RelationshipEstablishment,
                                     taxEnrolmentsConnector: TaxEnrolmentsConnector,
                                     view: IvSuccessView
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(UtrPage).map { utr =>

        def body(enrol: Boolean): Future[Result] = {

          val isAgentManagingTrust = request.userAnswers.get(IsAgentManagingTrustPage) match {
            case None => false
            case Some(value) => value
          }

          if(enrol){
            taxEnrolmentsConnector.enrol(TaxEnrolmentsRequest(utr)) map { _ =>
              Ok(view(isAgentManagingTrust, utr))
            }
          } else {
            Future.successful(Ok(view(isAgentManagingTrust, utr)))
          }

        }
        relationshipEstablishment.check(request.internalId, utr, body(false), body(true))

      } getOrElse Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))

  }
}
