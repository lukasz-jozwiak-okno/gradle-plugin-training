package pl.ljozwiak

import org.gradle.api.provider.Property

abstract class GreetingPluginSubExtension {
  abstract Property<Boolean> getJacoco()

  GreetingPluginSubExtension() {
    jacoco.convention(true)
  }
}
