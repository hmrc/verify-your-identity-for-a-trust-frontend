/*
 * Copyright 2026 HM Revenue & Customs
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

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{AuthorisationProblemView, CouldNotConfirmIdentityView}

class FallbackFailureControllerSpec extends SpecBase {

  private def onFailureRoute = controllers.routes.FallbackFailureController.onPageLoad().url

  private def contactHelpDesk = controllers.routes.FallbackFailureController.contactHelpDesk().url

  private def couldNotConfirmIdentity = controllers.routes.FallbackFailureController.couldNotConfirmIdentity().url

  private def questionTamper = controllers.routes.FallbackFailureController.questionTamper().url

  "FallbackFailure Controller" must {

    "render internal server error view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, onFailureRoute)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "render authorisation problem view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, contactHelpDesk)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AuthorisationProblemView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(request, messages).toString

      application.stop()
    }

    "render could not confirm identity view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, couldNotConfirmIdentity)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CouldNotConfirmIdentityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(request, messages).toString

      application.stop()
    }

    "redirect to Manage a Trust page when user encountered with Question Tamper error" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, questionTamper)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.trustsContinueUrl

      application.stop()
    }

  }

}
