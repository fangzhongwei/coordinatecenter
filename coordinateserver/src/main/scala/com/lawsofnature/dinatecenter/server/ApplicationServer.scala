package com.lawsofnature.dinatecenter.server

import java.util

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice}
import com.lawsofnatrue.common.ice.ConfigHelper
import com.lawsofnature.common.rabbitmq.{RabbitmqConsumerTemplate, RabbitmqConsumerTemplateImpl}
import com.lawsofnature.common.redis.{RedisClientTemplate, RedisClientTemplateImpl}
import com.lawsofnature.dinatecenter.service.{CoordinateService, CoordinateServiceImpl}
import com.lawsofnature.member.client.{MemberClientService, MemberClientServiceImpl}
import org.slf4j.LoggerFactory

/**
  * Created by fangzhongwei on 2016/11/21.
  */
object ApplicationServer extends App {
  var logger = LoggerFactory.getLogger(this.getClass)

  private val injector = Guice.createInjector(new AbstractModule() {
    override def configure() {
      val map: util.HashMap[String, String] = ConfigHelper.configMap
      Names.bindProperties(binder(), map)
      bind(classOf[RabbitmqConsumerTemplate]).to(classOf[RabbitmqConsumerTemplateImpl]).asEagerSingleton()
      bind(classOf[RedisClientTemplate]).to(classOf[RedisClientTemplateImpl]).asEagerSingleton()
      bind(classOf[MemberClientService]).to(classOf[MemberClientServiceImpl]).asEagerSingleton()
      bind(classOf[CoordinateService]).to(classOf[CoordinateServiceImpl]).asEagerSingleton()
    }
  })

  private val consumerTemplate: RabbitmqConsumerTemplate = injector.getInstance(classOf[RabbitmqConsumerTemplate])
  consumerTemplate.connect
  consumerTemplate.startConsume("queue-game-wait-1000", injector.getInstance(classOf[CoordinateService]))
  consumerTemplate.startConsume("queue-game-wait-2000", injector.getInstance(classOf[CoordinateService]))
  consumerTemplate.startConsume("queue-game-wait-4000", injector.getInstance(classOf[CoordinateService]))
  consumerTemplate.startConsume("queue-game-wait-8000", injector.getInstance(classOf[CoordinateService]))
  consumerTemplate.startConsume("queue-game-wait-20000", injector.getInstance(classOf[CoordinateService]))
}
