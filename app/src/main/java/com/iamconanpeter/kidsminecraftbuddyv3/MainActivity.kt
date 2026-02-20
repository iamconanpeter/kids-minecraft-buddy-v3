package com.iamconanpeter.kidsminecraftbuddyv3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var progressManager: KidsMinecraftBuddyProgressManager
    private lateinit var engine: KidsMinecraftBuddyEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressManager = KidsMinecraftBuddyProgressManager(this)
        engine = KidsMinecraftBuddyEngine(progressManager.load())

        val gameView = KidsMinecraftBuddyView(
            context = this,
            engine = engine,
            onProgressChanged = { snapshot -> progressManager.save(snapshot) }
        )

        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        progressManager.save(engine.progressSnapshot())
    }
}
