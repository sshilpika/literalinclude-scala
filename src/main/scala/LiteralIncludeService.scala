package luc.literalinclude.scala
import common._
/**
 * Created by sshilpika on 3/8/15.
 */


import java.net.URI

import _root_.scredis.Redis
import akka.util.Timeout
import spray.httpx.SprayJsonSupport
import spray.routing.directives.OnCompleteFutureMagnet
import scala.util._
import scala.concurrent.ExecutionContext.Implicits._
import akka.actor._
import spray.httpx.encoding.Deflate
import spray.routing.authentication.BasicAuth
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import scala.concurrent.Future

import spray.can.Http
import akka.io.IO
import akka.pattern.ask
import spray.http._
import HttpMethods._
import scala.concurrent.duration._

import spray.httpx.marshalling._
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport._
import org.apache.commons.codec.binary.Base64
import spray.json.lenses.JsonLenses._
import java.io._

case class githubRepoContent( name:String, path:String, size:Int ,content:String)
case class Content(content:String)
object RepoContentJsonSupport extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val contentFormat = jsonFormat4(githubRepoContent)
  //implicit val contents = jsonFormat(Content,"content")
}


case class gitH(content:String)


object gitFor extends DefaultJsonProtocol{
  implicit val con = jsonFormat(gitH,"content")

  def write(c: gitH) =
    JsArray(JsString(c.content))
}

trait LiteralIncludeService extends HttpService {


  import spray.httpx.RequestBuilding._
  def githubCall(user: String, repo: String, responseType:String): Future[HttpResponse] =  {
    implicit val actor = ActorSystem("githubCall")
    implicit val timeout = Timeout(5.seconds)
    val response2: Future[HttpResponse] =
      (IO(Http) ? Get("https://api.github.com/repos/"+user+"/"+repo+"/"+responseType)).mapTo[HttpResponse]

    response2
  }

  def githubCallForFilePaths(user: String, repo: String, sha:String): Future[String] =  {
    implicit val actor = ActorSystem("githubCallForFilePaths")
    implicit val timeout = Timeout(15.seconds)
    val contents = {
      (IO(Http) ? (Get("https://api.github.com/repos/" + user + "/" + repo + "/git/trees/" + sha+"?recursive=1")/* ~> addHeader("Authorization","token "+accessToken)*/)).mapTo[HttpResponse]
    }

    val res2 = contents.map(x1 =>


      x1.entity.data.asString.parseJson.asJsObject.fields.filter{case (s, v) =>
        Set("tree").contains(s)
      }//.toString
    )

    val res3 = res2.flatMap(x => {

        val tx = x.transform( (a,b) =>{
          //b.fields.filter
          val maps = b.toString.replaceAll("[\\[\\]]","").split("},")
          val newMap = for{
            m <- maps
            if(! m.equals(maps(maps.length-1)))
        }yield m+"}"

          val newMap1 = newMap.map( x => {
            x.parseJson.asJsObject.fields.filter { case (s, v) => {
              var bool = false
              if(v.toString.contains("/blobs/")) {
                bool = Set("url").contains(s)

              }
              bool

            }}
          })
          //val newMap2 = newMap1.filter{x => x.nonEmpty}//.mkString("\t")
          val res4: Array[Future[Int]] = for{
          m <- newMap1
          if(m.nonEmpty)
          x <- m.foldLeft(Nil:List[Future[Int]])((a,b) => {
            a:+githubCallToContentsN(b._2.compactPrint.replaceAll("[\"]",""))
          })
          }yield x


          val res5 = Future.sequence(res4.toList)
          var finalResult = 0
          val res6 = res5.map(a => {
            val writer = new FileWriter("test2.txt",true)
            finalResult = a.sum
            try{
              writer.write(a.sum)
            }
            finally writer.close()
            a.sum
          })

          res6
        }
        )
          tx("tree")

      })

    val resF = res3.map(a => a.toString)
    resF
  }



  def githubCallToContentsN(url:String): Future[Int] =  {
    implicit val actor = ActorSystem("githubCallForContent")
    implicit val timeout = Timeout(15.seconds)
    val contents = {
      //println("https://api.github.com/repos/"+user+"/"+repo+"/contents/"+filePath)
      (IO(Http) ? (Get(url))).mapTo[HttpResponse]
    }
    import RepoContentJsonSupport._

    val res2 = contents.map(x1 =>


      x1.entity.data.asString.parseJson.asJsObject.fields.filter{case (s, v) =>
        Set("content").contains(s)

      }
    )
    val res3 = res2.map(x => {
      val r = x.mapValues { b =>
        val s1 = b.compactPrint.replace("\\n"," ")
        val s = new String(Base64.decodeBase64(s1), "UTF-8")
        val strNoComments = s.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","")
        val strNoComments1 = strNoComments.replaceAll("\\n\\r","")



        val writer1 = new PrintWriter(new File("test1.txt" ))
        writer1.write(strNoComments1)
        writer1.close()
        val lines = io.Source.fromFile("test1.txt").getLines().size
        lines
      }
      val fw = new FileWriter("test3.txt", true)
      try {
        fw.write(r("content").toString+"\t"+url+"\n")
      }
      finally fw.close()
      r("content")

    })

    res3

  }


  def githubCallToContents(user: String, repo: String, filePath:String): Future[String] =  {
    implicit val actor = ActorSystem("githubCallForContent")
    implicit val timeout = Timeout(15.seconds)
    val contents = {
      (IO(Http) ? Get("https://api.github.com/repos/" + user + "/" + repo + "/contents/" + filePath)).mapTo[HttpResponse]
    }
    import RepoContentJsonSupport._
    /*val c = contents.flatMap(x => {x.flatMap(x1 => {
        val result = x1.toString.parseJson.asJsObject.getFields("name","size","path","contents") match{
          case Seq(JsString(name), JsString(path), JsNumber(size), JsString(contents1)) =>
            new githubRepoContent(name, path, size.toInt, contents1)
        }

        Future{new String(Base64.decodeBase64(result.contents),"UTF-8")}
      })})*/

    val res2 = contents.map(x1 =>


      x1.entity.data.asString.parseJson.asJsObject.fields.filter{case (s, v) =>
        Set("content").contains(s)

      }
    )


    val res3 = res2.map(x => {
      val r = x.mapValues { b =>
        val s1 = b.compactPrint.replace("\\n"," ")
        val s = new String(Base64.decodeBase64(s1), "UTF-8")
        //val strNoComments = s.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","")
        //val strNoComments1 = strNoComments.replaceAll("\\n\\r","")

        val writer = new PrintWriter(new File("test.txt" ))

        writer.write(s)
        writer.close()

        val writer1 = new PrintWriter(new File("test1.txt" ))
        writer1.write(s1)
        writer1.close()
        io.Source.fromFile("test.txt").getLines().mkString("\n")
      }
      r("content").toString

    })

    res3

  }

  val completeWithUnmatchedPath =
    unmatchedPath { p =>
      complete(p.toString)
    }
  val myRoute =
    path("test") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }~ pathEndOrSingleSlash{

        complete("/foo test")


    }~ pathPrefix("prefixTest"){
        pathEnd {
          complete("/foo1111 test")
        }~path("path2"){
          complete("Prefix path2")
        }

    }~ pathPrefixTest("PPT1" | "PPT2"){
      pathPrefix("PPT1"){
        completeWithUnmatchedPath
      }~ pathPrefix("PPT2"){
        completeWithUnmatchedPath
      }
    }~ path("Seg"/Segment) {

      s =>
        complete(if (s.equals("segment")) "even ball" else "odd ball")
    }~
      path("commits") {
        get{

          parameters('user, 'repo) { (user, repo) =>
            onComplete (githubCall(user,repo,"commits")){
              case Success(value) => respondWithMediaType(`application/json`) {
                complete(value)
              }
              //case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            }
          }
        }
      }~ path("issues") {
      //redirect("https://api.github.com/repos/"+user+"/"+repo+"/"+responseType)

        get{

          parameters('user, 'repo) { (user, repo) =>
            redirect("https://api.github.com/repos/"+user+"/"+repo+"/issues",StatusCodes.TemporaryRedirect)//PermanentRedirect)
            //onComplete(githubCall(user,repo,"issues")) {complete(_)}
          }

        }
      }~
      path("loc") {
        get{
          respondWithMediaType(`text/plain`)  {
          //parameters('user, 'repo) { (user, repo) =>
            onComplete(githubCallToContents("sshilpika","metrics-test","src/main/scala/Boot.scala")) {
              case Success(value) =>  //respondWithMediaType(`application/json`)  {
                complete(value)
              //}
              //case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
             // complete{

              /*(x:Future[DefaultJsonProtocol]) => {
                  x onSuccess {
                    case _ => _
                  }


                } *///}
            }
          }
          }


      }~
      path("literalinclude" ~Slash~ "github" ~Slash~ "code") {
        get{
          respondWithMediaType(`text/plain`)  {
            parameters('user, 'repo, 'path) { (user, repo, path) =>
              //http://localhost:5000/literalinclude/github/code?user=LoyolaChicagoCode&repo=scala-tdd-fundamentals
              onComplete(githubCallToContents(user, repo , path)) {
                case Success(value) =>  //respondWithMediaType(`application/json`)  {
                  complete(value)
                // }
                //case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                // complete{

                /*(x:Future[DefaultJsonProtocol]) => {
                    x onSuccess {
                      case _ => _
                    }


                  } *///}
              }
            }
          }
        }


      }~path("literalinclude" ~Slash~ "github" ~Slash~ "code"~Slash~ Segment~Slash~ Segment ~RestPath) {(user,repo, path)=>{
        get{
          respondWithMediaType(`text/plain`)  {
            //parameters('user, 'repo, 'path) { (user, repo, path) =>
              //http://localhost:5000/literalinclude/github/code?user=LoyolaChicagoCode&repo=scala-tdd-fundamentals
              onComplete(githubCallToContents(user, repo, path.toString)) {
                case Success(value) =>  //respondWithMediaType(`application/json`)  {
                  complete(value)
                // }
                //case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                // complete{

                /*(x:Future[DefaultJsonProtocol]) => {
                    x onSuccess {
                      case _ => _
                    }


                  } *///}
              }
            }
          }
        }


      }




  // TODO Authentication

  val route = {
    path("orders") {
      authenticate(BasicAuth(realm = "admin area")) { user =>
        get {
          //cache(simpleCache) {
            encodeResponse(Deflate) {
              complete {
                // marshal custom object with in-scope marshaller
                <html>
                  <body>
                    <h1>DOne using AUth</h1>
                  </body>
                </html>
             // }
            }
          }
        }
      }
    }
  }

}


class MyServiceActor extends Actor with LiteralIncludeService {
  //import context.dispatcher
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}
