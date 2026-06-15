package com.example.finaltravelproject.ui.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.finaltravelproject.R
import com.example.finaltravelproject.data.local.TravelDBHelper
import com.example.finaltravelproject.domain.model.TravelRecord
import com.example.finaltravelproject.ui.record.TravelRecordActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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

        // 비동기로 구글 맵 호출
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    // 지도 렌더링 끝나면 자동 실행
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val dbHelper = TravelDBHelper(requireContext())

        // DB에서 좌표(위도, 경도) 있는 기록만 뽑아냄
        val records = dbHelper.getAllRecords().filter {
            it.latitude != null && it.longitude != null
        }

        // GPS 데이터 있는 기록이 하나라도 있으면
        if (records.isNotEmpty()) {
            // 화면에 마커 다 들어오게 영역 잡는 빌더
            val boundsBuilder = LatLngBounds.Builder()

            records.forEach { record ->
                val position = LatLng(record.latitude!!, record.longitude!!)
                val marker = googleMap?.addMarker(MarkerOptions().position(position).title(record.place))
                marker?.tag = record

                boundsBuilder.include(position)
            }

            // 마커들 짤리지 않게 여백 주고 카메라 이동
            val padding = 100
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding))

            googleMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoWindow(marker: com.google.android.gms.maps.model.Marker): View? = null

                override fun getInfoContents(marker: com.google.android.gms.maps.model.Marker): View {
                    // 커스텀 레이아웃 불러오기
                    val view = layoutInflater.inflate(R.layout.item_map_info_window, null)
                    val ivThumbnail = view.findViewById<android.widget.ImageView>(R.id.iv_info_thumbnail)
                    val tvPlace = view.findViewById<android.widget.TextView>(R.id.tv_info_place)

                    // 마커 태그에서 TravelRecord 꺼내기
                    val record = marker.tag as? TravelRecord

                    record?.let {
                        tvPlace.text = it.place

                        // 사진 띄우기 (없거나 에러나면 갤러리 기본 아이콘)
                        if (!it.photoUri.isNullOrEmpty()) {
                            try {
                                ivThumbnail.setImageURI(Uri.parse(it.photoUri))
                            } catch (e: Exception) {
                                ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                            }
                        } else {
                            ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    }
                    return view
                }
            })

            // 말풍선 클릭 이벤트 (상세/수정 화면 이동)
            googleMap?.setOnInfoWindowClickListener { marker ->
                val record = marker.tag as? TravelRecord
                record?.let {
                    // 리스트에서 누른 거랑 똑같이 RECORD_NO 담아서 넘김
                    val intent = Intent(requireContext(), TravelRecordActivity::class.java)
                    intent.putExtra("RECORD_NO", it.no)
                    startActivity(intent)
                }
            }
        } else {
            // 위치 정보 등록된 게 없으면 기본 화면(서울 시청) 띄움
            val seoul = LatLng(37.5665, 126.9780)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12.0f))
        }

        // 지도 로딩 완료: 코루틴을 통해 0.5초 뒤 로딩 바 숨기기
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            progressBar.visibility = View.GONE
        }
    }
}