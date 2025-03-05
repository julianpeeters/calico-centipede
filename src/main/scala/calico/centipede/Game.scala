package calico.centipede

import calico.centipede.models.{Bullet, EnemySegment, GameState, Player}
import calico.html.io.{*, given}
import cats.data.State
import cats.effect.{IO, Resource}
import fs2.concurrent.SignallingRef
import fs2.dom.{HtmlCanvasElement, HtmlDivElement, HtmlLabelElement}

object Game:

  def behaviors(state: SignallingRef[IO, GameState]): IO[State[GameState, Unit]] =
    for
      s <- state.get
    yield s.behaviors.foldLeft(State.pure(())): (acc, b) =>
      acc.flatMap(_ => b)

  val canvas: Resource[IO, HtmlCanvasElement[IO]] =
    canvasTag.withSelf: (self: HtmlCanvasElement[IO]) =>
      (
        idAttr     := "game_canvas",
        widthAttr  := GameState.width,
        heightAttr := GameState.height,
      )

  def inputs(state: SignallingRef[IO, GameState]):  Resource[IO, HtmlLabelElement[IO]] =
    label("Controls: ",
      input.withSelf { self =>
        (
          placeholder := "↑, ↓, ←, →, and SHIFT",
          idAttr := "game_input",
          onKeyDown --> (_.foreach: e =>
            for
              _ <- state.modify(s =>  e.code match
                case "ArrowLeft"  =>
                  (s.copy(player1 = s.player1.copy(xPos = s.player1.xPos - Player.rate)), ())
                case "ArrowRight" =>
                  (s.copy(player1 = s.player1.copy(xPos = s.player1.xPos + Player.rate)), ())
                case "ArrowUp"    =>
                  (s.copy(player1 = s.player1.copy(yPos = s.player1.yPos - Player.rate)), ())
                case "ArrowDown"  =>
                  (s.copy(player1 = s.player1.copy(yPos = s.player1.yPos + Player.rate)), ())
                case "ShiftLeft"  =>
                  (s.copy(player1 = s.player1.copy(bullets = Bullet(s.player1.xPos + Player.width/2 - Bullet.width/2, s.player1.yPos) :: s.player1.bullets)), ())
                case "ShiftRight" =>
                  (s.copy(player1 = s.player1.copy(bullets = Bullet(s.player1.xPos + Player.width/2 - Bullet.width/2, s.player1.yPos) :: s.player1.bullets)), ())
                case _ => (s, ())
              )
            yield ()
          )
        )
      }
    )

  def page(
    inputs: HtmlLabelElement[IO],
    canvas: HtmlCanvasElement[IO]
  ): Resource[IO, HtmlDivElement[IO]] =
    div(
      div(
        canvas
      ),
      div(
        inputs
      ),
    )

  def state: Resource[IO, SignallingRef[IO, GameState]] =
    SignallingRef[IO].of(
      GameState(
        player1 = Player(GameState.width/2 - Player.width/2, GameState.height - GameState.height/10, Nil),
        enemies = (0 to 10).toList.map(i => EnemySegment(GameState.width + EnemySegment.radius*2*i, EnemySegment.radius*2)),
        behaviors = List(Bullet.behavior, EnemySegment.behavior, GameState.behavior)
      )
    ).toResource

