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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import config.FrontendAppConfig
import models.TaxEnrolmentsRequest
import org.scalatest.{AsyncWordSpec, MustMatchers, RecoverMethods}
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream5xxResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class TaxEnrolmentsConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with RecoverMethods {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  lazy val config: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  lazy val connector: TaxEnrolmentsConnector = app.injector.instanceOf[TaxEnrolmentsConnector]

  lazy val app = new GuiceApplicationBuilder()
    .configure(Seq(
      "microservice.services.tax-enrolments.port" -> server.port(),
      "auditing.enabled" -> false): _*
    )
    .build()

  lazy val url: String = s"/tax-enrolments/service/${config.serviceName}/enrolment"

  val utr = "1234567890"

  val request = Json.stringify(Json.obj(
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "SAUTR",
        "value" -> utr
      )),
    "verifiers" -> Json.arr(
      Json.obj(
        "key" -> "SAUTR1",
        "value" -> utr
      )
    )
  ))


  private def wiremock(payload: String, expectedStatus: Int, expectedResponse: String) =
    server.stubFor(
      put(urlEqualTo(url))
        .withHeader(CONTENT_TYPE, containing("application/json"))
        .withRequestBody(equalTo(payload))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedResponse)
        )
    )

  "TaxEnrolmentsConnector" must {

    "call PUT /" which {

      "returns 204 NO_CONTENT" in {

        wiremock(
          payload = request,
          expectedStatus = NO_CONTENT,
          expectedResponse = ""
        )

        connector.enrol(TaxEnrolmentsRequest(utr)) map { response =>
          response.status mustBe NO_CONTENT
        }

      }

      "returns 400 BAD_REQUEST" in {

        val response =
          """{
            |  "status": "400",
            |  "message":  "Unable to parse request body into a TrustClaim"
            |}""".stripMargin

        wiremock(
          payload = request,
          expectedStatus = BAD_REQUEST,
          expectedResponse = response
        )

        recoverToSucceededIf[BadRequestException](connector.enrol(TaxEnrolmentsRequest(utr)))

      }
      "returns 500 INTERNAL_SERVER_ERROR" in {

        val response =
          """{
            |  "status": "500",
            |  "message":  ""unable to store to trusts store""
            |}""".stripMargin

        wiremock(
          payload = request,
          expectedStatus = Status.INTERNAL_SERVER_ERROR,
          expectedResponse = response
        )

        recoverToSucceededIf[Upstream5xxResponse](connector.enrol(TaxEnrolmentsRequest(utr)))

      }

    }

  }

}
