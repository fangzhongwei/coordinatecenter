package com.lawsofnatrue.coordinateserver.test

import com.lawsofnature.dinatecenter.helper.CardsHelper

/**
  * Created by fangzhongwei on 2016/12/21.
  */
object ProjectTest extends App{
  val (player1CardsList, player2CardsList, player3CardsList, dzCardsList) = CardsHelper.initCards()
  println(player1CardsList.mkString(","))
}
