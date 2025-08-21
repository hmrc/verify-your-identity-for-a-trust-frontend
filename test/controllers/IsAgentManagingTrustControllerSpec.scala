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
import forms.IsAgentManagingTrustFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.{verify, when}
import pages.{IdentifierPage, IsAgentManagingTrustPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{FakeRelationshipEstablishmentService, RelationshipFound, RelationshipNotFound}
import views.html.IsAgentManagingTrustView

import scala.concurrent.Future

class IsAgentManagingTrustControllerSpec extends SpecBase {

  val formProvider = new IsAgentManagingTrustFormProvider()
  val form: Form[Boolean] = formProvider()
  val utr = "0987654321"

  lazy val isAgentManagingTrustRoute: String = controllers.routes.IsAgentManagingTrustController.onPageLoad().url

  val fakeEstablishmentServiceFailing = new FakeRelationshipEstablishmentService(RelationshipNotFound)
  val fakeEstablishmentServiceNotFound = new FakeRelationshipEstablishmentService(RelationshipNotFound)

  val fakeEstablishmentServiceFound = new FakeRelationshipEstablishmentService(RelationshipFound)
  "IsAgentManagingTrust Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(IdentifierPage, utr)
        .success
        .value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFailing).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[IsAgentManagingTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, utr)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, true)
        .success.value
        .set(IdentifierPage, utr)
        .success.value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFailing).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)

      val view = application.injector.instanceOf[IsAgentManagingTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), utr)(request, messages).toString

      application.stop()
    }

    "redirect to IvSuccess on GET when relationship is already found" in {
      val userAnswers = emptyUserAnswers
        .set(IdentifierPage, utr).success.value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFound
      ).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IvSuccessController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if identifier is missing" in {
      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFound
      ).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = Mockito.mock(classOf[SessionRepository])
      val fakeNavigator = new FakeNavigator()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), relationshipEstablishment = fakeEstablishmentServiceFailing)
          .overrides(
            bind[Navigator].toInstance(fakeNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, isAgentManagingTrustRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }
    "redirect to the next page when valid data is submitted (false) and persist false" in {
      val mockSessionRepository = Mockito.mock(classOf[SessionRepository])
      val fakeNavigator = new FakeNavigator()

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers.set(IdentifierPage, utr).success.value),
          relationshipEstablishment = fakeEstablishmentServiceNotFound
        ).overrides(
          bind[Navigator].toInstance(fakeNavigator),
          bind[SessionRepository].toInstance(mockSessionRepository)
        ).build()

      val request =
        FakeRequest(POST, isAgentManagingTrustRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(captor.capture())
      captor.getValue.get(IsAgentManagingTrustPage).value mustBe false

      application.stop()
    }
    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(IdentifierPage, utr)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers),
        relationshipEstablishment = fakeEstablishmentServiceFailing)
        .build()

      val request =
        FakeRequest(POST, isAgentManagingTrustRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[IsAgentManagingTrustView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, utr)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing).build()

      val request = FakeRequest(GET, isAgentManagingTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing).build()

      val request =
        FakeRequest(POST, isAgentManagingTrustRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
