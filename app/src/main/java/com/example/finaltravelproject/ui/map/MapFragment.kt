package com.example.finaltravelproject.ui.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.finaltravelproject.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // xml에 만들어둔 SupportMapFragment를 찾아 지도를 비동기로 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    // 지도가 로딩 완료되면 자동으로 실행되는 함수
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 기본 위치를 서울 시청 좌표로 설정
        val seoul = LatLng(37.5665, 126.9780)

        // 마커(핀) 추가
        googleMap?.addMarker(MarkerOptions().position(seoul).title("서울 시청"))

        // 지도의 카메라를 서울 위치로 이동 (줌 레벨 12.0f)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12.0f))
    }
}