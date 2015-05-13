/**
 * Created by sshilpika on 4/6/15.
 */
package object common {
  val homeDir = System.getProperty("user.home")
  val accessToken = scala.io.Source.fromFile(homeDir + "/githubAccessToken").getLines.next()
}
