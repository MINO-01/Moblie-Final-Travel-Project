package com.example.finaltravelproject.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finaltravelproject.R
import com.example.finaltravelproject.data.local.TravelDBHelper
import com.example.finaltravelproject.domain.model.TravelRecord
import com.example.finaltravelproject.ui.record.TravelRecordActivity

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var rvTravelRecords: RecyclerView
    private lateinit var adapter: TravelRecordAdapter
    private lateinit var dbHelper: TravelDBHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 및 데이터베이스 초기화
        rvTravelRecords = view.findViewById(R.id.rv_travel_records)
        dbHelper = TravelDBHelper(requireContext())

        val fabAdd = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add)
        fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), TravelRecordActivity::class.java)
            startActivity(intent)
        }

        // 리스트 레이아웃 매니저 설정
        rvTravelRecords.layoutManager = LinearLayoutManager(requireContext())

        // DB 데이터를 불러와 Adapter와 연결
        val initialData = dbHelper.getAllRecords()
        adapter = TravelRecordAdapter(initialData) { position, action ->
            // 현재 리스트에서 클릭된 아이템의 데이터 가져오기
            val record = dbHelper.getAllRecords()[position]

            when (action) {
                "수정" -> {
                    val intent = Intent(requireContext(), TravelRecordActivity::class.java)
                    intent.putExtra("RECORD_NO", record.no)
                    startActivity(intent)
                }
                "삭제" -> {
                    showDeleteDialog(record)
                }
            }
        }
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

    private fun showDeleteDialog(record: TravelRecord) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                // 1. DB에서 해당 데이터 삭제
                dbHelper.deleteRecord(record.no)
                // 2. DB를 다시 조회하여 리스트 화면 즉시 갱신
                adapter.updateData(dbHelper.getAllRecords())
            }
            .setNegativeButton("취소", null) // 취소 시 아무 동작 안 함
            .show()
    }
}