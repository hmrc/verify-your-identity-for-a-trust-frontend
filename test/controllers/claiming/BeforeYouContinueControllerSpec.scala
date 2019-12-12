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

import base.SpecBase
import connectors.TrustsStoreConnector
import models.TrustsStoreRequest
import navigation.{ClaimingNavigator, FakeNavigator}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito.{verify => verifyMock, _}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{IsAgentManagingTrustPage, IsClaimedPage, UtrPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeRelationshipEstablishmentService, RelationshipNotFound}
import uk.gov.hmrc.http.HttpResponse
import views.html.claiming

import scala.concurrent.Future

class BeforeYouContinueControllerSpec extends SpecBase {

  val utr = "0987654321"
  val managedByAgent = true
  val trustLocked = false

  val fakeEstablishmentServiceFailing = new FakeRelationshipEstablishmentService(RelationshipNotFound)

  "BeforeYouContinue Controller" must {

    "return OK and the correct view for a GET" in {

      val answers = emptyUserAnswers
        .set(UtrPage, utr).success.value
        .set(IsClaimedPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(answers), fakeEstablishmentServiceFailing).build()

      val request = FakeRequest(GET, controllers.claiming.routes.BeforeYouContinueController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[claiming.BeforeYouContinueView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to relationship establishment for a POST" in {

      val fakeNavigator = new FakeNavigator

      val connector = mock[TrustsStoreConnector]

      when(connector.claim(eqTo(TrustsStoreRequest(userAnswersId, utr, managedByAgent, trustLocked)))(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(CREATED)))

      val answers = emptyUserAnswers
        .set(UtrPage, "0987654321").success.value
        .set(IsAgentManagingTrustPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(answers), fakeEstablishmentServiceFailing)
        .overrides(bind[TrustsStoreConnector].toInstance(connector))
        .overrides(bind[ClaimingNavigator].toInstance(fakeNavigator))
        .build()

      val request = FakeRequest(POST, controllers.returning.routes.BeforeYouContinueController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value must include("0987654321")

      verifyMock(connector).claim(eqTo(TrustsStoreRequest(userAnswersId, utr, managedByAgent, trustLocked)))(any(), any(), any())

      application.stop()

    }
  }
}
