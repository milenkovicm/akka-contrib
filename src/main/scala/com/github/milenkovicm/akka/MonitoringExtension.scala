/*
 * Copyright 2018 Marko Milenkovic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.milenkovicm.akka

import akka.actor.{ ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import kamon.Kamon
import kamon.system.SystemMetrics

private[akka] class MonitoringExtension(system: ExtendedActorSystem)
    extends Extension
    with LazyLogging {
  logger.debug("Monitoring&Tracing extension has been activated.")

  implicit val config: Config = system.settings.config
  private val enable          = config.getBoolean("akka-contrib.monitoring.enable")

  if (isAspectJAgentLoaded && enable) {
    start()

    system.registerOnTermination(() ⇒ {
      stop()
    })

  } else if (enable && !isAspectJAgentLoaded) {
    logger.warn("Monitoring and tracing enabled but AspectjWeaver JVM Agent is NOT loaded.")
  } else if (!enable) {
    logger.info("Monitoring and tracing available but DISABLED.")
    logger.debug("Is AspectjWeaver JVM Agent loaded? [{}]", isAspectJAgentLoaded)
  }

  def isAspectJAgentLoaded: Boolean = {
    try org.aspectj.weaver.loadtime.Agent.getInstrumentation
    catch {
      case _: NoClassDefFoundError | _: UnsupportedOperationException ⇒
        return false
    }
    true
  }

  def start()(implicit config: Config): Unit = {

    Kamon.reconfigure(config) // otherwise will load application.conf
    SystemMetrics.startCollecting()
    Kamon.loadReportersFromConfig()

    logger.info("Monitoring and tracing STARTED.")
  }

  def stop(): Unit = {
    Kamon.stopAllReporters()
    SystemMetrics.stopCollecting()
    logger.info("Monitoring and tracing STOPPED.")
  }

}

object MonitoringExtension extends ExtensionId[MonitoringExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): MonitoringExtension =
    new MonitoringExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = MonitoringExtension
}
