# Akka Contrib 

A not very interesting library, but it helps not to repeat myself when codding [akka](https://akka.io/) and [akka http](https://doc.akka.io/docs/akka-http/current/).
It helps me to setup [kamon library](https://github.com/kamon-io/Kamon) using `akka extensios`. 
And it also (ab)use same functionality to expose `stream materializer` (as a singleton) to actor tree. 

## Usage

Library is published to [bintray](https://bintray.com) to use it  add `sbt-bintray` plugin to `plugins.sbt`

```
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
```

Add bintray resolver to `build.sbt'

```
resolvers += Resolver.bintrayRepo("milenkovicm", "maven")
```

Add the dependency

```
"com.github.milenkovicm" %% "akka-contrib" % "0.1.0"
```

## Setup Monitoring 

Akka extension to start up [kamon](https://github.com/kamon-io/Kamon) monitoring.

```hocon

akka {
  extensions = [com.github.milenkovicm.akka.ActorMaterializerExtension]
}

akka-contrib.monitoring.enable = true

kamon {
  reporters = [kamon.prometheus.PrometheusReporter, kamon.jaeger.JaegerReporter]
  jaeger {
    //host = "localhost"
    //port = 14268
    host = ${?MONITORING_JEAGER_HOST}
    port = ${?MONITORING_JEAGER_PORT}
  }
  prometheus {
    embedded-server {
      hostname = "0.0.0.0"
      port = 9095
      port = ${?MONITORING_PROMETHEUS_PORT}
    }
  }
  akka-http {
    name-generator = "default"
  }
  akka {
    ask-pattern-timeout-warning = "lightweight"
  }
  trace {
    sampler = "always"
    sampler = ${?MONITORING_TRACE_SAMPLER}
  }
  environment {
    service = ${?MONITORING_ENV_HOST}
    service = ${?MARATHON_APP_ID}
    host = ${?MONITORING_ENV_HOST}
    host = ${?HOST}
    instance = ${?MONITORING_ENV_INSTANCE}
    instance = ${?MESOS_TASK_ID}
  }
  system-metrics {
    jvm {
      enabled = on
      enabled = ${?MONITORING_SYSTEM_JVM_ENABLED}
      hiccup-monitor {
        enabled = off
        enabled = ${?MONITORING_SYSTEM_JVM_HICCUP_ENABLED}
      }
    }
    host {
      enabled = off
      enabled = ${?MONITORING_SYSTEM_HOST_ENABLED}
      // refresh-interval = 1 second
      // context-switches-refresh-interval = 1 second
    }
  }
 }
```

## Setup Shared Materializer

Create and reuse stream `materializer` across akka application:

```scala
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.milenkovicm.akka.GlobalMaterializerExtension

implicit val system: ActorSystem          = ActorSystem()
implicit val materializer: Materializer   = GlobalMaterializerExtension(system).materializer
```


## Release 

Released using `sbt-git`

```
git tag v0.1.0
sbt release
```
## License

This code is open source software licensed under the
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0) license.
