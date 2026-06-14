package com.example.finaltravelproject.ui.map

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.finaltravelproject.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progress_bar_map)

        // 지도 로딩 요청 전: 프로그레스바 보여주기
        progressBar.visibility = View.VISIBLE

        // 지도를 비동기로 구글 맵에 요청
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    // 구글 지도 네트워크 로딩이 완료되면 이 함수가 자동으로 실행됨
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val seoul = LatLng(37.5665, 126.9780)
        googleMap?.addMarker(MarkerOptions().position(seoul).title("서울 시청"))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12.0f))

        // 지도 로딩 완료: 코루틴을 통해 0.5초 뒤 로딩 바 숨기기
        viewLifecycleOwner.lifecycleScope.launch {
            // TODO: 확인 후 삭제하거나 주석 처리
            delay(500)
            progressBar.visibility = View.GONE
        }
    }
}