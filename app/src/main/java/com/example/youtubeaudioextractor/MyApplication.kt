package com.example.youtubeaudioextractor

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
            com.yausername.ffmpeg.FFmpeg.getInstance().init(this)
            
            // yt-dlp 바이너리 비동기 업데이트 (90일 경과 및 시그니처 오류 해결)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    YoutubeDL.getInstance().updateYoutubeDL(this@MyApplication)
                    Log.d("MyApplication", "yt-dlp 업데이트 완료")
                } catch (e: Exception) {
                    Log.e("MyApplication", "yt-dlp 업데이트 실패", e)
                }
            }
        } catch (e: YoutubeDLException) {
            Log.e("MyApplication", "초기화 실패", e)
        }
    }
}