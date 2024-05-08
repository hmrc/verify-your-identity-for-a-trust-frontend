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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.{TrustsStoreRequest, UserAnswersCached}
import org.scalatest.RecoverMethods
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class TrustsStoreConnectorSpec extends SpecBase with WireMockHelper with RecoverMethods {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(defaultAppConfigurations ++ Map("microservice.services.trusts-store.port" -> server.port()))
    .build()

  lazy val connector: TrustsStoreConnector = app.injector.instanceOf[TrustsStoreConnector]

  lazy val url: String = "/trusts-store/claim"

  val utr = "1234567890"
  val internalId = "some-authenticated-internal-id"
  val managedByAgent = true

  val request: TrustsStoreRequest = TrustsStoreRequest(internalId, utr, managedByAgent, false)

  private def wiremock(payload: String, status: Int, response: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(url))
        .withHeader(CONTENT_TYPE, containing("application/json"))
        .withRequestBody(equalTo(payload))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
        )
    )

  "TrustsStoreConnector" must {

    "call POST /claim" which {

      "returns 201 CREATED" in {

        val json = Json.stringify(Json.toJson(request))

        val response =
          """{
            |  "utr": "a string representing the tax reference to associate with this internalId",
            |  "managedByAgent": "boolean derived from answers in the claim a trust journey"
            |}""".stripMargin


        wiremock(
          payload = json,
          status = Status.CREATED,
          response = response
        )

        connector.claim(request) map { response =>
          response mustBe UserAnswersCached
        }

      }

      "returns 400 BAD_REQUEST" in {

        val json = Json.stringify(Json.toJson(request))

        val response =
          """{
            |  "status": "400",
            |  "message":  "Unable to parse request body into a TrustClaim"
            |}""".stripMargin

        wiremock(
          payload = json,
          status = Status.BAD_REQUEST,
          response = response
        )

        recoverToSucceededIf[UpstreamErrorResponse](connector.claim(request))

      }
      "returns 500 INTERNAL_SERVER_ERROR" in {

        val json = Json.stringify(Json.toJson(request))

        val response =
          """{
            |  "status": "500",
            |  "message":  ""unable to store to trusts store""
            |}""".stripMargin

        wiremock(
          payload = json,
          status = Status.INTERNAL_SERVER_ERROR,
          response = response
        )

        recoverToSucceededIf[UpstreamErrorResponse](connector.claim(request))

      }

    }

  }

}
