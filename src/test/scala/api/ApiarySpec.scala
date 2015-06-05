package luc.literalinclude.service
package api

import org.specs2.mutable.Specification
import org.specs2.matcher.JsonMatchers
import dispatch._

/**
 * Created by sshilpika on 5/23/15.
 */


/*class ApiarySpec extends HttpSpec {
  val serviceRoot = host("private-d6d49-literalincludescala.apiary-mock.com")
}*/

/*class LocalSpec extends HttpSpec {
  val serviceRoot = host("localhost",8080)
}*/

class HerokuSprayHttpSpec extends HttpSpec{
  val serviceRoot = host("literalinclude.herokuapp.com")
}

trait HttpSpec extends Specification with JsonMatchers{

  sequential
  def serviceRoot: Req

  val owner = "LoyolaChicagoCode"
  val repo = "scala-tdd-fundamentals"
  val branch = "master"
  val lines = "20-30"
  val lines1 = "40"
  val lines2 = "-30"
  val lines3 = "40-"
  val dedent = 2
  val dedent1 = "PPP"
  val dedent2 = -2
  val lines4 = "aaa"
  val lines5 = "10-3"
  
  val sha = "cb3b7ac188a29812e1811e44f2e676363cd35705"

  /*"The literal include service, on call to file," should {
    "return the entire file content, with empty parameter list, Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getStatusCode === 200
    }
    "return the entire file content, with empty parameter list, Content-Type = Any" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha
      val response = Http(request.setHeader("Content-Type","Any").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (L1-L2), Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (L1-L2), with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (L1), Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines1)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (L1), with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines1)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (-L1), Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines2)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (-L1), with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines2)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (L1-), Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines3)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter lines = (L1-), with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines3)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter dedent = (#N), Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("dedent" -> dedent.toString)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameter dedent = (#N), with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("dedent" -> dedent.toString)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameters lines = (L1-L2), dedent = (#N), Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines, "dedent" -> dedent.toString)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getStatusCode === 200
    }
    "return the entire range of lines in file, with parameters lines = (L1-L2), dedent = (#N), with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines, "dedent" -> dedent.toString)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getStatusCode === 200
    }
  }

  "The literal include service, on error while call to file," should {
    "return the entire range of lines in file, with parameter lines = String, Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines4)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getResponseBody === "requirement failed: line values should be integers"
    }
    "return the entire range of lines in file, with parameter lines = String, with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines4)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getResponseBody === "requirement failed: line values should be integers"
    }
    "return the entire range of lines in file, with parameter lines = Empty, Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> "")
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getResponseBody === "requirement failed: lines parameter must not be empty"
    }
    "return the entire range of lines in file, with parameter lines = Empty, with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> "")
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getResponseBody === "requirement failed: lines parameter must not be empty"
    }
    "return error message, with parameter lines = L1 > L2, with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines5)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getResponseBody === "requirement failed: Line arguments L1 should be greater than L2"
    }
    "return error message, with parameter lines = L1 > L2, with Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("lines" -> lines5)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getResponseBody === "requirement failed: Line arguments L1 should be greater than L2"
    }
    "return the entire range of lines in file, with parameter dedent = String, Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("dedent" -> dedent1)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getResponseBody === "The query parameter 'dedent' was malformed:\n'PPP' is not a valid 32-bit integer value"
    }
    "return the entire range of lines in file, with parameter dedent = String, with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("dedent" -> dedent1)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getResponseBody === "The query parameter 'dedent' was malformed:\n'PPP' is not a valid 32-bit integer value"
    }
    "return the entire range of lines in file, with parameter dedent = negative, Content-Type = jsonp" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("dedent" -> dedent2.toString)
      val response = Http(request.setHeader("Content-Type","jsonp").GET)
      response().getResponseBody === "requirement failed: dedent has to be positive"
    }
    "return the entire range of lines in file, with parameter dedent = negative, with Content-Type = text/plain" in {

      val request = serviceRoot / "github" / "code" / owner / repo / sha <<? Map("dedent" -> dedent2.toString)
      val response = Http(request.setHeader("Content-Type","text/plain").GET)
      response().getResponseBody === "requirement failed: dedent has to be positive"
    }
  }*/
}