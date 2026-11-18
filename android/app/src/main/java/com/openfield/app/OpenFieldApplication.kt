package com.openfield.app

import android.app.Application
import com.openfield.app.data.local.OpenFieldDatabase
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.engine.LiteRtLmEngine
import com.openfield.app.feature.ai.model.ModelDownloadManager
import com.openfield.app.feature.ai.model.ModelRepository
import com.openfield.app.feature.ai.model.ModelStorageManager
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class OpenFieldApplication : Application() {

    lateinit var database: OpenFieldDatabase
        private set

    lateinit var modelRepository: ModelRepository
        private set

    lateinit var aiEngine: LiteRtLmEngine
        private set

    lateinit var repository: OpenFieldRepository
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            PDFBoxResourceLoader.init(this)
        } catch (ignored: Exception) {}

        database = OpenFieldDatabase.getInstance(this)
        val storageManager = ModelStorageManager(this)
        val downloadManager = ModelDownloadManager(storageManager)
        modelRepository = ModelRepository(this, storageManager, downloadManager)
        aiEngine = LiteRtLmEngine()

        repository = OpenFieldRepository.initialize(
            context = this,
            database = database,
            modelRepository = modelRepository,
            aiEngine = aiEngine
        )
    }
}
