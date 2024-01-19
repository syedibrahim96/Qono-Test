package com.example.explorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CoordinateAdapter(private var coordinateList: List<LocationEntity>) :
    RecyclerView.Adapter<CoordinateAdapter.CoordinateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoordinateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.coordinates_item, parent, false)
        return CoordinateViewHolder(view)
    }
    override fun onBindViewHolder(holder: CoordinateViewHolder, position: Int) {
        val usercoordinates: LocationEntity? = coordinateList?.get(position)


        holder.latvalue.text = usercoordinates?.latitude.toString()
        holder.longvalue.text = usercoordinates?.longitude.toString()
        holder.datevalue.text = usercoordinates?.timestamp.toString()

    }

    override fun getItemCount(): Int {
        return coordinateList?.size ?: 0
    }

    inner class CoordinateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val latvalue: TextView = itemView.findViewById(R.id.latvalue)
        val longvalue: TextView = itemView.findViewById(R.id.longvalue)
        val datevalue: TextView = itemView.findViewById(R.id.datevalue)
    }

    fun updateData (newData : List<LocationEntity>){
        coordinateList = newData
        notifyDataSetChanged()
    }

}