package com.example.youtubeaudioextractor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etYoutubeUrl = findViewById<EditText>(R.id.etYoutubeUrl)
        val btnDownload = findViewById<Button>(R.id.btnDownload)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnOpenFolder = findViewById<Button>(R.id.btnOpenFolder)

        btnDownload.setOnClickListener {
            val url = etYoutubeUrl.text.toString()
            if (url.isBlank()) {
                Toast.makeText(this, "링크를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 권한 체크 (Android 10 이하에서는 쓰기 권한 필요)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
                return@setOnClickListener
            }

            // 공용 Download 폴더 내의 '유튜브 음원추출' 폴더
            val publicDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDownloadDir = File(publicDownloadDir, "유튜브 음원추출")
            
            if (!appDownloadDir.exists()) {
                appDownloadDir.mkdirs()
            }

            val finalDownloadDir = appDownloadDir

            // 버튼 상태 초기화
            btnDownload.isEnabled = false
            btnOpenFolder.visibility = View.GONE
            tvStatus.text = "추출 준비 중..."

            // 백그라운드 스레드(IO)에서 다운로드 실행
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = YoutubeDLRequest(url)
                    // 최고 음질의 오디오만 추출하여 mp3로 변환
                    request.addOption("-f", "bestaudio")
                    request.addOption("--extract-audio")
                    request.addOption("--audio-format", "mp3")
                    request.addOption("-o", "${finalDownloadDir.absolutePath}/%(title)s.%(ext)s")

                    // 다운로드 실행 및 진행률 업데이트
                    YoutubeDL.getInstance().execute(request) { progress, _, _ ->
                        // UI 업데이트는 반드시 Main 스레드에서 해야 함
                        CoroutineScope(Dispatchers.Main).launch {
                            progressBar.progress = progress.toInt()
                            tvStatus.text = "진행률: ${progress.toInt()}%"
                        }
                    }

                    // 완료 시 UI 업데이트
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "다운로드 완료!\n저장 경로: ${finalDownloadDir.absolutePath}"
                        btnDownload.isEnabled = true
                        btnOpenFolder.visibility = View.VISIBLE
                        
                        btnOpenFolder.setOnClickListener {
                            val uri = FileProvider.getUriForFile(
                                this@MainActivity,
                                "${applicationContext.packageName}.fileprovider",
                                finalDownloadDir
                            )
                            
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "vnd.android.document/directory") // 폴더임을 명시적으로 지정
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            
                            try {
                                val chooser = Intent.createChooser(intent, "폴더를 열 앱을 선택하세요")
                                startActivity(chooser)
                            } catch (e: Exception) {
                                // 모든 시도가 실패할 경우 토스트 메시지
                                Toast.makeText(this@MainActivity, "폴더를 열 수 있는 앱이 없습니다. 직접 '내 파일' 앱 등에서 확인해주세요.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    // 에러 발생 시 UI 업데이트
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "오류 발생: ${e.message}"
                        btnDownload.isEnabled = true
                    }
                }
            }
        }
    }
}