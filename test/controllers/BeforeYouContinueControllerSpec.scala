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

package controllers

import base.SpecBase
import connectors.TrustsStoreConnector
import models.{TrustsStoreRequest, UserAnswersCached}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.mockito.Mockito.{verify => verifyMock, _}
import pages.{IdentifierPage, IsAgentManagingTrustPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeRelationshipEstablishmentService, RelationshipNotFound}
import views.html.BeforeYouContinueView

import scala.concurrent.Future

class BeforeYouContinueControllerSpec extends SpecBase {

  val utr = "0987654321"
  val managedByAgent = true
  val trustLocked = false

  val fakeEstablishmentServiceFailing = new FakeRelationshipEstablishmentService(RelationshipNotFound)

  "BeforeYouContinue Controller" must {

    "return OK and the correct view for a GET" in {

      val answers = emptyUserAnswers
        .set(IdentifierPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(answers), fakeEstablishmentServiceFailing).build()

      val request = FakeRequest(GET, controllers.routes.BeforeYouContinueController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BeforeYouContinueView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr)(request, messages).toString

      application.stop()
    }

    "redirect to relationship establishment for a POST" in {

      val fakeNavigator = new FakeNavigator(Call("GET", "/foo"))

      val connector = Mockito.mock(classOf[TrustsStoreConnector])

      when(connector.claim(eqTo(TrustsStoreRequest(userAnswersId, utr, managedByAgent, trustLocked)))(any(), any(), any()))
        .thenReturn(Future.successful(UserAnswersCached))

      val answers = emptyUserAnswers
        .set(IdentifierPage, "0987654321").success.value
        .set(IsAgentManagingTrustPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(answers), fakeEstablishmentServiceFailing)
        .overrides(bind[TrustsStoreConnector].toInstance(connector))
        .overrides(bind[Navigator].toInstance(fakeNavigator))
        .build()

      val request = FakeRequest(POST, controllers.routes.BeforeYouContinueController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value must include("0987654321")

      verifyMock(connector).claim(eqTo(TrustsStoreRequest(userAnswersId, utr, managedByAgent, trustLocked)))(any(), any(), any())

      application.stop()

    }
  }
}
