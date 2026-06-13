package com.example.finaltravelproject.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.finaltravelproject.domain.model.TravelRecord

// Room 라이브러리를 사용하지 않고 SQLiteOpenHelper를 상속받아 구현
// 앱 종료 후에도 데이터가 유지되도록 설계
class TravelDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // DB 기본 정보
        private const val DATABASE_NAME = "TravelRecord.db"
        private const val DATABASE_VERSION = 2

        // 테이블 및 컬럼 이름 상수화
        const val TABLE_NAME = "travel_record"
        const val COLUMN_NO = "no"
        const val COLUMN_PLACE = "place"
        const val COLUMN_START_DATE = "start_date"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMN_MEMO = "memo"
        const val COLUMN_PHOTO_URI = "photo_uri"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                `$COLUMN_NO` INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PLACE TEXT NOT NULL,
                $COLUMN_START_DATE TEXT NOT NULL,
                $COLUMN_END_DATE TEXT NOT NULL,
                $COLUMN_MEMO TEXT,
                $COLUMN_PHOTO_URI TEXT
            )
        """.trimIndent()

        try {
            db?.execSQL(createTableQuery)
            Log.d("TravelDBHelper", "테이블 생성 성공: $TABLE_NAME")
        } catch (e: Exception) {
            // 앱의 비정상 종료를 막기 위한 기본 예외 처리
            Log.e("TravelDBHelper", "테이블 생성 중 오류 발생", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "DB 업그레이드 중 오류 발생", e)
        }
    }

    // ==========================================
    // CRUD 메서드 구현

    // Create (Insert)
    fun insertRecord(record: TravelRecord): Long {
        var result: Long = -1
        try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_PLACE, record.place)
                put(COLUMN_START_DATE, record.startDate)
                put(COLUMN_END_DATE, record.endDate)
                put(COLUMN_MEMO, record.memo)
                put(COLUMN_PHOTO_URI, record.photoUri)
            }
            // insert 성공 시 삽입된 행의 ID 반환, 실패 시 -1 반환
            result = db.insert(TABLE_NAME, null, values)
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "데이터 삽입 중 오류 발생", e)
        }
        return result
    }

    // Read (Select All)
    fun getAllRecords(): List<TravelRecord> {
        val recordList = mutableListOf<TravelRecord>()
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY `$COLUMN_NO` DESC" // 최신순 정렬
        var cursor: Cursor? = null

        try {
            val db = this.readableDatabase
            cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                val noIndex = cursor.getColumnIndexOrThrow(COLUMN_NO)
                val placeIndex = cursor.getColumnIndexOrThrow(COLUMN_PLACE)
                val startDateIndex = cursor.getColumnIndexOrThrow(COLUMN_START_DATE)
                val endDateIndex = cursor.getColumnIndexOrThrow(COLUMN_END_DATE)
                val memoIndex = cursor.getColumnIndexOrThrow(COLUMN_MEMO)
                val photoIndex = cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI)

                do {
                    val record = TravelRecord(
                        no = cursor.getInt(noIndex),
                        place = cursor.getString(placeIndex),
                        startDate = cursor.getString(startDateIndex),
                        endDate = cursor.getString(endDateIndex),
                        memo = cursor.getString(memoIndex),
                        photoUri = cursor.getString(photoIndex)
                    )
                    recordList.add(record)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "전체 데이터 조회 중 오류 발생", e)
        } finally {
            // 메모리 누수를 방지하기 위해 Cursor 해제
            cursor?.close()
        }
        return recordList
    }

    // Update
    fun updateRecord(record: TravelRecord): Int {
        var result = 0
        try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_PLACE, record.place)
                put(COLUMN_START_DATE, record.startDate)
                put(COLUMN_END_DATE, record.endDate)
                put(COLUMN_MEMO, record.memo)
                put(COLUMN_PHOTO_URI, record.photoUri)
            }
            // update 성공 시 영향을 받은 행의 개수 반환
            result = db.update(
                TABLE_NAME,
                values,
                "$COLUMN_NO = ?",
                arrayOf(record.no.toString())
            )
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "데이터 수정 중 오류 발생", e)
        }
        return result
    }

    // Delete
    fun deleteRecord(no: Int): Int {
        var result = 0
        try {
            val db = this.writableDatabase
            // delete 성공 시 영향을 받은 행의 개수 반환
            result = db.delete(
                TABLE_NAME,
                "$COLUMN_NO = ?",
                arrayOf(no.toString())
            )
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "데이터 삭제 중 오류 발생", e)
        }
        return result
    }
}