package com.example.finaltravelproject.ui.record

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finaltravelproject.R
import com.example.finaltravelproject.data.local.TravelDBHelper
import com.example.finaltravelproject.domain.model.TravelRecord

class TravelRecordActivity : AppCompatActivity() {

    private lateinit var dbHelper: TravelDBHelper

    private var recordNo: Int = -1
    private var currentPhotoUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_record)

        dbHelper = TravelDBHelper(this)

        val etPlace = findViewById<EditText>(R.id.et_place)
        val etVisitDate = findViewById<EditText>(R.id.et_visit_date)
        val etMemo = findViewById<EditText>(R.id.et_memo)
        val ivThumbnail = findViewById<ImageView>(R.id.iv_thumbnail)
        val btnSelectPhoto = findViewById<Button>(R.id.btn_select_photo)
        val btnSave = findViewById<Button>(R.id.btn_save)

        recordNo = intent.getIntExtra("RECORD_NO", -1)

        // 기존 데이터 바인딩 (수정 모드)
        if (recordNo != -1) {
            val record = dbHelper.getAllRecords().find { it.no == recordNo }
            record?.let {
                etPlace.setText(it.place)
                etVisitDate.setText(it.visitDate)
                etMemo.setText(it.memo)
                currentPhotoUri = it.photoUri

                if (!currentPhotoUri.isNullOrEmpty()) {
                    try {
                        ivThumbnail.setImageURI(Uri.parse(currentPhotoUri))
                    } catch (e: Exception) {
                        ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }
        }

        btnSelectPhoto.setOnClickListener {
            // TODO: 사진 선택 Intent (카메라/갤러리)
            Toast.makeText(this, "사진 선택 기능 추가 예정", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            val place = etPlace.text.toString().trim()
            val visitDate = etVisitDate.text.toString().trim()
            val memo = etMemo.text.toString().trim()

            if (place.isEmpty() || visitDate.isEmpty()) {
                Toast.makeText(this, "장소와 방문 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (recordNo == -1) {
                val newRecord = TravelRecord(
                    place = place,
                    visitDate = visitDate,
                    memo = memo.ifEmpty { null },
                    photoUri = currentPhotoUri
                )
                dbHelper.insertRecord(newRecord)
                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val updateRecord = TravelRecord(
                    no = recordNo,
                    place = place,
                    visitDate = visitDate,
                    memo = memo.ifEmpty { null },
                    photoUri = currentPhotoUri
                )
                dbHelper.updateRecord(updateRecord)
                Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
}