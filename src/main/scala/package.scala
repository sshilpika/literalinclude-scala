package luc.literalinclude

/**
 * Created by sshilpika on 4/6/15.
 */

package object scala {
  val homeDir = System.getProperty("user.home")
  val accessToken = io.Source.fromFile(homeDir + "/githubAccessToken").getLines.next()
}
