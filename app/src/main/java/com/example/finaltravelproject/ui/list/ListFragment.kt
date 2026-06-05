package com.example.finaltravelproject.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finaltravelproject.R
import com.example.finaltravelproject.data.local.TravelDBHelper
import com.example.finaltravelproject.domain.model.TravelRecord

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var rvTravelRecords: RecyclerView
    private lateinit var adapter: TravelRecordAdapter
    private lateinit var dbHelper: TravelDBHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 및 데이터베이스 초기화
        rvTravelRecords = view.findViewById(R.id.rv_travel_records)
        dbHelper = TravelDBHelper(requireContext())

        // 리스트 레이아웃 매니저 설정
        rvTravelRecords.layoutManager = LinearLayoutManager(requireContext())

        // 테스트용 더미 데이터 삽입
        // 실제 데이터 입력 기능 구현 후 삭제 예정
        if (dbHelper.getAllRecords().isEmpty()) {
            dbHelper.insertRecord(TravelRecord(place = "제주도 한라산", visitDate = "2026-06-15", memo = "너무 힘들었지만 경치가 좋았다.", photoUri = null))
            dbHelper.insertRecord(TravelRecord(place = "부산 해운대", visitDate = "2026-06-20", memo = "바다 보면서 힐링", photoUri = null))
        }

        // DB 데이터를 불러와 Adapter와 연결 (초기 화면 구성)
        val initialData = dbHelper.getAllRecords()
        adapter = TravelRecordAdapter(initialData)
        rvTravelRecords.adapter = adapter
    }

    // 화면이 다시 활성화될 때 DB 데이터를 새로고침하여 최신 상태 유지
    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized && ::adapter.isInitialized) {
            val updatedList = dbHelper.getAllRecords()
            adapter.updateData(updatedList)
        }
    }
}