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

import base.SpecBase
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.IdentifierPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{FakeRelationshipEstablishmentService, RelationshipNotFound}

import scala.concurrent.Future

class SaveIdentifierControllerSpec extends SpecBase {

  val utr = "0987654321"
  val urn = "ABTRUST12345678"

  val fakeEstablishmentServiceFailing = new FakeRelationshipEstablishmentService(RelationshipNotFound)

  "SaveIdentifierController" when {

    "invalid identifier provided" must {

      "render an error page" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        val request = FakeRequest(GET, routes.SaveIdentifierController.save("123").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe routes.FallbackFailureController.onPageLoad().url
      }

    }

    "utr provided" must {

      "send UTR to session repo" when {

        "user answers does not exist" in {

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(captor.capture()))
            .thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          val request = FakeRequest(GET, controllers.routes.SaveIdentifierController.save(utr).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.IsAgentManagingTrustController.onPageLoad().url

          captor.getValue.get(IdentifierPage).value mustBe utr

        }

        "user answers exists" in {

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(captor.capture()))
            .thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), fakeEstablishmentServiceFailing)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          val request = FakeRequest(GET, controllers.routes.SaveIdentifierController.save(utr).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.IsAgentManagingTrustController.onPageLoad().url

          captor.getValue.get(IdentifierPage).value mustBe utr

        }
      }
    }

    "urn provided" must {

      "send URN to session repo" when {

        "user answers does not exist" in {

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(captor.capture()))
            .thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = None, fakeEstablishmentServiceFailing)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          val request = FakeRequest(GET, controllers.routes.SaveIdentifierController.save(urn).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.IsAgentManagingTrustController.onPageLoad().url

          captor.getValue.get(IdentifierPage).value mustBe urn

        }

        "user answers exists" in {

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(captor.capture()))
            .thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), fakeEstablishmentServiceFailing)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          val request = FakeRequest(GET, controllers.routes.SaveIdentifierController.save(urn).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.IsAgentManagingTrustController.onPageLoad().url

          captor.getValue.get(IdentifierPage).value mustBe urn

        }
      }
    }

  }

}
