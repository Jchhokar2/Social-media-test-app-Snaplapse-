package com.example.snaplapse.camera

import android.os.Build
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snaplapse.R
import com.example.snaplapse.view_models.CurrentPlaceViewModel

class LocationAdapter(private val lList: List<CurrentPlaceViewModel>, private val currentItem: Int, private val handler: RadioButtonCallback ): RecyclerView.Adapter<LocationAdapter.ViewHolder>() {
    private var views = ArrayList<ViewHolder>()
    private var expandedView = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPlaceViewModel = lList[position]
        if (position == currentItem) {
            holder.radioButton.isChecked = true
        }

        holder.expandButton.setOnClickListener {
            if (holder.hiddenView.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
                holder.hiddenView.visibility = View.GONE
                holder.expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                expandedView = -1
            }
            else {
                TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
                holder.hiddenView.visibility = View.VISIBLE
                holder.expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                if (expandedView != -1) {
                    TransitionManager.beginDelayedTransition(views[expandedView].cardView, AutoTransition())
                    views[expandedView].hiddenView.visibility = View.GONE
                    views[expandedView].expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                }
                expandedView = holder.absoluteAdapterPosition
            }
        }

        holder.radioButton.text = currentPlaceViewModel.name
        holder.radioButton.setOnClickListener {
            for (i in views) {
                i.radioButton.isChecked = false
            }
            holder.radioButton.isChecked = true
            handler.changeLocation(position)
        }

        holder.addressText.text = currentPlaceViewModel.address
        holder.coordinatesText.text = StringBuilder().append("(").append(currentPlaceViewModel.latitude).append(",").append(currentPlaceViewModel.longitude).append(")").toString()
        holder.typesText.text = currentPlaceViewModel.types.toString()
        holder.idText.text = currentPlaceViewModel.id

        views.add(holder)
    }

    override fun getItemCount(): Int {
        return lList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val hiddenView: LinearLayout = itemView.findViewById(R.id.hiddenView)
        val expandButton: ImageButton = itemView.findViewById(R.id.expandButton)
        val radioButton: RadioButton = itemView.findViewById(R.id.locationNameButton)
        val addressText: TextView = itemView.findViewById(R.id.addressText)
        val coordinatesText: TextView = itemView.findViewById(R.id.coordinatesText)
        val typesText: TextView = itemView.findViewById(R.id.typesText)
        val idText: TextView = itemView.findViewById(R.id.idText)
    }

    interface RadioButtonCallback {
        fun changeLocation(location: Int)
    }
}