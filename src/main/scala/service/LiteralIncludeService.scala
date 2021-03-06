package luc.literalinclude.service
//package service

/**
 * Created by sshilpika on 3/8/15.
 */

import java.io._
import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import org.apache.commons.codec.binary.Base64
import spray.can.Http
import spray.http.HttpHeaders.RawHeader
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.RequestBuilding._
import spray.json._
import spray.routing._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._

case class Options(lines: String,  dedent: Int){
  val linesArr = lines.split("-")
  require(!lines.isEmpty, "lines parameter must not be empty")
  require(linesArr.length > 0 , "lines parameter should be of the form L1-L2, where either L2 and L1 are optional")
  require(linesArr(0).forall(_.isDigit) , "line values should be integers")
  if(linesArr.length ==2) {
      require(linesArr(1).forall(_.isDigit) , "line values should be integers")
    if(!linesArr(0).isEmpty && linesArr(0).forall(_.isDigit))
      require(linesArr(0).toInt < linesArr(1).toInt, "Line arguments L1 should be greater than L2")
   }
  require(0 <= dedent, "dedent has to be positive")

}

case class JsonPResult(finalLines: String)

object JsonPResultProtocol {
  import spray.json.DefaultJsonProtocol._
  implicit val gitResult = jsonFormat(JsonPResult,"fileContent")
}

trait LiteralIncludeService extends HttpService {


  def getGithubContent(user: String, repo: String, branch: String, filePath: String, accessToken: Option[String]): Future[HttpResponse] = {

    implicit val actor = Option(Boot.system).getOrElse(ActorSystem("literalinclude"))
    implicit val timeout = Timeout(15.seconds)
    val rawHeadersList = accessToken.foldLeft(Nil: List[RawHeader])((list, token) => list:+RawHeader("Authorization", "token "+token))
    (IO(Http) ? Get("https://api.github.com/repos/" + user + "/" + repo + "/contents/" + filePath + "?ref=" + branch).withHeaders(rawHeadersList)).mapTo[HttpResponse]

  }

  def githubCall(user: String, repo: String, responseType: String): Future[HttpResponse] = {

    implicit val actor = Option(Boot.system).getOrElse(ActorSystem("literalinclude"))
    implicit val timeout = Timeout(5.seconds)
    (IO(Http) ? Get("https://api.github.com/repos/" + user + "/" + repo + "/" + responseType)).mapTo[HttpResponse]

  }

  def githubCallForContent(user: String, repo: String, branch: String, filePath: String, linesArr: Array[String], dedent: Int, accessToken: Option[String]): Future[String] = {

    getGithubContent(user, repo, branch, filePath, accessToken).map(x1 => {
      val res3 = x1.entity.data.asString.parseJson.asJsObject.fields.foldLeft(new StringBuilder("")) { case (lis, v) =>

        if (v._1.equals("content")) {

          val s1 = v._2.compactPrint.replace("\\n", " ")
          val s = new String(Base64.decodeBase64(s1), "UTF-8")
          val writer = new PrintWriter(new File("store.txt"))
          writer.write(s)
          writer.close()
          val iteratorStr: Iterator[String] = linesArr.length match {

            case 2 =>
              if (linesArr(0).isEmpty)
                io.Source.fromFile("store.txt").getLines().take(linesArr(1).toInt)

              else
                io.Source.fromFile("store.txt").getLines().slice(linesArr(0).toInt - 1, linesArr(1).toInt)

            case 1 => io.Source.fromFile("store.txt").getLines().drop(linesArr(0).toInt - 1)
            //case _ => io.Source.fromFile("store.txt").getLines()
          }
          lis.append(iteratorStr.map(x => {

            if (x.length >= dedent)
              x.substring(dedent)
            else
              ""

          }).mkString("\n"))

        }else if (v._1.equals("message")){

          val s1 = v._2.compactPrint.replace("\\n", " ")
          val s = new String(Base64.decodeBase64(s1), "UTF-8")

          val writer = new PrintWriter(new File("store.txt"))
          writer.write(s)
          writer.close()

          lis.append(io.Source.fromFile("store.txt").getLines().mkString("\n"))

        } else
          lis.append("")
      }

      if (res3.length == 0)
        res3.append("Please enter a valid Url in the form /github/code/:user/:repo/:branch/path?lines=#L1-#L2&dedent=#Num")
      new File("store.txt").delete()
      Base64.encodeBase64String(String.valueOf(res3).getBytes())
    })

  }


  val myRoute =
    pathEndOrSingleSlash {
      respondWithMediaType(`text/html`) {
        complete {
          <html>
            <body>
              <h3>Welcome to the Awesome Literal Include Service!</h3>
              <h3>Please enter a valid Url in the form
                <i>/github/code/:user/:repo/:branch/path?lines=#L1-#L2
                  &amp;
                  dedent=#Num</i>
              </h3>
              <h4>lines and dedent are optional</h4>
            </body>
          </html>
        }
      }

    } ~ path("commits") {
      get {
        respondWithMediaType(`text/plain`) {
          parameters('user, 'repo) { (user, repo) =>
            onComplete(githubCall(user, repo, "commits")) {
              case Success(value) =>
                complete(value)
              case Failure(value) =>
                complete(s"Failed to retrieve commits, with error $value")
            }
          }
        }
      }
    } ~
      path("issues") {
        get {
          parameters('user, 'repo) { (user, repo) =>
            redirect("https://api.github.com/repos/" + user + "/" + repo + "/issues", StatusCodes.TemporaryRedirect) //PermanentRedirect)
            //onComplete(githubCall(user,repo,"issues")) {complete(_)}
          }

        }
      } ~ // jsonp with lines and dedent
      path("github" ~ Slash ~ "code" ~ Slash ~ Segment ~ Slash ~ Segment ~ Slash ~ Segment ~ Slash ~ RestPath) { (user, repo, branch, path) => {
        get {
          (headerValueByName("Content-Type") & optionalHeaderValueByName("Authorization")) { (contentType, authToken) =>
            jsonpWithParameter("jsonp") {
              import JsonPResultProtocol._
              import spray.httpx.SprayJsonSupport._
              parameters('lines ? "1", 'dedent.as[Int] ? 0).as(Options) { (options) =>
                val linesArr = options.lines.split("-")
                onComplete(githubCallForContent(user, repo, branch, path.toString, linesArr, options.dedent, authToken)) {
                  case Success(value) =>
                    complete(if (contentType.equals("jsonp")) JsonPResult(value) else value)
                  case Failure(value) =>
                    complete(if (contentType.equals("jsonp")) JsonPResult(s"Failed to retrieve content, with error $value") else s"Failed to retrieve content, with error $value")
                }
              }
            }
          }
        }
      }
      }


  }


class MyServiceActor extends LiteralIncludeService with Actor{


  def actorRefFactory = context

  def receive = runRoute(myRoute)

}
