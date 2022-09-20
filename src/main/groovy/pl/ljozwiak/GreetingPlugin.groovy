package pl.ljozwiak

import org.gradle.api.Plugin
import org.gradle.api.Project

class GreetingPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def extension = project.extensions.create('greeting', GreetingPluginExtension)

    project.task('hello') {
      doLast {
        println "${extension.message.get()} from ${extension.greeter.get()}"
      }
    }
  }
}