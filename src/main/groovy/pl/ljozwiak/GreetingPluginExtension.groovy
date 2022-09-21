package pl.ljozwiak

import org.gradle.api.provider.Property

abstract class GreetingPluginExtension {
  abstract Property<String> getMessage()

  abstract Property<String> getGreeter()
}
