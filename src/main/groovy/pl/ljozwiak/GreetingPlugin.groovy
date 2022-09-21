package pl.ljozwiak

import org.gradle.api.Plugin
import org.gradle.api.Project

class GreetingPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def extension = project.extensions.create('greeting', GreetingPluginExtension)
    def subExtension = project.extensions.create('sub', GreetingPluginSubExtension)

    project.plugins.apply('java')
    project.plugins.apply('idea')


    project.task('hello') {
      doLast {
        println "${extension.message.get()} from ${extension.greeter.get()} ${subExtension.jacoco.get()}"
      }
    }

    println("Jacoco: ${subExtension.jacoco.get()}")

    project.afterEvaluate {
      applyToSubprojects(project, subExtension.jacoco.get())
    }
  }

  void applyToSubprojects(Project project, Boolean runJacoco) {

    println("Jacoco subprojects: ${runJacoco}")
    if (runJacoco) {
      project.plugins.apply('jacoco')

      project.test.finalizedBy project.jacocoTestReport
    }
  }
}