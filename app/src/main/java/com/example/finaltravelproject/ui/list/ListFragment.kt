package com.example.finaltravelproject.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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

        //  옵션 메뉴 설정 (전체 삭제, 앱 정보)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // res/menu/option_menu.xml 파일을 연결
                menuInflater.inflate(R.menu.option_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all -> {
                        showDeleteAllDialog() // 하단에 정의한 전체 삭제 다이얼로그 띄우기
                        true
                    }
                    R.id.action_app_info -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("앱 정보")
                            .setMessage("여행 계획 및 기록 앱 v1.0")
                            .setPositiveButton("확인", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // DB 데이터를 불러와 Adapter와 연결 (일반 클릭, 롱클릭 콜백 분리)
        val initialData = dbHelper.getAllRecords()
        adapter = TravelRecordAdapter(
            recordList = initialData,
            onItemClicked = { position ->
                // 짧게 클릭 시 상세(수정) 화면으로 이동
                val record = dbHelper.getAllRecords()[position]
                val intent = Intent(requireContext(), TravelRecordActivity::class.java)
                intent.putExtra("RECORD_NO", record.no)
                startActivity(intent)
            },
            onContextMenuItemClicked = { position, action ->
                // 길게 클릭 시 컨텍스트 메뉴 동작
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
        )
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

    // 개별 삭제 확인 다이얼로그
    private fun showDeleteDialog(record: TravelRecord) {
        AlertDialog.Builder(requireContext())
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

    // 전체 삭제 확인 다이얼로그
    private fun showDeleteAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("전체 삭제 확인")
            .setMessage("모든 여행 기록을 정말로 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("전체 삭제") { _, _ ->
                dbHelper.deleteAllRecords()
                adapter.updateData(dbHelper.getAllRecords()) // 리스트 즉시 갱신 (빈 화면이 됨)
            }
            .setNegativeButton("취소", null)
            .show()
    }
}