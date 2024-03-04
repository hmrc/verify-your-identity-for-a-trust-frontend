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
import models.UserAnswers
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito.{verify => verifyMock, _}
import org.scalatest.BeforeAndAfterAll
import org.mockito.MockitoSugar.mock
import pages.{IdentifierPage, IsAgentManagingTrustPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{RelationshipEstablishment, RelationshipFound}
import views.html.IvSuccessView

import scala.concurrent.Future

class IvSuccessControllerSpec extends SpecBase with BeforeAndAfterAll {

  private val utr = "0987654321"

  private val mockRelationshipEstablishment = mock[RelationshipEstablishment]

  "Returning IvSuccess Controller" must {

    "return OK and the correct view for a GET with no Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, false).success.value
        .set(IdentifierPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
        .build()

      val request = FakeRequest(GET, controllers.routes.IvSuccessController.onPageLoad().url)

      val view = application.injector.instanceOf[IvSuccessView]

      val viewAsString = view(isAgent = false, utr)(request, messages).toString

      when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
        .thenReturn(Future.successful(RelationshipFound))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

      reset(mockRelationshipEstablishment)

      application.stop()

    }

    "return OK and the correct view for a GET with Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, true).success.value
        .set(IdentifierPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
        .build()

      val request = FakeRequest(GET, controllers.routes.IvSuccessController.onPageLoad().url)

      val view = application.injector.instanceOf[IvSuccessView]

      val viewAsString = view(isAgent = true, utr)(request, messages).toString

      when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
        .thenReturn(Future.successful(RelationshipFound))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

      reset(mockRelationshipEstablishment)

      application.stop()

    }

    "redirect to Session Expired" when {

      "no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, controllers.routes.IvSuccessController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()

      }
    }

    "redirect to next page for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(POST, controllers.routes.IvSuccessController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.trustsContinueUrl

      application.stop()

    }

  }
}
