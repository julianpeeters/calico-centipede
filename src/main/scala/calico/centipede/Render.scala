package calico.centipede

import org.scalajs.dom.CanvasRenderingContext2D

object Render:

  trait `2D`[F[_], A]:
    def render(obj: A)(using Ctx: CanvasRenderingContext2D): F[Unit]