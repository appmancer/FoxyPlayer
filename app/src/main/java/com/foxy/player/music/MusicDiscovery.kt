package com.foxy.player.music

// ===== COMPATIBILITY LAYER FOR EXISTING IMPORTS =====
// This file maintains backward compatibility while delegating to new domain modules

import com.foxy.player.music.business.AudioFileScanner
import com.foxy.player.music.business.RealAudioFileScanner
import com.foxy.player.music.network.DefaultFolderListingStrategy
import com.foxy.player.music.network.MusicDiscoveryService
import com.foxy.player.music.network.PCloudFolderListingStrategy
import com.foxy.player.music.utils.MusicLibraryScanProgressService
import com.foxy.player.music.utils.MusicLibraryUtils

// Re-export key classes for backward compatibility
typealias MusicDiscoveryServiceAlias = MusicDiscoveryService
typealias PCloudFolderListingStrategyAlias = PCloudFolderListingStrategy
typealias DefaultFolderListingStrategyAlias = DefaultFolderListingStrategy
typealias AudioFileScannerAlias = AudioFileScanner
typealias RealAudioFileScannerAlias = RealAudioFileScanner
typealias MusicLibraryScanProgressServiceAlias = MusicLibraryScanProgressService

// Direct class exports for backward compatibility
val MusicLibraryScanProgressService = com.foxy.player.music.utils.MusicLibraryScanProgressService::class
val RealAudioFileScanner = com.foxy.player.music.business.RealAudioFileScanner::class

// Re-export utility functions for backward compatibility
fun extractBaseName(path: String): String = MusicLibraryUtils.extractBaseName(path)
fun generatePathBasedFileList(
    path: String,
    extensions: List<String>,
    count: Int
): List<String> = MusicLibraryUtils.generatePathBasedFileList(
    path,
    extensions,
    count
)
fun detectAudioFormat(fileName: String): String = MusicLibraryUtils.detectAudioFormat(fileName)
fun parsePathForMetadata(filePath: String, fallbackFileName: String = "Unknown"): Triple<String, String, String> =
    MusicLibraryUtils.parsePathForMetadata(filePath, fallbackFileName)
