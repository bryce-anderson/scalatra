package org.scalatra

import javax.servlet.ServletContext
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import servlet.ServletApiImplicits



/**
* The core Scalatra DSL.
*/
trait CoreDsl extends Handler with Control with ScalatraContext with ServletApiImplicits {

  import scala.reflect.internal.annotations.compileTimeOnly
  import org.scalatra.macros.DSLMacros._
  import scala.language.experimental.macros

  @compileTimeOnly("Http Request cannot be called outside of a http method builder")
  implicit def request: HttpServletRequest = sys.error("Shouldn't get here.")

  @compileTimeOnly("Http Response cannot be called outside of a http method builder")
  implicit def response: HttpServletResponse = sys.error("Shouldn't get here.")

  def addRoute(method: HttpMethod,
               transformers: Seq[RouteTransformer],
               action: (HttpServletRequest, HttpServletResponse) => Any): Route

  /**
   * Adds a filter to run before the route.  The filter only runs if each
   * routeMatcher returns Some.  If the routeMatchers list is empty, the
   * filter runs for all routes.
   */
  def beforeAction(transformers: RouteTransformer*)(fun: Action): Unit

  def before(transformers: RouteTransformer*)(block: Any): Unit = macro beforeImpl

  /**
   * Adds a filter to run after the route.  The filter only runs if each
   * routeMatcher returns Some.  If the routeMatchers list is empty, the
   * filter runs for all routes.
   */
  def afterAction(transformers: RouteTransformer*)(fun: Action): Unit

  def after(transformers: RouteTransformer*)(block: Any): Unit = macro afterImpl

  /**
   * Defines a block to run if no matching routes are found, or if all
   * matching routes pass.
   */
  def notFoundAction(block: Action): Unit

  def notFound(block: Any): Unit = macro notFoundImpl

  /**
   * Defines a block to run if matching routes are found only for other
   * methods.  The set of matching methods is passed to the block.
   */
  def methodNotAllowed(f: (HttpServletRequest, HttpServletResponse, Set[HttpMethod]) => Any): Unit

  /**
   * Defines an error handler for exceptions thrown in either the before
   * block or a route action.
   *
   * If the error handler does not match, the result falls through to the
   * previously defined error handler.  The default error handler simply
   * rethrows the exception.
   *
   * The error handler is run before the after filters, and the result is
   * rendered like a standard response.  It is the error handler's
   * responsibility to set any appropriate status code.
   */
  def error(handler: ErrorHandler): Unit = macro errorImpl

  def errorAction(handler: ErrorHandlerAction): Unit

  /**
   * The Scalatra DSL core methods take a list of [[org.scalatra.RouteMatcher]]
   * and a block as the action body.  The return value of the block is
   * rendered through the pipeline and sent to the client as the response body.
   *
   * See [[org.scalatra.ScalatraBase#renderResponseBody]] for the detailed
   * behaviour and how to handle your response body more explicitly, and see
   * how different return types are handled.
   *
   * The block is executed in the context of a CoreDsl instance, so all the
   * methods defined in this trait are also available inside the block.
   *
   * {{{
   *   get("/") {
   *     <form action="/echo">
   *       <label>Enter your name</label>
   *       <input type="text" name="name"/>
   *     </form>
   *   }
   *
   *   post("/echo") {
   *     "hello {params('name)}!"
   *   }
   * }}}
   *
   * ScalatraKernel provides implicit transformation from boolean blocks,
   * strings and regular expressions to [[org.scalatra.RouteMatcher]], so
   * you can write code naturally.
   * {{{
   *   get("/", request.getRemoteHost == "127.0.0.1") { "Hello localhost!" }
   * }}}
   *
   */
  def get(transformers: RouteTransformer*)(block: => Any): Route = macro getImpl

  /**
   * @see get
   */
  def post(transformers: RouteTransformer*)(block: => Any): Route = macro postImpl

  /**
   * @see get
   */
  def put(transformers: RouteTransformer*)(block: => Any): Route = macro putImpl

  /**
   * @see get
   */
  def delete(transformers: RouteTransformer*)(block: => Any): Route = macro deleteImpl

  /**
   * @see get
   */
  def options(transformers: RouteTransformer*)(block: => Any): Route = macro optionsImpl

  /**
   * @see head
   */
  def head(transformers: RouteTransformer*)(block: => Any): Route = macro headImpl

  /**
   * @see patch
   */
  def patch(transformers: RouteTransformer*)(block: => Any): Route = macro patchImpl


  /**
   * Error handler for HTTP response status code range. You can intercept every response code previously
   * specified with #status or even generic 404 error.
   * {{{
   *   trap(403) {
   *    "You are not authorized"
   *   }
   }* }}}
   }}*/
  def trapAction(codes: Range)(block: Action): Unit

  //  /**
  //   * @see error
  //   */
  def trap(codes: Range)(block: Any): Unit = macro trapRangeImpl

  def trap(code: Int)(block: Any): Unit = macro trapImpl

  /**
   * Converts a boolean expression to a route matcher.
   *
   * @param block a block that evaluates to a boolean
   *
   * @return a route matcher based on `block`.  The route matcher should
   *         return `Some` if the block is true and `None` if the block is false.
   *
   * @see [[org.scalatra.BooleanBlockRouteMatcher]]
   */
  protected implicit def booleanBlock2RouteMatcher(block: Boolean): RouteMatcher = macro booleanBlock2RouteMatcherImpl

}
