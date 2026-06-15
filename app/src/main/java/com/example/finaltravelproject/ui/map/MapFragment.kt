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

        // 지도 제스처 활성화
        googleMap?.uiSettings?.isZoomGesturesEnabled = true
        googleMap?.uiSettings?.isScrollGesturesEnabled = true

        val dbHelper = TravelDBHelper(requireContext())

        // 유효한 데이터만 필터링
        val records = dbHelper.getAllRecords().filter {
            it.latitude != null && it.longitude != null && it.latitude != 0.0 && it.longitude != 0.0
        }

        if (records.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()

            records.forEach { record ->
                val position = LatLng(record.latitude!!, record.longitude!!)
                val marker = googleMap?.addMarker(MarkerOptions().position(position).title(record.place))
                marker?.tag = record
                boundsBuilder.include(position)
            }

            // 카메라 이동 로직
            try {
                if (records.size == 1) {
                    val position = LatLng(records[0].latitude!!, records[0].longitude!!)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
                } else {
                    val padding = 150
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding))
                }
            } catch (e: Exception) {
                // 예외 발생 시 기본 좌표로 이동
                val seoul = LatLng(37.5665, 126.9780)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))
            }

            // InfoWindow 설정
            googleMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoWindow(marker: com.google.android.gms.maps.model.Marker): View? = null

                override fun getInfoContents(marker: com.google.android.gms.maps.model.Marker): View {
                    val view = layoutInflater.inflate(R.layout.item_map_info_window, null)
                    val ivThumbnail = view.findViewById<android.widget.ImageView>(R.id.iv_info_thumbnail)
                    val tvPlace = view.findViewById<android.widget.TextView>(R.id.tv_info_place)

                    val record = marker.tag as? TravelRecord
                    record?.let {
                        tvPlace.text = it.place
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

            // 마커 클릭 이동
            googleMap?.setOnInfoWindowClickListener { marker ->
                val record = marker.tag as? TravelRecord
                record?.let {
                    val intent = Intent(requireContext(), TravelRecordActivity::class.java)
                    intent.putExtra("RECORD_NO", it.no)
                    startActivity(intent)
                }
            }
        } else {
            // 데이터가 없을 때 기본 좌표
            val seoul = LatLng(37.5665, 126.9780)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12.0f))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            progressBar.visibility = View.GONE
        }
    }
}