package com.example.finaltravelproject.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.finaltravelproject.domain.model.TravelRecord

// Room 안 쓰고 SQLiteOpenHelper로 직접 구현
// 앱 종료 후에도 데이터가 유지되도록 설계
class TravelDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // DB 기본 세팅
        private const val DATABASE_NAME = "TravelRecord.db"
        private const val DATABASE_VERSION = 3

        // 테이블이랑 컬럼명 상수화
        const val TABLE_NAME = "travel_record"
        const val COLUMN_NO = "no"
        const val COLUMN_PLACE = "place"
        const val COLUMN_START_DATE = "start_date"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMN_MEMO = "memo"
        const val COLUMN_PHOTO_URI = "photo_uri"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // 테이블 만들 때 좌표 컬럼 추가
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                `$COLUMN_NO` INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PLACE TEXT NOT NULL,
                $COLUMN_START_DATE TEXT NOT NULL,
                $COLUMN_END_DATE TEXT NOT NULL,
                $COLUMN_MEMO TEXT,
                $COLUMN_PHOTO_URI TEXT,
                $COLUMN_LATITUDE REAL,
                $COLUMN_LONGITUDE REAL
            )
        """.trimIndent()

        try {
            db?.execSQL(createTableQuery)
            Log.d("TravelDBHelper", "테이블 생성 성공: $TABLE_NAME")
        } catch (e: Exception) {
            // 앱 튕김 방지
            Log.e("TravelDBHelper", "테이블 생성 오류", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            // v3보다 낮으면 기존 데이터 안 날리고 좌표 컬럼만 추가
            if (oldVersion < 3) {
                db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_LATITUDE REAL")
                db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_LONGITUDE REAL")
            }
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "DB 업그레이드 오류", e)
        }
    }

    // ==========================================
    // CRUD 구현

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

                // 좌표값 있으면 같이 저장
                record.latitude?.let { put(COLUMN_LATITUDE, it) }
                record.longitude?.let { put(COLUMN_LONGITUDE, it) }
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
                val latIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)
                val lonIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)

                do {
                    // 좌표는 null일 수 있어서 isNull 체크 필수
                    val lat = if (cursor.isNull(latIndex)) null else cursor.getDouble(latIndex)
                    val lon = if (cursor.isNull(lonIndex)) null else cursor.getDouble(lonIndex)

                    val record = TravelRecord(
                        no = cursor.getInt(noIndex),
                        place = cursor.getString(placeIndex),
                        startDate = cursor.getString(startDateIndex),
                        endDate = cursor.getString(endDateIndex),
                        memo = cursor.getString(memoIndex),
                        photoUri = cursor.getString(photoIndex),
                        latitude = lat,
                        longitude = lon
                    )
                    recordList.add(record)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "조회 오류", e)
        } finally {
            // 메모리 누수 방지용 커서 닫기
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

                // 좌표 업데이트 (null이면 null로 덮어쓰기)
                record.latitude?.let { put(COLUMN_LATITUDE, it) } ?: putNull(COLUMN_LATITUDE)
                record.longitude?.let { put(COLUMN_LONGITUDE, it) } ?: putNull(COLUMN_LONGITUDE)
            }
            // 성공하면 바뀐 행 개수 반환
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

    // 전체 삭제
    fun deleteAllRecords(): Int {
        var result = 0
        try {
            val db = this.writableDatabase
            result = db.delete(TABLE_NAME, null, null)
        } catch (e: Exception) {
            Log.e("TravelDBHelper", "전체 삭제 오류", e)
        }
        return result
    }

    // 검색어 포함된 기록 찾기
    fun searchRecords(keyword: String): List<TravelRecord> {
        val recordList = mutableListOf<TravelRecord>()
        // %keyword% 형태로 검색어가 포함된 거 다 가져오기
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_PLACE LIKE ? ORDER BY `$COLUMN_NO` DESC"
        var cursor: Cursor? = null

        try {
            val db = this.readableDatabase
            cursor = db.rawQuery(selectQuery, arrayOf("%$keyword%"))

            if (cursor.moveToFirst()) {
                val noIndex = cursor.getColumnIndexOrThrow(COLUMN_NO)
                val placeIndex = cursor.getColumnIndexOrThrow(COLUMN_PLACE)
                val startDateIndex = cursor.getColumnIndexOrThrow(COLUMN_START_DATE)
                val endDateIndex = cursor.getColumnIndexOrThrow(COLUMN_END_DATE)
                val memoIndex = cursor.getColumnIndexOrThrow(COLUMN_MEMO)
                val photoIndex = cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI)
                val latIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)
                val lonIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)

                do {
                    val lat = if (cursor.isNull(latIndex)) null else cursor.getDouble(latIndex)
                    val lon = if (cursor.isNull(lonIndex)) null else cursor.getDouble(lonIndex)

                    val record = TravelRecord(
                        no = cursor.getInt(noIndex),
                        place = cursor.getString(placeIndex),
                        startDate = cursor.getString(startDateIndex),
                        endDate = cursor.getString(endDateIndex),
                        memo = cursor.getString(memoIndex),
                        photoUri = cursor.getString(photoIndex),
                        latitude = lat,
                        longitude = lon
                    )
                    recordList.add(record)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            android.util.Log.e("TravelDBHelper", "검색 오류", e)
        } finally {
            cursor?.close()
        }
        return recordList
    }
}