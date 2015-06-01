package directive

/**
 * Created by sshilpika on 6/1/15.
 */


import luc.literalinclude.service.LiteralIncludeService
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest

abstract class DirectiveSpec extends Specification with Specs2RouteTest with LiteralIncludeService {

  def actorRefFactory = system
}
