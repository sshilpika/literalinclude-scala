package luc.literalinclude.service
package directive

/**
 * Created by sshilpika on 6/1/15.
 */



import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest

abstract class DirectiveSpec extends Specification with Specs2RouteTest with LiteralIncludeService {

  def actorRefFactory = system

  val sha = "cb3b7ac188a29812e1811e44f2e676363cd35705"
}
