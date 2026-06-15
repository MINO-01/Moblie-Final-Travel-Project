package com.example.finaltravelproject.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finaltravelproject.R
import com.example.finaltravelproject.data.local.TravelDBHelper
import com.example.finaltravelproject.domain.model.TravelRecord
import com.example.finaltravelproject.ui.record.TravelRecordActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var rvTravelRecords: RecyclerView
    private lateinit var adapter: TravelRecordAdapter
    private lateinit var dbHelper: TravelDBHelper
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    // 비동기로 불러온 데이터 임시 보관
    private var currentRecords: List<TravelRecord> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 및 DB 연결
        rvTravelRecords = view.findViewById(R.id.rv_travel_records)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmpty = view.findViewById(R.id.tv_empty)
        dbHelper = TravelDBHelper(requireContext())

        val fabAdd = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add)
        fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), TravelRecordActivity::class.java)
            startActivity(intent)
        }

        // 리사이클러뷰 레이아웃 매니저 세팅
        rvTravelRecords.layoutManager = LinearLayoutManager(requireContext())

        // 상단 옵션 메뉴 세팅
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.option_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all -> {
                        showDeleteAllDialog()
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

        // 처음에 빈 리스트로 어댑터 달아두기
        adapter = TravelRecordAdapter(
            recordList = emptyList(),
            onItemClicked = { position ->
                // 짧게 누르면 상세 화면 이동
                val record = currentRecords[position] // DB 재조회 없이 캐시된 데이터 씀
                val intent = Intent(requireContext(), TravelRecordActivity::class.java)
                intent.putExtra("RECORD_NO", record.no)
                startActivity(intent)
            },
            onContextMenuItemClicked = { position, action ->
                // 길게 누르면 컨텍스트 메뉴 띄우기
                val record = currentRecords[position]
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

        // 뷰 세팅 끝나면 데이터 로딩 시작
        loadDataAsync()
    }

    // 화면 다시 켜질 때 데이터 갱신
    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized && ::adapter.isInitialized) {
            loadDataAsync()
        }
    }

    // 코루틴 써서 비동기로 DB 읽어오기
    private fun loadDataAsync() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            // 로딩바 띄우기
            progressBar.visibility = View.VISIBLE

            // 백그라운드에서 DB 데이터 가져옴
            val records = withContext(Dispatchers.IO) {
                dbHelper.getAllRecords()
            }

            // 데이터 세팅하고 로딩바 숨기기
            currentRecords = records
            adapter.updateData(records)
            progressBar.visibility = View.GONE

            // 기록이 없으면 안내 문구 띄우고 리스트 숨김
            if (records.isEmpty()) {
                rvTravelRecords.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            } else {
                rvTravelRecords.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
            }
        }
    }

    // 개별 삭제 다이얼로그
    private fun showDeleteDialog(record: TravelRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                dbHelper.deleteRecord(record.no)
                loadDataAsync() // 삭제 후 리스트 새로고침
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 전체 삭제 다이얼로그
    private fun showDeleteAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("전체 삭제 확인")
            .setMessage("모든 여행 기록을 정말로 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("전체 삭제") { _, _ ->
                dbHelper.deleteAllRecords()
                loadDataAsync() // 삭제 후 리스트 새로고침
            }
            .setNegativeButton("취소", null)
            .show()
    }
}