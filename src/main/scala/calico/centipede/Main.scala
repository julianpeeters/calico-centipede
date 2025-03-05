package calico.centipede

import calico.IOWebApp
import cats.effect.{IO, Resource}
import cats.implicits.given
import fs2.dom.*

object Main extends IOWebApp:

  def render: Resource[IO, HtmlElement[IO]] =
    for
      s <- Game.state
      i <- Game.inputs(s)
      c <- Game.canvas
      b <- Game.behaviors(s).toResource
      p <- Game.page(i, c)
      R <- Run.canvasRenderer(c).toResource
      _ <- R.loop(i.focus >> R.advance(s, b) >>= R.draw)
    yield p