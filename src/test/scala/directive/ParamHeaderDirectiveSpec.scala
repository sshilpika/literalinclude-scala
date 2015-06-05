/**
 * Created by sshilpika on 5/23/15.
 */
package luc.literalinclude.service
package directive

import spray.http.HttpHeaders.RawHeader
import spray.http.{ContentTypes, HttpCharsets, MediaTypes}
import spray.routing.{Rejection, ValidationRejection, MissingHeaderRejection, MalformedQueryParamRejection}
import org.specs2.matcher._
import scala.concurrent.duration._

class ParamHeaderDirectiveSpec extends DirectiveSpec {

  "The LiteralIncludeService" should {
    implicit val timeout = RouteTestTimeout(DurationInt(10000).millis)
    "return a greeting for GET requests to the root path" in {

      Get() ~> myRoute ~> check {
        responseAs[String] must contain("Welcome to the Awesome Literal Include Service!")
      }
    }
    "return commit information for GET requests with user and repository name" in {

      Get("/commits?user=django&repo=django") ~> myRoute ~> check {
        responseAs[String] must contain("commit")
      }
    }
    "fail to retrieve commit information for GET requests with malformed url" in {

      Get("/commits?user=django&repo=kjhfdhk") ~> myRoute ~> check {
        responseAs[String] must contain("\"message\":\"Not Found\"")
      }
    }
    "return issues for GET requests with user and repository name" in {

      Get("/issues?user=LoyolaChicagCode&repo=scala-tdd-fundamentals") ~> myRoute ~> check {
        responseAs[String] must contain("issues")
      }
    }
    /*"return a json string for GET requests with incomplete url" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
        responseAs[String] must contain("Failed to retrieve content, with error spray.json.DeserializationException: JSON object expected")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a json string for GET requests without parameters and malformed path" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational12333").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
        responseAs[String] must contain("UGxlYXNlIGVudGVyIGEgdmFsaWQgVXJsIGluIHRoZSBmb3JtIC9naXRodWIvY29kZS86dXNlci86cmVwby86YnJhbmNoL3BhdGg/bGluZXM9I0wxLSNMMiZkZWRlbnQ9I051bQ==")
        contentType === ContentTypes.`application/json`
      }
    }*/
    "return a json string for GET requests without parameters" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
        responseAs[String] must contain("fileContent")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a json string for GET requests without parameters and Content-Type: text/plain" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala").withHeaders(List(RawHeader("Content-Type", "text/plain"))) ~> myRoute ~> check {
        contentType === ContentTypes.`text/plain`.withCharset(HttpCharsets.`UTF-8`)
      }
    }
    "return a json string for GET requests with line range L1-L2" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=2-10") ~> addHeader("Content-Type", "jsonp") ~> myRoute ~> check {
        responseAs[String] must contain("fileContent")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a json string for GET requests with lines" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=-10") ~> addHeader("Content-Type", "jsonp") ~> myRoute ~> check {
        responseAs[String] must contain("fileContent")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a json string for GET requests with dedent" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?dedent=2") ~> addHeader("Content-Type", "jsonp") ~> myRoute ~> check {
        responseAs[String] must contain("fileContent")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a json string for GET requests with dedent and lines and no jsonp specified" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=-10&dedent=2").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
        responseAs[String] must contain("fileContent")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a javascript string for GET requests with jsonp, dedent and lines" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?jsonp=result&lines=-10&dedent=2").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
        responseAs[String] must contain("result")
        contentType === MediaTypes.`application/javascript`.withCharset(HttpCharsets.`UTF-8`)
      }
    }
  }


    "The LiteralIncludeService check" should {
      implicit val timeout = RouteTestTimeout(DurationInt(10000).millis)
      "fail for GET requests with empty lines parameter" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
          val message = "requirement failed: lines parameter must not be empty"
          rejectionConstruct(message)
        }
      }
      "fail for GET requests with empty lines parameter -" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=-").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
          val message = "requirement failed: lines parameter should be of the form L1-L2, where either L2 and L1 are optional"
          rejectionConstruct(message)
        }
      }
      "fail with error message for requests without headers specified" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala") ~> myRoute ~> check {
          rejectionConstruct("")
        }
      }
      "fail for GET requests with string values of lines parameter" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=aaa").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
          val message = "requirement failed: line values should be integers"
          rejectionConstruct(message)
        }
      }
      "fail for GET requests with lines= 20-aaa" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=20-aaa").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
          val message = "requirement failed: line values should be integers"
          rejectionConstruct(message)
        }
      }
      "fail for GET requests with lines parameter = L1 > L2" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=40-9").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
          val message = "requirement failed: Line arguments L1 should be greater than L2"
          rejectionConstruct(message)
        }
      }
      "fail for GET requests with negative dedent parameter" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?dedent=-1").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
          val message = "requirement failed: dedent has to be positive"
          rejectionConstruct(message)
        }
      }
      "fail for GET requests with String values of dedent parameter" in {

        Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?dedent=aaa").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
          val message = "'aaa' is not a valid 32-bit integer value"
          rejectionConstruct(message)
        }
      }
    }

  def rejectionConstruct(message: String): MatchResult[Rejection] = {
    rejection must beLike {
      case ValidationRejection(errMsg, cause) => message === errMsg
      case MalformedQueryParamRejection(paramName, errMsg, cause) => message === errMsg
      case MissingHeaderRejection("Content-Type") => ok
    }
  }

}


