package pl.ljozwiak

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GreetingPluginTest {

  @TempDir
  File testProjectDir
  private File settingsFile
  private File buildFile

  @BeforeEach
  void setup() {
    settingsFile = new File(testProjectDir, "settings.gradle")
    buildFile = new File(testProjectDir, "build.gradle")
  }

  @Test
  void testHelloWorldTask() throws IOException {
    writeFile(settingsFile, "rootProject.name = 'hello-world'")
    String buildFileContent = """
        task helloWorld {
            doLast {
                println 'Hello world!'
            }
        }"""
    writeFile(buildFile, buildFileContent)

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("helloWorld")
        .build()

    Assertions.assertTrue(result.output.contains("Hello world!"))
    Assertions.assertEquals(SUCCESS, result.task(":helloWorld").outcome)
  }

  @Test
  void testJacocoPlugin() throws IOException {
    writeFile(settingsFile, "rootProject.name = 'hello-world'")
    String buildFileContent = """
        plugins {
          id 'java'
          id 'jacoco'
        }
        
        test {
            finalizedBy jacocoTestReport
        }
        jacocoTestReport {
            dependsOn test
        }        
        """
    writeFile(buildFile, buildFileContent)

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .build()

    Assertions.assertTrue(result.tasks.any { it.path == ":jacocoTestReport" })
    Assertions.assertEquals(SKIPPED, result.task(":jacocoTestReport").outcome)
  }

  @Test
  void shouldNotCreateJacocoTask() throws IOException {
    writeFile(settingsFile, "rootProject.name = 'hello-world'")
    String buildFileContent = """
        plugins {
          id 'java'
          id 'jacoco'
        }   
        """
    writeFile(buildFile, buildFileContent)

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .build()

    Assertions.assertFalse(result.tasks.any() { it.path == ":jacocoTestReport" })
  }

  @Test
  void shouldRunHelloTask() throws IOException {
    writeFile(settingsFile, "rootProject.name = 'hello-world'")
    String buildFileContent = """
        plugins {
          id 'pl.ljozwiak.greeting'
        }   
        
        greeting {
          message = 'Hello'
          greeter = 'Poland'
        }
        """
    writeFile(buildFile, buildFileContent)

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("hello")
        .withPluginClasspath()
        .build()

    Assertions.assertTrue(result.output.contains("Hello from Poland"))
    Assertions.assertEquals(SUCCESS, result.task(":hello").outcome)
  }

  static private void writeFile(File destination, String content) throws IOException {
    destination.write(content)
  }

  @Test
  void greeterPluginAddsGreetingTaskToProject() {
    Project project = ProjectBuilder.builder().build()
    project.pluginManager.apply("pl.ljozwiak.greeting")

    Assertions.assertTrue(project.pluginManager.hasPlugin("pl.ljozwiak.greeting"))
    Assertions.assertTrue(project.getTasks().any({ it.name == "hello" }))
  }
}
