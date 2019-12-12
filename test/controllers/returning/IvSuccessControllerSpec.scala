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

package controllers.returning

import base.SpecBase
import connectors.TaxEnrolmentsConnector
import models.{EnrolmentCreated, TaxEnrolmentsRequest, UpstreamTaxEnrolmentsError, UserAnswers}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito.{verify => verifyMock, _}
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{IsAgentManagingTrustPage, UtrPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{RelationshipEstablishment, RelationshipFound}
import uk.gov.hmrc.http.BadRequestException
import views.html.returning

import scala.concurrent.Future

class IvSuccessControllerSpec extends SpecBase with BeforeAndAfterAll {

  private val utr = "0987654321"

  private val connector = mock[TaxEnrolmentsConnector]
  private val mockRelationshipEstablishment = mock[RelationshipEstablishment]

  "IvSuccess Controller" must {

    "return OK and the correct view for a GET with no Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, false).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
        .overrides(
          bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
        )
        .build()

      val request = FakeRequest(GET, controllers.returning.routes.IvSuccessController.onPageLoad().url)

      val view = application.injector.instanceOf[returning.IvSuccessView]

      val viewAsString = view(isAgent = false, utr)(fakeRequest, messages).toString

      when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
        .thenReturn(Future.successful(RelationshipFound))

      when(connector.enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any()))
        .thenReturn(Future.successful(EnrolmentCreated))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verifyMock(connector).enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any())
      verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

      reset(connector)
      reset(mockRelationshipEstablishment)

      application.stop()

    }

    "return OK and the correct view for a GET with Agent" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(IsAgentManagingTrustPage, true).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
        .overrides(
          bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
        )
        .build()

      val request = FakeRequest(GET, controllers.returning.routes.IvSuccessController.onPageLoad().url)

      val view = application.injector.instanceOf[returning.IvSuccessView]

      val viewAsString = view(isAgent = true, utr)(fakeRequest, messages).toString

      when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
        .thenReturn(Future.successful(RelationshipFound))

      when(connector.enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any()))
        .thenReturn(Future.successful(EnrolmentCreated))

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual viewAsString

      verifyMock(connector).enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any())
      verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

      reset(connector)
      reset(mockRelationshipEstablishment)

      application.stop()

    }

    "redirect to Session Expired" when {

      "no existing data is found" in {

        lazy val application = applicationBuilder(userAnswers = None).build()

        lazy val request = FakeRequest(GET, controllers.returning.routes.IvSuccessController.onPageLoad().url)

        lazy val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()

      }

    "redirect to Internal Server Error" when {

      "tax enrolments fails" when {

        "401 UNAUTHORIZED" in {

          val utr = "1234567890"

          val userAnswers = UserAnswers(userAnswersId)
            .set(IsAgentManagingTrustPage, true).success.value
            .set(UtrPage, utr).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
            .overrides(
              bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
            )
            .build()

          val request = FakeRequest(GET, controllers.returning.routes.IvSuccessController.onPageLoad().url)

          when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
            .thenReturn(Future.successful(RelationshipFound))

          when(connector.enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any()))
            .thenReturn(Future.failed(UpstreamTaxEnrolmentsError("Unauthorized")))

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR

          verifyMock(connector).enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any())
          verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

          reset(connector)
          reset(mockRelationshipEstablishment)

          application.stop()

        }
        "400 BAD_REQUEST" in {

          val utr = "0987654321"

          val userAnswers = UserAnswers(userAnswersId)
            .set(IsAgentManagingTrustPage, true).success.value
            .set(UtrPage, utr).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers), relationshipEstablishment = mockRelationshipEstablishment)
            .overrides(
              bind(classOf[TaxEnrolmentsConnector]).toInstance(connector)
            )
            .build()

          val request = FakeRequest(GET, controllers.returning.routes.IvSuccessController.onPageLoad().url)

          when(mockRelationshipEstablishment.check(eqTo("id"), eqTo(utr))(any()))
            .thenReturn(Future.successful(RelationshipFound))

          when(connector.enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any()))
            .thenReturn(Future.failed(new BadRequestException("BadRequest")))

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR

          verifyMock(connector).enrol(eqTo(TaxEnrolmentsRequest(utr)))(any(), any(), any())
          verifyMock(mockRelationshipEstablishment).check(eqTo("id"), eqTo(utr))(any())

          reset(connector)
          reset(mockRelationshipEstablishment)

          application.stop()

        }
      }

    }

    }

  }
}
