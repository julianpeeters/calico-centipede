package calico.centipede.models

import calico.centipede.Render
import cats.syntax.all.given
import cats.data.State
import cats.effect.Sync
import org.scalajs.dom.CanvasRenderingContext2D

case class Bullet(xPos: Int, yPos: Int)
object Bullet:
  def width: Int = 2
  def height: Int = 4
  def rate: Int = 30
  def behavior: State[GameState, Unit] =
    State(s => (s.copy(player1 = s.player1.copy(bullets = s.player1.bullets.map(b => b.copy(yPos = b.yPos - rate)))), ()))
  given [F[_]: Sync]: Render.`2D`[F, Bullet] =
    new Render.`2D`[F, Bullet]:
      def render(obj: Bullet)(using Ctx: CanvasRenderingContext2D): F[Unit] =
        for
          _ <- Sync[F].delay(Ctx.fillStyle = "red")
          _ <- Sync[F].delay(Ctx.fillRect(obj.xPos, obj.yPos, Bullet.width, Bullet.height))
        yield ()

case class EnemySegment(xPos: Int, yPos: Int)
object EnemySegment:
  def radius: Int = 10
  def rate: Int = 7
  def behavior: State[GameState, Unit] =
    State(s => (s.copy(enemies = s.enemies.map(e => e.copy(xPos = e.xPos - rate))), ()))
  given [F[_]: Sync]: Render.`2D`[F, EnemySegment] =
    new Render.`2D`[F, EnemySegment]:
      def render(obj: EnemySegment)(using Ctx: CanvasRenderingContext2D): F[Unit] =
        for
          _ <- Sync[F].delay(Ctx.fillStyle = "green")
          _ <- Sync[F].delay(Ctx.beginPath())
          _ <- Sync[F].delay(Ctx.arc(obj.xPos, obj.yPos, EnemySegment.radius, 0, 2 * Math.PI))
          _ <- Sync[F].delay(Ctx.fill())
        yield ()

case class GameState(player1: Player, enemies: List[EnemySegment], behaviors: List[State[GameState, Unit]])
object GameState:
  def width: Int = 400
  def height: Int = 600
  def behavior: State[GameState, Unit] =
    State(state =>
      val hits: List[(EnemySegment, Bullet)] = 
        for
          e <- state.enemies
          b <- state.player1.bullets
          t <- if (b.xPos > e.xPos && b.xPos < (e.xPos + EnemySegment.radius * 2) &&
                   b.yPos > e.yPos && b.yPos < (e.yPos + EnemySegment.radius * 2))
               then List((e, b))
               else Nil
        yield t
      val newState: GameState = hits.foldLeft(state): (acc, x) =>
        acc.copy(
          enemies = state.enemies.filter(e => e != x._1),
          player1 = state.player1.copy(bullets = state.player1.bullets.filter(b => b != x._2))
        )
      (newState, ())
    )
  given [F[_]: Sync](using E: Render.`2D`[F, EnemySegment], P: Render.`2D`[F, Player]): Render.`2D`[F, GameState] =
    new Render.`2D`[F, GameState]:
      def render(obj: GameState)(using Ctx: CanvasRenderingContext2D): F[Unit] =
        for
          _ <- Sync[F].delay(Ctx.clearRect(0, 0, GameState.width, GameState.height))
          _ <- Sync[F].delay(Ctx.fillStyle = "beige")
          _ <- Sync[F].delay(Ctx.fillRect(0, 0, GameState.width, GameState.height))
          _ <- obj.enemies.traverse_(e => E.render(e))
          _ <- P.render(obj.player1)
        yield ()

case class Player(xPos: Int, yPos: Int, bullets: List[Bullet])
object Player:
  def width: Int = 12
  def height: Int = 12
  def rate: Int = 15
  given [F[_]: Sync](using R: Render.`2D`[F, Bullet]): Render.`2D`[F, Player] =
    new Render.`2D`[F, Player]:
      def render(obj: Player)(using Ctx: CanvasRenderingContext2D): F[Unit] =
        for
          _ <- Sync[F].delay(Ctx.fillStyle = "blue")
          _ <- Sync[F].delay(Ctx.fillRect(obj.xPos, obj.yPos, Player.width, Player.height))
          _ <- obj.bullets.traverse_(b => R.render(b))
        yield ()
