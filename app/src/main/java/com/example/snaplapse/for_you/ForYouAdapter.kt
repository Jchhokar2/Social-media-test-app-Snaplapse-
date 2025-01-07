package com.example.snaplapse.for_you

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snaplapse.R
import com.example.snaplapse.timeline.TimelineFragment
import com.example.snaplapse.view_models.ForYouViewModel
import androidx.fragment.app.Fragment

class ForYouAdapter(
    private val mList: List<ForYouViewModel>,
    private val fragmentManager: FragmentManager,
    private val fragment: Fragment
    ) : RecyclerView.Adapter<ForYouAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.for_you_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val forYouViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        holder.imageView.setImageBitmap(forYouViewModel.thumbnail)
        holder.imageView.setOnClickListener{
            val timelineFragment = TimelineFragment(forYouViewModel.locationId)
            val transaction = fragmentManager.beginTransaction()
            transaction.hide(fragment)
            transaction.add(R.id.fragmentContainerView, timelineFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // sets the text to the textview from our itemHolder class
        holder.textView.text = forYouViewModel.name
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.forYouImageView)
        val textView: TextView = itemView.findViewById(R.id.forYouItem)
    }
}
