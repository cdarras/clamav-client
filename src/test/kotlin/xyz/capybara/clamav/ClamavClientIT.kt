package xyz.capybara.clamav

import mu.KotlinLogging
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.data.MapEntry.entry
import org.junit.jupiter.api.*
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.concurrent.schedule
import kotlin.io.path.pathString

private val logger = KotlinLogging.logger {}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ClamavClientIT {

    companion object {
        const val EICAR_SIGNATURE = "Eicar-Signature"
        val LOCAL_EICAR_PATH: Path = Paths.get("test-data", "eicar.txt")
        val LOCAL_SAFE_FILE_PATH: Path = Paths.get("test-data", "safe-file.txt")
        val SERVER_TMP_PATH: Path = Paths.get("/tmp")
        val SERVER_EICAR_PATH: Path = SERVER_TMP_PATH.resolve(Paths.get("eicar.txt"))
        val SERVER_EICAR_COPY_PATH: Path = SERVER_TMP_PATH.resolve(Paths.get("eicar_copy.txt"))
        val SERVER_SAFE_FILE_PATH: Path = SERVER_TMP_PATH.resolve(Paths.get("safe-file.txt"))

        @Container
        @JvmStatic
        private val clamavContainer = KGenericContainer("clamav/clamav")
            .withLogConsumer { Slf4jLogConsumer(logger) }
            .withClasspathResourceMapping(LOCAL_EICAR_PATH.pathString, SERVER_EICAR_PATH.pathString, BindMode.READ_ONLY)
            .withClasspathResourceMapping(
                LOCAL_EICAR_PATH.pathString,
                SERVER_EICAR_COPY_PATH.pathString,
                BindMode.READ_ONLY
            )
            .withClasspathResourceMapping(
                LOCAL_SAFE_FILE_PATH.pathString,
                SERVER_SAFE_FILE_PATH.pathString,
                BindMode.READ_ONLY
            )
            .withExposedPorts(ClamavClient.DEFAULT_SERVER_PORT)

        @BeforeAll
        @JvmStatic
        fun initClamavClient() {
            val clamavServerHost = clamavContainer.host
            val clamavServerPort = clamavContainer.getMappedPort(ClamavClient.DEFAULT_SERVER_PORT)
            clamavClient = ClamavClient(clamavServerHost, clamavServerPort)
        }

        private lateinit var clamavClient: ClamavClient
    }

    @Test
    fun `should be able to ping a ClamAV server`() {
        assertDoesNotThrow {
            clamavClient.ping()
        }
    }

    @Test
    fun `should get the version of a ClamAV server`() {
        val version = assertDoesNotThrow {
            clamavClient.version()
        }

        // Then
        then(version).isNotEmpty
    }

    @Test
    fun `should get statistics from a ClamAV server`() {
        val stats = assertDoesNotThrow {
            clamavClient.stats()
        }

        // Then
        then(stats).isNotEmpty
    }

    @Test
    fun `should reload the virus databases of a ClamAV server`() {
        assertDoesNotThrow {
            clamavClient.reloadVirusDatabases()
        }
    }

    @Test
    @Order(Int.MAX_VALUE)
    fun `should be able to shut down a ClamAV server`() {
        assertDoesNotThrow {
            clamavClient.shutdownServer()
        }

        // Then
        Timer().schedule(5000) {
            then(clamavClient.isReachable()).isFalse
        }
    }

    @Test
    fun `should detect a virus (EICAR test file) in a content sent through an input stream to a ClamAV server`() {
        // Given
        val eicar = javaClass.getResource("/$LOCAL_EICAR_PATH").readBytes()

        val virusFound = assertDoesNotThrow {
            clamavClient.scan(eicar.inputStream())
        }

        // Then
        then(virusFound).isExactlyInstanceOf(ScanResult.VirusFound::class.java)
        with(virusFound as ScanResult.VirusFound) {
            then(this.foundViruses).containsOnly(entry("stream", listOf(EICAR_SIGNATURE)))
        }
    }

    @Test
    fun `should detect no viruses in a safe content sent through an input stream to a ClamAV server`() {
        // Given
        val safeContent = javaClass.getResource("/$LOCAL_SAFE_FILE_PATH").readBytes()

        val virusFound = assertDoesNotThrow {
            clamavClient.scan(safeContent.inputStream())
        }

        // Then
        then(virusFound).isExactlyInstanceOf(ScanResult.OK::class.java)
    }

    @Test
    fun `should detect a virus (EICAR test file) in a file copied to a ClamAV server`() {
        val virusFound = assertDoesNotThrow {
            clamavClient.scan(SERVER_EICAR_PATH)
        }

        // Then
        then(virusFound).isExactlyInstanceOf(ScanResult.VirusFound::class.java)
        with(virusFound as ScanResult.VirusFound) {
            then(this.foundViruses).containsOnly(entry(SERVER_EICAR_PATH.pathString, listOf(EICAR_SIGNATURE)))
        }
    }

    @Test
    fun `should detect no viruses in a safe file copied to a ClamAV server`() {
        val virusFound = assertDoesNotThrow {
            clamavClient.scan(SERVER_SAFE_FILE_PATH)
        }
        // Then
        then(virusFound).isExactlyInstanceOf(ScanResult.OK::class.java)
    }

    @Test
    fun `should detect the first found virus (EICAR test file) in a directory copied to a ClamAV server`() {
        val virusFound = assertDoesNotThrow {
            clamavClient.scan(SERVER_TMP_PATH)
        }

        // Then
        then(virusFound).isExactlyInstanceOf(ScanResult.VirusFound::class.java)
        with(virusFound as ScanResult.VirusFound) {
            then(this.foundViruses).containsOnly(entry(SERVER_EICAR_PATH.pathString, listOf(EICAR_SIGNATURE)))
        }
    }

    @Test
    fun `should detect all viruses (EICAR test files) in a directory copied to a ClamAV server`() {
        val virusFound = assertDoesNotThrow {
            clamavClient.scan(SERVER_TMP_PATH, continueScan = true)
        }

        // Then
        then(virusFound).isExactlyInstanceOf(ScanResult.VirusFound::class.java)
        with(virusFound as ScanResult.VirusFound) {
            then(this.foundViruses).containsExactlyInAnyOrderEntriesOf(
                mapOf(
                    SERVER_EICAR_PATH.pathString to listOf(EICAR_SIGNATURE),
                    SERVER_EICAR_COPY_PATH.pathString to listOf(EICAR_SIGNATURE)
                )
            )
        }
    }

    @Test
    fun `should detect all viruses (EICAR test files) in a directory copied to a ClamAV server (parallel scan)`() {
        val virusFound = assertDoesNotThrow {
            clamavClient.parallelScan(SERVER_TMP_PATH)
        }

        // Then
        then(virusFound).isExactlyInstanceOf(ScanResult.VirusFound::class.java)
        with(virusFound as ScanResult.VirusFound) {
            then(this.foundViruses).containsExactlyInAnyOrderEntriesOf(
                mapOf(
                    SERVER_EICAR_PATH.pathString to listOf(EICAR_SIGNATURE),
                    SERVER_EICAR_COPY_PATH.pathString to listOf(EICAR_SIGNATURE)
                )
            )
        }
    }

    class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)


}
