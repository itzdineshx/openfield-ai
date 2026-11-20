package com.openfield.app

import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.entity.KnowledgePackArticleEntity
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.feature.library.PackZipInstaller
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PackZipInstallerTest {

    private class FakeKnowledgePackDao : KnowledgePackDao {
        val packs = mutableListOf<KnowledgePackEntity>()
        val articles = mutableListOf<KnowledgePackArticleEntity>()

        override fun getKnowledgePacksFlow(): Flow<List<KnowledgePackEntity>> = flowOf(packs)
        override suspend fun getKnowledgePacks(): List<KnowledgePackEntity> = packs.toList()
        override suspend fun getPackById(id: String): KnowledgePackEntity? = packs.find { it.id == id }

        override suspend fun insertAll(packs: List<KnowledgePackEntity>) {
            this.packs.removeAll { p -> packs.any { it.id == p.id } }
            this.packs.addAll(packs)
        }

        override suspend fun updateInstallation(id: String, isInstalled: Boolean) {
            val idx = packs.indexOfFirst { it.id == id }
            if (idx != -1) {
                packs[idx] = packs[idx].copy(isInstalled = isInstalled)
            }
        }

        override suspend fun deletePack(id: String) {
            packs.removeAll { it.id == id }
            articles.removeAll { it.packId == id }
        }

        override fun getArticlesForPackFlow(packId: String): Flow<List<KnowledgePackArticleEntity>> =
            flowOf(articles.filter { it.packId == packId })

        override suspend fun getArticlesForPack(packId: String): List<KnowledgePackArticleEntity> =
            articles.filter { it.packId == packId }

        override suspend fun getArticleById(id: String): KnowledgePackArticleEntity? =
            articles.find { it.id == id }

        override suspend fun insertArticles(articles: List<KnowledgePackArticleEntity>) {
            this.articles.removeAll { a -> articles.any { it.id == a.id } }
            this.articles.addAll(articles)
        }

        override suspend fun deleteArticlesForPack(packId: String) {
            articles.removeAll { it.packId == packId }
        }

        override suspend fun searchArticles(query: String): List<KnowledgePackArticleEntity> =
            articles.filter { it.title.contains(query, true) || it.content.contains(query, true) }

        override suspend fun getAllInstalledArticles(): List<KnowledgePackArticleEntity> =
            articles.toList()
    }

    private lateinit var dao: FakeKnowledgePackDao
    private lateinit var installer: PackZipInstaller

    @Before
    fun setUp() {
        dao = FakeKnowledgePackDao()
        installer = PackZipInstaller(dao)
    }

    private fun createZipArchive(entries: Map<String, String>): ByteArray {
        val byteStream = ByteArrayOutputStream()
        ZipOutputStream(byteStream).use { zip ->
            for ((name, content) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return byteStream.toByteArray()
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun installFromZipStream_validZipWithJsonArticles_installsSuccessfully() = runBlocking {
        val manifestJson = """
            {
                "pack_id": "embedded_systems",
                "title": "Embedded Systems & IoT",
                "category": "Technology",
                "version": "1.0.0",
                "size": "150 KB",
                "description": "Guides for microcontrollers, UART, I2C, and SPI.",
                "topics": ["Microcontrollers", "I2C", "SPI"]
            }
        """.trimIndent()

        val articlesJson = """
            [
                {
                    "id": "uart_protocol",
                    "title": "UART Communication Basics",
                    "section": "Protocols",
                    "content": "# UART Protocol\nUniversal Asynchronous Receiver-Transmitter explained with baud rate.",
                    "tags": ["UART", "Hardware"]
                },
                {
                    "id": "i2c_bus",
                    "title": "I2C Bus Diagnostics",
                    "section": "Protocols",
                    "content": "# I2C Bus\nTwo-wire serial interface with SDA and SCL lines.",
                    "tags": ["I2C", "Hardware"]
                }
            ]
        """.trimIndent()

        val zipBytes = createZipArchive(
            mapOf(
                "manifest.json" to manifestJson,
                "articles.json" to articlesJson
            )
        )

        val result = installer.installFromZipStream(ByteArrayInputStream(zipBytes))

        assertTrue("Expected installation to succeed, but was: $result", result.isSuccess)
        val installInfo = result.getOrThrow()
        assertEquals("embedded_systems", installInfo.packId)
        assertEquals("Embedded Systems & IoT", installInfo.title)
        assertEquals("1.0.0", installInfo.version)
        assertEquals(2, installInfo.articleCount)

        // Verify Room DAO state
        val savedPack = dao.getPackById("embedded_systems")
        assertNotNull(savedPack)
        assertEquals("Embedded Systems & IoT", savedPack?.title)
        assertTrue(savedPack?.isInstalled == true)

        val savedArticles = dao.getArticlesForPack("embedded_systems")
        assertEquals(2, savedArticles.size)
        assertEquals("UART Communication Basics", savedArticles[0].title)
        assertEquals("I2C Bus Diagnostics", savedArticles[1].title)
    }

    @Test
    fun installFromZipStream_validZipWithMarkdownFiles_extractsArticlesFromMarkdown() = runBlocking {
        val manifestJson = """
            {
                "pack_id": "solar_energy",
                "title": "Solar Energy Field Guide",
                "category": "Energy",
                "version": "1.0.0",
                "description": "Off-grid solar power design, MPPT controllers, and battery banks.",
                "topics": ["Solar", "MPPT", "Batteries"]
            }
        """.trimIndent()

        val mpptMarkdown = """
            # MPPT vs PWM Charge Controllers
            Maximum Power Point Tracking (MPPT) optimizes solar array voltage to battery charging voltage.
        """.trimIndent()

        val batteryMarkdown = """
            # LiFePO4 Battery Safety
            Lithium Iron Phosphate cells offer 3000+ cycles and superior thermal stability.
        """.trimIndent()

        val zipBytes = createZipArchive(
            mapOf(
                "manifest.json" to manifestJson,
                "articles/mppt_controllers.md" to mpptMarkdown,
                "articles/lifepo4_batteries.md" to batteryMarkdown
            )
        )

        val result = installer.installFromZipStream(ByteArrayInputStream(zipBytes))

        assertTrue("Expected markdown extraction to succeed, but was: $result", result.isSuccess)
        val installInfo = result.getOrThrow()
        assertEquals("solar_energy", installInfo.packId)
        assertEquals(2, installInfo.articleCount)

        val articles = dao.getArticlesForPack("solar_energy")
        assertEquals(2, articles.size)
        assertTrue(articles.any { it.title == "MPPT vs PWM Charge Controllers" })
        assertTrue(articles.any { it.title == "LiFePO4 Battery Safety" })
    }

    @Test
    fun installFromZipStream_sha256Verification_succeedsWhenChecksumMatches() = runBlocking {
        val manifestJson = """
            {
                "pack_id": "secure_pack",
                "title": "Secure Pack",
                "category": "Security",
                "version": "1.0.0",
                "description": "Integrity test pack",
                "topics": ["Security"]
            }
        """.trimIndent()

        val zipBytes = createZipArchive(mapOf("manifest.json" to manifestJson))
        val expectedHash = sha256(zipBytes)

        val result = installer.installFromZipStream(
            inputStream = ByteArrayInputStream(zipBytes),
            expectedSha256 = expectedHash
        )

        assertTrue("Verification should succeed with matching hash", result.isSuccess)
    }

    @Test
    fun installFromZipStream_sha256Verification_failsWhenChecksumMismatches() = runBlocking {
        val manifestJson = """
            {
                "pack_id": "tampered_pack",
                "title": "Tampered Pack",
                "category": "Security",
                "version": "1.0.0",
                "description": "Integrity test pack",
                "topics": ["Security"]
            }
        """.trimIndent()

        val zipBytes = createZipArchive(mapOf("manifest.json" to manifestJson))
        val wrongHash = "0000000000000000000000000000000000000000000000000000000000000000"

        val result = installer.installFromZipStream(
            inputStream = ByteArrayInputStream(zipBytes),
            expectedSha256 = wrongHash
        )

        assertTrue("Verification should fail with mismatched hash", result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is SecurityException)
        assertTrue(ex?.message?.contains("SHA-256") == true)
    }

    @Test
    fun installFromZipStream_pathTraversalAttempt_rejectedWithSecurityException() = runBlocking {
        val manifestJson = """
            {
                "pack_id": "exploit_pack",
                "title": "Exploit Pack",
                "category": "Security",
                "version": "1.0.0",
                "description": "Exploit test pack",
                "topics": ["Exploit"]
            }
        """.trimIndent()

        // Attempt Zip-Slip path traversal
        val zipBytes = createZipArchive(
            mapOf(
                "manifest.json" to manifestJson,
                "../../../../../../etc/passwd" to "root:x:0:0:root:/root:/bin/bash"
            )
        )

        val result = installer.installFromZipStream(ByteArrayInputStream(zipBytes))

        assertTrue("Zip-Slip attempt must fail", result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is SecurityException)
        assertTrue(ex?.message?.contains("Malicious path traversal") == true)
    }

    @Test
    fun installFromZipStream_missingManifest_failsWithIllegalArgumentException() = runBlocking {
        val zipBytes = createZipArchive(
            mapOf(
                "notes.txt" to "Random notes without manifest"
            )
        )

        val result = installer.installFromZipStream(ByteArrayInputStream(zipBytes))

        assertTrue("Missing manifest must fail", result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is IllegalArgumentException)
        assertTrue(ex?.message?.contains("manifest.json") == true)
    }

    @Test
    fun installFromZipStream_realFirstAidPack_installsWithSha256Verification() = runBlocking {
        val packFile = java.io.File("../../catalog/packs/first_aid-1.0.0.zip")
        if (packFile.exists()) {
            val bytes = packFile.readBytes()
            val expectedHash = com.openfield.app.feature.library.KnowledgePackManager.CURATED_CATALOG
                .find { it.packId == "first_aid" }!!.checksumSha256
            val result = installer.installFromZipStream(
                inputStream = ByteArrayInputStream(bytes),
                expectedSha256 = expectedHash
            )
            assertTrue(result.isSuccess)
            val installResult = result.getOrThrow()
            assertEquals("first_aid", installResult.packId)
            assertTrue(installResult.articleCount >= 4)
            val articles = dao.getArticlesForPack("first_aid")
            assertTrue(articles.any { it.title.contains("CPR") })
            assertTrue(articles.any { it.title.contains("Tourniquet") })
        }
    }
}
