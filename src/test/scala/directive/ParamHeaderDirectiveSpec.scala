/**
 * Created by sshilpika on 5/23/15.
 */

package directive

import spray.http.HttpHeaders.RawHeader
import spray.http.{ContentTypes, HttpCharsets, MediaTypes}
import spray.routing.MissingHeaderRejection

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
    "return issues for GET requests with user and repository name" in {

      Get("/issues?user=LoyolaChicagCode&repo=scala-tdd-fundamentals") ~> myRoute ~> check {
        responseAs[String] must contain("issues")
      }
    }
    "return a header error message for requests without headers specified" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala") ~> myRoute ~> check {
        rejection must beLike {

          case MissingHeaderRejection("Content-Type") => ok
        }
      }
    }
    "return a json string for GET requests without parameters" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala").withHeaders(List(RawHeader("Content-Type", "jsonp"))) ~> myRoute ~> check {
        responseAs[String] must contain("fileContent")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a json string for GET requests with lines" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?lines=-10") ~> addHeader("Content-Type","jsonp") ~>  myRoute ~> check {
        responseAs[String] must contain("fileContent")
        contentType === ContentTypes.`application/json`
      }
    }
    "return a json string for GET requests with dedent" in {

      Get("/github/code/LoyolaChicagoCode/scala-tdd-fundamentals/master/src/main/scala/Rational.scala?dedent=2") ~> addHeader("Content-Type","jsonp") ~>  myRoute ~> check {
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
}