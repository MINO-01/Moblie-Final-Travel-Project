package com.example.finaltravelproject.ui.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finaltravelproject.R
import com.example.finaltravelproject.domain.model.TravelRecord

class TravelRecordAdapter(
    private var recordList: List<TravelRecord>,
    private val onItemClicked: (Int) -> Unit,
    private val onContextMenuItemClicked: (position: Int, action: String) -> Unit
) : RecyclerView.Adapter<TravelRecordAdapter.TravelRecordViewHolder>() {

    // 외부에서 리스트 데이터가 바뀔 때 화면을 갱신해주는 함수
    fun updateData(newList: List<TravelRecord>) {
        recordList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelRecordViewHolder {
        // 아이템 레이아웃 파일을 연결함
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel_record, parent, false)
        return TravelRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelRecordViewHolder, position: Int) {
        holder.bind(recordList[position], position, onItemClicked, onContextMenuItemClicked)
    }

    override fun getItemCount(): Int = recordList.size

    // 뷰홀더: 리스트 항목들을 관리하는 클래스
    class TravelRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        private val tvPlace: TextView = itemView.findViewById(R.id.tv_place)
        private val tvVisitDate: TextView = itemView.findViewById(R.id.tv_visit_date)
        fun bind(record: TravelRecord, position: Int, onItemClicked: (Int) -> Unit, onContextMenuItemClicked: (Int, String) -> Unit) {
            tvPlace.text = record.place
            tvVisitDate.text = "${record.startDate} ~ ${record.endDate}"

            // 사진 URI가 있으면 띄우고, 없거나 오류가 나면 기본 갤러리 아이콘을 보여줌
            if (!record.photoUri.isNullOrEmpty()) {
                try {
                    ivThumbnail.setImageURI(Uri.parse(record.photoUri))
                } catch (e: Exception) {
                    ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener {
                onItemClicked(position)
            }

            // 아이템을 길게 눌렀을 때 띄울 컨텍스트 메뉴 설정
            itemView.setOnCreateContextMenuListener { menu, _, _ ->
                menu.add("수정").setOnMenuItemClickListener {
                    onContextMenuItemClicked(position, "수정")
                    true
                }
                menu.add("삭제").setOnMenuItemClickListener {
                    onContextMenuItemClicked(position, "삭제")
                    true
                }
            }
        }
    }
}