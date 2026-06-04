package com.example.finaltravelproject.ui.main

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.finaltravelproject.R
import com.example.finaltravelproject.ui.list.ListFragment
import com.example.finaltravelproject.ui.map.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // 초기 화면 설정 (앱 실행 시 ListFragment 표시)
        if (savedInstanceState == null) {
            replaceFragment(ListFragment())
        }

        // 하단 탭 클릭 시 화면 전환 로직
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_list -> {
                    replaceFragment(ListFragment())
                    true
                }
                R.id.nav_map -> {
                    replaceFragment(MapFragment())
                    true
                }
                else -> false
            }
        }

        // 백스택(뒤로 가기) 로직 적용
        setupBackPressed()
    }

    // 프래그먼트를 교체하는 공통 함수
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // 사용자가 기기의 뒤로 가기 버튼을 눌렀을 때의 동작을 제어.
    // 지도 화면 등 다른 탭에 있을 때는 메인(리스트) 화면으로 돌아오고,
    // 이미 메인 화면일 때만 앱을 종료하도록 흐름을 제어
    private fun setupBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 현재 선택된 탭이 '리스트(메인)'가 아니라면, 리스트 탭으로 이동
                if (bottomNavigationView.selectedItemId != R.id.nav_list) {
                    bottomNavigationView.selectedItemId = R.id.nav_list
                } else {
                    // 현재 선택된 탭이 '리스트'라면 앱 종료
                    finish()
                }
            }
        }
        // 액티비티에 뒤로 가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, callback)
    }
}