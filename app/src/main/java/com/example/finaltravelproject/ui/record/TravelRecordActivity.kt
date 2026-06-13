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
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar

class TravelRecordActivity : AppCompatActivity() {

    private lateinit var dbHelper: TravelDBHelper

    private var recordNo: Int = -1
    private var currentPhotoUri: String? = null
    // 시작일과 종료일을 따로 저장할 변수
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""

    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            currentPhotoUri = uri.toString()
            findViewById<ImageView>(R.id.iv_thumbnail).setImageURI(uri)
        } else {
            Toast.makeText(this, "사진 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_record)

        dbHelper = TravelDBHelper(this)

        val etPlace = findViewById<EditText>(R.id.et_place)
        val etStartDate = findViewById<EditText>(R.id.et_start_date)
        val etEndDate = findViewById<EditText>(R.id.et_end_date)
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
                etMemo.setText(it.memo)
                currentPhotoUri = it.photoUri

                // 시작일, 종료일 변수 복구 및 화면 표시
                selectedStartDate = it.startDate
                selectedEndDate = it.endDate
                etStartDate.setText(selectedStartDate)
                etEndDate.setText(selectedEndDate)

                if (!currentPhotoUri.isNullOrEmpty()) {
                    try {
                        ivThumbnail.setImageURI(Uri.parse(currentPhotoUri))
                    } catch (e: Exception) {
                        ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }
        }

        // 시작일 칸 클릭 이벤트 (달력 호출)
        etStartDate.setOnClickListener {
            showSingleDatePicker("여행 시작일 선택") { date ->
                selectedStartDate = date
                etStartDate.setText(date)
            }
        }

        // 종료일 칸 클릭 이벤트 (달력 호출)
        etEndDate.setOnClickListener {
            showSingleDatePicker("여행 종료일 선택") { date ->
                selectedEndDate = date
                etEndDate.setText(date)
            }
        }

        btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnSave.setOnClickListener {
            val place = etPlace.text.toString().trim()
            val memo = etMemo.text.toString().trim()

            // 필수값 검증 (장소, 시작일, 종료일 모두 있는지 확인)
            if (place.isEmpty() || selectedStartDate.isEmpty() || selectedEndDate.isEmpty()) {
                Toast.makeText(this, "장소와 여행 기간을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (recordNo == -1) {
                val newRecord = TravelRecord(
                    place = place,
                    startDate = selectedStartDate,
                    endDate = selectedEndDate,
                    memo = memo.ifEmpty { null },
                    photoUri = currentPhotoUri
                )
                dbHelper.insertRecord(newRecord)
                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val updateRecord = TravelRecord(
                    no = recordNo,
                    place = place,
                    startDate = selectedStartDate,
                    endDate = selectedEndDate,
                    memo = memo.ifEmpty { null },
                    photoUri = currentPhotoUri
                )
                dbHelper.updateRecord(updateRecord)
                Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
    // 단일 달력을 띄워주는 재사용 가능한 함수
    private fun showSingleDatePicker(title: String, onDateSelected: (String) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            onDateSelected(format.format(java.util.Date(selection)))
        }
        datePicker.show(supportFragmentManager, "SINGLE_DATE_PICKER")
    }
}