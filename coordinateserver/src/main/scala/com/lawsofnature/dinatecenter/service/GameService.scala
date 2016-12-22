package com.lawsofnature.dinatecenter.service

import java.sql.Timestamp
import javax.inject.Inject

import RpcMember.MemberResponse
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.lawsofnature.common.redis.RedisClientTemplate
import com.lawsofnature.dinatecenter.enumnate.GameStatus
import com.lawsofnature.dinatecenter.helper.CardsHelper
import com.lawsofnature.dinatecenter.repo.{CoordinateRepository, TmGameRow, TmSeatRow}
import com.lawsofnature.gamecenter.noti.GameStart
import com.lawsofnature.member.client.MemberClientService
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.StringBuilder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

/**
  * Created by fangzhongwei on 2016/12/20.
  */
trait GameService {
  def initActor(): Future[Unit]

  def getOnlineMemberAkkaAddress(memberId: Long): Option[String]

  def createGame(traceId: String, gameType: Int, seat1: TmSeatRow, seat2: TmSeatRow, seat3: TmSeatRow)
}

class GameServiceImpl @Inject()(coordinateRepository: CoordinateRepository, redisClientTemplate: RedisClientTemplate, memberClientService: MemberClientService) extends GameService {
  private[this] val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private[this] val akkaAddressPre = "gc.ddz.mi-addr:"
  private[this] var actor: ActorRef = _

  implicit def gameStatus2Byte(gameStatus: GameStatus): Byte = gameStatus.getCode

  def notifyGameStart(traceId: String, memberId: Long, game: TmGameRow, gameIndex: Int, cards: List[Int], dzCards: List[Int], previousUsername: String, nextUsername: String): Unit = {
    actor ! GameStart(traceId, memberId, cards, dzCards, gameIndex == 1, previousUsername, nextUsername)
  }

  override def createGame(traceId: String, gameType: Int, seat1: TmSeatRow, seat2: TmSeatRow, seat3: TmSeatRow): Unit = {
    val memberId1 = seat1.memberId
    val memberId2 = seat2.memberId
    val memberId3 = seat3.memberId

    val (player1CardsList, player2CardsList, player3CardsList, dzCardsList) = CardsHelper.initCards()
    val gameId: Long = coordinateRepository.getNextGameId()
    val now = new Timestamp(System.currentTimeMillis())
    val cards1: String = player1CardsList.mkString(",")
    val cards2: String = player2CardsList.mkString(",")
    val cards3: String = player3CardsList.mkString(",")
    val dzCards: String = dzCardsList.mkString(",")
    val game: TmGameRow = TmGameRow(gameId, gameType, GameStatus.Playing, memberId1, memberId2, memberId3, cards1, cards2, cards3, dzCards, 1, now, now)
    coordinateRepository.createGame(game)

    val memberResponse1: MemberResponse = memberClientService.getMemberByMemberId(traceId, memberId1)
    val memberResponse2: MemberResponse = memberClientService.getMemberByMemberId(traceId, memberId2)
    val memberResponse3: MemberResponse = memberClientService.getMemberByMemberId(traceId, memberId3)

    notifyGameStart(seat1.traceId, memberId1, game, 1, player1CardsList, dzCardsList, memberResponse3.username, memberResponse2.username)
    notifyGameStart(seat2.traceId, memberId2, game, 2, player2CardsList, dzCardsList, memberResponse1.username, memberResponse3.username)
    notifyGameStart(seat3.traceId, memberId3, game, 3, player3CardsList, dzCardsList, memberResponse2.username, memberResponse1.username)
  }

  override def getOnlineMemberAkkaAddress(memberId: Long): Option[String] = {
    redisClientTemplate.getString(new StringBuilder(akkaAddressPre).append(memberId).toString)
  }

  override def initActor(): Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    Future {
      val system = ActorSystem()
      actor = system.actorOf(Props[CoordinateActor])
      logger.info("CoordinateActor startup.")
      promise.success()
    }
    promise.future
  }
}

class CoordinateActor(gameService: GameService) extends Actor {
  private[this] val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def receive: Receive = {
    case GameStart(traceId, memberId, cards, dzCards, isChooseDz, previousUsername, nextUsername) =>
      gameService.getOnlineMemberAkkaAddress(memberId) match {
        case Some(hostAndAddress) =>
          context.actorSelection(s"akka.tcp://DispatchSystem@$hostAndAddress/user/Dispatcher") ! GameStart(traceId, memberId, cards, dzCards, isChooseDz, previousUsername, nextUsername)
        case None => logger.error(s"member akka address not found, memberId:$memberId")
      }
    case any: Any => logger.info(s"receive:$any")
  }
}
