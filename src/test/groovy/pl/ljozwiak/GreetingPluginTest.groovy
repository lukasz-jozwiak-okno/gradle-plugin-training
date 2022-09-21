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
    settingsFile << """
      rootProject.name = 'hello-world'
      include 'subproject'
    """
    new File("${testProjectDir}/subproject").mkdir()
    new File("${testProjectDir}/subproject", "build.gradle").createNewFile()
    buildFile = new File(testProjectDir, "build.gradle")
  }

  @Test
  void testHelloWorldTask() throws IOException {
    buildFile << """
        task helloWorld {
            doLast {
                println 'Hello world!'
            }
        }"""

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("helloWorld")
        .build()

    Assertions.assertTrue(result.output.contains("Hello world!"))
    Assertions.assertEquals(SUCCESS, result.task(":helloWorld").outcome)
  }

  @Test
  void testJacocoPlugin() throws IOException {
    buildFile << """
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

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .build()

    Assertions.assertTrue(result.tasks.any { it.path == ":jacocoTestReport" })
    Assertions.assertEquals(SKIPPED, result.task(":jacocoTestReport").outcome)
  }

  @Test
  void shouldNotCreateJacocoTask() throws IOException {
    buildFile << """
        plugins {
          id 'java'
          id 'jacoco'
        }   
        """

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .build()

    Assertions.assertFalse(result.tasks.any() { it.path == ":jacocoTestReport" })
  }

  @Test
  void shouldRunHelloTask() throws IOException {
    buildFile << """
        plugins {
          id 'pl.ljozwiak.greeting'
        }   
        
        greeting {
          message = 'Hello'
          greeter = 'Poland'
        }
        """

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("hello")
        .withPluginClasspath()
        .build()

    Assertions.assertTrue(result.output.contains("Hello from Poland"))
    Assertions.assertEquals(SUCCESS, result.task(":hello").outcome)
  }

  @Test
  void shouldRunSubprojectTaskByDefault() throws IOException {
    buildFile << """
        plugins {
          id 'pl.ljozwiak.greeting'
        }   
        
        greeting {
          message = 'Hello'
          greeter = 'Poland'
        }
        """

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .withPluginClasspath()
        .build()

    Assertions.assertTrue(result.output.contains(":jacocoTestReport"))
  }

  @Test
  void shouldRunSubprojectTask() throws IOException {
    buildFile << """
        plugins {
          id 'pl.ljozwiak.greeting'
        }   
        
        greeting {
          message = 'Hello'
          greeter = 'Poland'
        }
        
        subprojects {
          sub {
            jacoco = true
          }
        }
        """

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .withPluginClasspath()
        .build()

    Assertions.assertTrue(result.output.contains(":jacocoTestReport"))
  }

  @Test
  void shouldNotRunSubprojectTask() throws IOException {
    buildFile << """
        plugins {
          id 'pl.ljozwiak.greeting'
        }   
        
        greeting {
          message = 'Hello'
          greeter = 'Poland'
        }
        
        subprojects {
          sub {
            jacoco = false
          }
        }
        """

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .withPluginClasspath()
        .build()

    Assertions.assertFalse(result.output.contains(":jacocoTestReport"))
  }

  @Test
  void shouldNotRunSubprojectTaskWhenConfiguredOnRootLevel() throws IOException {
    buildFile << """
        plugins {
          id 'pl.ljozwiak.greeting'
        }   
        
        greeting {
          message = 'Hello'
          greeter = 'Poland'
        }
         
        sub {
          jacoco = false
        }
        """

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("build")
        .withPluginClasspath()
        .build()

    Assertions.assertFalse(result.output.contains(":jacocoTestReport"))
  }

  @Test
  void greeterPluginAddsGreetingTaskToProject() {
    Project project = ProjectBuilder.builder().build()
    project.pluginManager.apply("pl.ljozwiak.greeting")

    Assertions.assertTrue(project.pluginManager.hasPlugin("pl.ljozwiak.greeting"))
    Assertions.assertTrue(project.getTasks().any({ it.name == "hello" }))
  }
}
