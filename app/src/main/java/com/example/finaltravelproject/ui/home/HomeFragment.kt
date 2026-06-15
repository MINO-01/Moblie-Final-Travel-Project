package com.example.finaltravelproject.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.finaltravelproject.R
import com.example.finaltravelproject.data.local.TravelDBHelper
import com.example.finaltravelproject.ui.record.TravelRecordActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var dbHelper: TravelDBHelper
    private lateinit var tvTotalCount: TextView
    private lateinit var tvRecentPlace: TextView
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = TravelDBHelper(requireContext())
        tvTotalCount = view.findViewById(R.id.tv_total_count)
        tvRecentPlace = view.findViewById(R.id.tv_recent_place)
        progressBar = view.findViewById(R.id.progress_bar_home)

        val btnAdd = view.findViewById<Button>(R.id.btn_add_record)
        btnAdd.setOnClickListener {
            // 버튼 누르면 기록 추가 화면으로 넘어감
            val intent = Intent(requireContext(), TravelRecordActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 화면 보일 때마다 비동기로 최신 DB 데이터 가져옴
        loadDataAsync()
    }

    private fun loadDataAsync() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            // 로딩바 띄우기
            progressBar.visibility = View.VISIBLE

            // 백그라운드에서 DB 데이터 가져옴
            val records = withContext(Dispatchers.IO) {
                dbHelper.getAllRecords()
            }

            // 개수 세팅
            tvTotalCount.text = "총 여행 기록: ${records.size}개"

            // 기록 있으면 제일 첫 번째(최신) 장소 띄워줌
            if (records.isNotEmpty()) {
                tvRecentPlace.text = "최근 다녀온 곳: ${records[0].place}"
            } else {
                tvRecentPlace.text = "최근 다녀온 곳: 없음"
            }

            // 세팅 끝나면 로딩바 숨김
            progressBar.visibility = View.GONE
        }
    }
}