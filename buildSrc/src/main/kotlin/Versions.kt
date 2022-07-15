import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version.
 */
object Versions {
  const val org_jetbrains_kotlinx_kotlinx_serialization: String = "1.0-M1-1.4.0-rc"

  const val org_jetbrains_kotlinx_kotlinx_coroutines: String = "1.3.9" // available: "1.3.9"

  const val org_apache_logging_log4j: String = "2.13.3" // available: "2.13.3"

  const val org_jetbrains_kotlin: String = "1.4.10" // available: "1.4.0"

  const val com_google_flogger: String = "0.5.1" // available: "0.5.1"

  const val org_aeonbits_owner: String = "1.0.12" // available: "1.0.12"

  const val com_typesafe: String = "1.4.0"

  const val io_netty: String = "4.1.51.Final" // available: "4.1.51.Final"

  const val io_vertx: String = "3.9.2" // available: "3.9.2"

  const val net_sf_trove4j_core: String = "3.1.0"

  const val de_fayard_buildsrcversions_gradle_plugin: String = "0.7.0"

  const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.4.10" // available: "1.4.0"

  const val net_ltgt_errorprone_gradle_plugin: String = "1.2.1" // available: "1.2.1"

  const val digitalascent_errorprone_flogger: String = "0.8.1"

  const val org_eclipse_jdt_compiler_tool: String = "1.2.900" // available: "1.2.900"

  const val org_eclipse_jdt_compiler_apt: String = "1.3.1000" // available: "1.3.1000"

  const val commons_collections4: String = "4.4"

  const val velocity_engine_core: String = "2.2" // available: "2.2"

  const val mariadb_java_client: String = "2.6.2" // available: "2.6.2"

  const val juniversalchardet: String = "2.3.2" // available: "2.3.2"

  const val error_prone_core: String = "2.4.0" // available: "2.4.0"

  const val htmlcompressor: String = "1.7.1" // available: "1.7.1"

  const val commons_lang3: String = "3.11" // available: "3.11"

  const val roaringbitmap: String = "0.9.1" // available: "0.9.1"

  const val commons_text: String = "1.9" // available: "1.9"

  const val jaxb_runtime: String = "2.4.0-b180830.0438"

  const val activation: String = "1.1.1"

  const val commons_io: String = "2.7" // available: "2.7"

  const val cron_utils: String = "9.1.1" // available: "9.1.1"

  const val disruptor: String = "3.4.2"

  const val jaxb_core: String = "2.3.0.1"

  const val jaxb_impl: String = "2.3.3" // available: "2.3.3"

  const val slf4j_api: String = "2.0.0-alpha1"

  const val caffeine: String = "2.8.5" // available: "2.8.5"

  const val hikaricp: String = "3.4.5" // available: "3.4.5"

  const val jaxb_api: String = "2.4.0-b180830.0359"

  const val exposed: String = "0.17.7" // available: "0.17.7"

  const val dom4j: String = "2.1.3" // available: "2.1.3"

  const val guava: String = "28.1-jre"

  const val javac: String = "9+181-r4173-1"

  const val jdom2: String = "2.0.6"

  const val gson: String = "2.8.6" // available: "2.8.6"

  const val kaml: String = "0.22.0" // available: "0.21.0"

  /**
   * Current version: "5.6.2"
   * See issue 19: How to update Gradle itself?
   * https://github.com/jmfayard/buildSrcVersions/issues/19
   */
  const val gradleLatestVersion: String = "6.6.1"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
  inline get() =
    id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
