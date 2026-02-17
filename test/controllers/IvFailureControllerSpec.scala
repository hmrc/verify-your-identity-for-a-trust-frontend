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
import connectors.{RelationshipEstablishmentConnector, TrustsStoreConnector}
import models.RelationshipEstablishmentStatus.{UnsupportedRelationshipStatus, UpstreamRelationshipError}
import models.{RelationshipEstablishmentStatus, TrustsStoreRequest, UserAnswersCached}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{verify => verifyMock, when}
import pages.{IdentifierPage, IsAgentManagingTrustPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class IvFailureControllerSpec extends SpecBase {

  lazy val connector: RelationshipEstablishmentConnector = Mockito.mock(classOf[RelationshipEstablishmentConnector])

  private val fakeNavigator = new FakeNavigator(Call("GET", "/foo"))

  private val agentNotManagingTrustsAnswers = emptyUserAnswers
    .set(IdentifierPage, "1234567890")
    .success
    .value
    .set(IsAgentManagingTrustPage, false)
    .success
    .value

  private val agentManagingTrustsAnswers = emptyUserAnswers
    .set(IdentifierPage, "1234567890")
    .success
    .value
    .set(IsAgentManagingTrustPage, true)
    .success
    .value

  private lazy val onIvFailureRoute = controllers.routes.IvFailureController.onTrustIvFailure().url

  private val journeyId = "journeyId=47a8a543-6961-4221-86e8-d22e2c3c91de"

  private lazy val request = FakeRequest(GET, s"$onIvFailureRoute?$journeyId")

  "IvFailure Controller" must {

    "callback-failure route" when {

      "redirect to IV FallbackFailure when no journeyId is provided" in {

        val application = applicationBuilder(userAnswers = Some(agentManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector),
            bind[Navigator].toInstance(fakeNavigator)
          )
          .build()

        val request = FakeRequest(GET, s"$onIvFailureRoute")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.FallbackFailureController.onPageLoad().url

        application.stop()
      }

      "redirect to trust locked page when user fails Trusts IV after multiple attempts" in {

        val application = applicationBuilder(userAnswers = Some(agentManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector),
            bind[Navigator].toInstance(fakeNavigator)
          )
          .build()

        when(connector.journeyId(any[String])(any(), any()))
          .thenReturn(Future.successful(RelationshipEstablishmentStatus.Locked))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.IvFailureController.trustLocked().url

        application.stop()
      }

      "redirect to trust utr not found page when the utr isn't found" in {

        val application = applicationBuilder(userAnswers = Some(agentManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector)
          )
          .build()

        when(connector.journeyId(any[String])(any(), any()))
          .thenReturn(Future.successful(RelationshipEstablishmentStatus.NotFound))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.IvFailureController.trustNotFound().url

        application.stop()
      }

      "redirect to trust utr in processing page when the utr is processing" in {
        val application = applicationBuilder(userAnswers = Some(agentManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector)
          )
          .build()

        when(connector.journeyId(any[String])(any(), any()))
          .thenReturn(Future.successful(RelationshipEstablishmentStatus.InProcessing))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.IvFailureController.trustStillProcessing().url

        application.stop()
      }

      "redirect to authorisation problem page when user data is empty" in {

        val application = applicationBuilder(userAnswers = Some(agentNotManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector),
            bind[Navigator].toInstance(fakeNavigator)
          )
          .build()

        when(connector.journeyId(any[String])(any(), any()))
          .thenReturn(Future.successful(RelationshipEstablishmentStatus.NotMatchAnswer))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.FallbackFailureController.contactHelpDesk().url

        application.stop()
      }

      "redirect to IV FallbackFailure given UnsupportedRelationshipStatus response" in {

        val application = applicationBuilder(userAnswers = Some(agentNotManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector),
            bind[Navigator].toInstance(fakeNavigator)
          )
          .build()

        when(connector.journeyId(any[String])(any(), any()))
          .thenReturn(Future.successful(UnsupportedRelationshipStatus("reason")))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.FallbackFailureController.onPageLoad().url

        application.stop()
      }

      "redirect to IV FallbackFailure given UpstreamRelationshipError response" in {

        val application = applicationBuilder(userAnswers = Some(agentNotManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector),
            bind[Navigator].toInstance(fakeNavigator)
          )
          .build()

        when(connector.journeyId(any[String])(any(), any()))
          .thenReturn(Future.successful(UpstreamRelationshipError("reason")))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.FallbackFailureController.onPageLoad().url

        application.stop()
      }

      "redirect to could not confirm identity page given NotEnoughQuestions response" in {

        val application = applicationBuilder(userAnswers = Some(agentNotManagingTrustsAnswers))
          .overrides(
            bind[RelationshipEstablishmentConnector].toInstance(connector),
            bind[Navigator].toInstance(fakeNavigator)
          )
          .build()

        when(connector.journeyId(any[String])(any(), any()))
          .thenReturn(Future.successful(RelationshipEstablishmentStatus.NotEnoughQuestions))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.FallbackFailureController
          .couldNotConfirmIdentity()
          .url

        application.stop()
      }
    }

    "locked route" when {

      "return OK and the correct view for a GET for locked route" in {

        val utr            = "3000000001"
        val managedByAgent = true
        val trustLocked    = true

        val storeConnector = Mockito.mock(classOf[TrustsStoreConnector])

        when(
          storeConnector
            .claim(eqTo(TrustsStoreRequest(userAnswersId, utr, managedByAgent, trustLocked)))(any(), any(), any())
        )
          .thenReturn(Future.successful(UserAnswersCached))

        val answers = emptyUserAnswers
          .set(IdentifierPage, utr)
          .success
          .value
          .set(IsAgentManagingTrustPage, managedByAgent)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[TrustsStoreConnector].toInstance(storeConnector),
            bind[Navigator].toInstance(fakeNavigator)
          )
          .build()

        val request = FakeRequest(GET, controllers.routes.IvFailureController.trustLocked().url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) must include(
          "As you have had 3 unsuccessful tries at accessing this trust you will need to try again in 30 minutes."
        )

        verifyMock(storeConnector)
          .claim(eqTo(TrustsStoreRequest(userAnswersId, utr, managedByAgent, trustLocked)))(any(), any(), any())

        application.stop()
      }

      "return OK and the correct view for a GET for not found route" in {

        val answers = emptyUserAnswers
          .set(IdentifierPage, "1234567890")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.routes.IvFailureController.trustNotFound().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) must include("The unique identifier you gave for the trust does not match our records")

        application.stop()
      }

      "return OK and the correct view for a GET for still processing route" in {

        val answers = emptyUserAnswers
          .set(IdentifierPage, "1234567891")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.routes.IvFailureController.trustStillProcessing().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) must include("We are processing the latest changes made to this trust")

        application.stop()
      }

      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, controllers.routes.IvFailureController.trustLocked().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

    }

  }

}
