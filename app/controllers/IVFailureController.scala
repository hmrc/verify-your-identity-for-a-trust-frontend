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

import controllers.actions.IdentifierAction
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.UnauthorisedView

import scala.concurrent.Future

class IVFailureController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        unauthorisedView: UnauthorisedView,
                                        identify: IdentifierAction
                                      ) extends FrontendBaseController with I18nSupport {

  def onTrustIVFailure: Action[AnyContent] = identify.async {
    implicit request =>
      println(s"calling on trust iv failure")
      Future.successful(Redirect(routes.IVFailureController.trustLocked()))
  }

  def trustLocked : Action[AnyContent] = identify.async {
    implicit request =>
      println(s"calling trust locked")
      Future.successful(Ok(unauthorisedView()))
  }
}
