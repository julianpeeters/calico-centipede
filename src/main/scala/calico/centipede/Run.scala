package calico.centipede

import cats.data.State
import cats.effect.Resource
import cats.effect.kernel.Async
import cats.syntax.all.given
import fs2.concurrent.SignallingRef
import fs2.dom.HtmlCanvasElement
import fs2.Stream
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas
import scala.concurrent.duration.*

trait Run[F[_]]:
  def advance[A](state: SignallingRef[F, A], behavior: State[A, Unit]): F[A]
  def draw[A](obj: A)(using R: Render.`2D`[F, A]): F[Unit]
  def loop(actionSequence: F[Unit]): Resource[F, Unit]

object Run:

  def canvasRenderer[F[_]: Async](html: HtmlCanvasElement[F]): F[Run[F]] =
    for
      can <- Async[F].delay(html.asInstanceOf[Canvas])
      ctx <- Async[F].delay(can.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
    yield

      new Run[F]:

        def advance[A](
          state: SignallingRef[F, A],
          behavior: State[A, Unit]
        ): F[A] =
            state.modifyState(behavior) >> state.get

        def draw[A](obj: A)(using R: Render.`2D`[F, A]): F[Unit] =
          R.render(obj)(using ctx)

        def loop(actionSequence: F[Unit]): Resource[F, Unit] =
          for
            T <- Stream.awakeDelay[F](25.milliseconds).as(()).holdResource(())
            _ <- T.discrete.evalMapChunk(_ => actionSequence).holdResource(())
          yield ()
