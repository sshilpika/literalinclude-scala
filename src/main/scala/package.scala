package luc.literalinclude.scala

/**
 * Created by sshilpika on 4/6/15.
 */

object `package` {
  val homeDir = System.getProperty("user.home")
  val accessToken = scala.io.Source.fromFile(homeDir + "/githubAccessToken").getLines.next()
}
