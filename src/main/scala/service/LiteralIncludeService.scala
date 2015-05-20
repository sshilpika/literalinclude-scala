package luc.literalinclude.service

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
import spray.http.MediaTypes._
import spray.http._
import spray.json._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._

trait LiteralIncludeService extends HttpService with Actor {
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

    } ~
      path("commits") {
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
      } ~ // with lines and dedent
      path("github" ~ Slash ~ "code" ~ Slash ~ Segment ~ Slash ~ Segment ~ Slash ~ Segment ~ Slash ~ RestPath) { (user, repo, branch, path) => {
        get {
          respondWithMediaType(`text/plain`) {
            parameters('lines ? "1", 'dedent ? "0") { (lines, dedent) =>
              val linesArr = lines.split("-")
              if (linesArr.length > 0 && !dedent.isEmpty && !lines.isEmpty) {

                onComplete(githubCallForContent(user, repo, branch, path.toString, linesArr, dedent.toInt)) {
                  case Success(value) =>
                    complete(value)
                  case Failure(value) =>
                    complete(s"Failed to retrieve content, with error $value")
                }

              } else {

                complete("Illegal parameter entry, enter lines in the format L1-L2 and integers for dedent")

              }
            }
          }
        }
      }
      } ~ // without lines and dedent
      path("github" ~ Slash ~ "code" ~ Slash ~ Segment ~ Slash ~ Segment ~ Slash ~ Segment ~ RestPath) { (user, repo, branch, path) => {
        get {
          respondWithMediaType(`text/plain`) {
            onComplete(githubCallForContent(user, repo, branch, path.toString, Array.empty, 0)) {
              case Success(value) =>
                complete(value)
              case Failure(value) =>
                complete(s"Failed to retrieve code content, with error $value")
            }
          }
        }
      }
      }

  def actorRefFactory = context

  import spray.httpx.RequestBuilding._

  def receive = runRoute(myRoute)

  def githubCall(user: String, repo: String, responseType: String): Future[HttpResponse] = {
    implicit val actor = context.system //ActorSystem("githubCall")
    implicit val timeout = Timeout(5.seconds)
    (IO(Http) ? Get("https://api.github.com/repos/" + user + "/" + repo + "/" + responseType)).mapTo[HttpResponse]


  }

  def githubCallForContent(user: String, repo: String, branch: String, filePath: String, linesArr: Array[String], dedent: Int): Future[String] = {

    getGithubContent(user, repo, branch, filePath).map(x1 => {
      val res3 = x1.entity.data.asString.parseJson.asJsObject.fields.foldLeft(new StringBuilder("")) { case (lis, v) =>

        if (v._1.equals("content")) {

          val s1 = v._2.compactPrint.replace("\\n", " ")
          val s = new Predef.String(Base64.decodeBase64(s1), "UTF-8")
          val writer = new PrintWriter(new File("store.txt"))
          writer.write(s)
          writer.close()
          val iteratorStr: Iterator[String] = linesArr.length match {

            case 2 =>
              if (linesArr(0).isEmpty)
                io.Source.fromFile("store.txt").getLines().take(linesArr(1).toInt)

              else {

                if (linesArr(0).toInt < linesArr(1).toInt)
                  io.Source.fromFile("store.txt").getLines().slice(linesArr(0).toInt - 1, linesArr(1).toInt)
                else
                  Iterator("Line arguments L1 should be greater than L2")

              }
            case 1 => io.Source.fromFile("store.txt").getLines().drop(linesArr(0).toInt - 1)
            case _ => io.Source.fromFile("store.txt").getLines()
          }
          lis.append(iteratorStr.map(x => {

            if (x.length >= dedent)
              x.substring(dedent)
            else
              ""

          }).mkString("\n"))

        } else
          lis.append("")
      }

      if (res3.length == 0)
        res3.append("Please enter a valid Url in the form /github/code/:user/:repo/:branch/path?lines=#L1-#L2&dedent=#Num")

      res3.mkString
    })
  }

  def getGithubContent(user: String, repo: String, branch: String, filePath: String): Future[HttpResponse] = {

    implicit val actor = context.system
    implicit val timeout = Timeout(15.seconds)
    (IO(Http) ? Get("https://api.github.com/repos/" + user + "/" + repo + "/contents/" + filePath + "?ref=" + branch)).mapTo[HttpResponse]

  }
}


class MyServiceActor extends LiteralIncludeService {


}
