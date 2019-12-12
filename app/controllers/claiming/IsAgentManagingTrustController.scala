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

import controllers.actions._
import controllers.routes.SessionExpiredController
import forms.IsAgentManagingTrustFormProvider
import javax.inject.Inject
import models.Mode
import navigation.ClaimingNavigator
import pages.{IsAgentManagingTrustPage, UtrPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{RelationshipEstablishment, RelationshipFound, RelationshipNotFound}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.claiming

import scala.concurrent.{ExecutionContext, Future}

class IsAgentManagingTrustController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                navigator: ClaimingNavigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: IsAgentManagingTrustFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: claiming.IsAgentManagingTrustView,
                                                relationship: RelationshipEstablishment
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

        request.userAnswers.get(UtrPage) map { utr =>

        lazy val body = {
          val preparedForm = request.userAnswers.get(IsAgentManagingTrustPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, utr)))
        }

        relationship.check(request.internalId, utr) flatMap {
          case RelationshipFound =>
            Future.successful(Redirect(controllers.claiming.routes.IvSuccessController.onPageLoad()))
          case RelationshipNotFound =>
            body
        }

      } getOrElse Future.successful(Redirect(SessionExpiredController.onPageLoad()))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          (for {
            utr <- request.userAnswers.get(UtrPage)
          } yield {
            Future.successful(BadRequest(view(formWithErrors, mode, utr)))
          }) getOrElse Future.successful(Redirect(SessionExpiredController.onPageLoad()))
        ,
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IsAgentManagingTrustPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(IsAgentManagingTrustPage, mode, updatedAnswers))
      )
  }
}
