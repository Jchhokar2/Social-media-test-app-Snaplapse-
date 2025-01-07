package com.example.snaplapse.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snaplapse.R
import com.example.snaplapse.image_details.ImageDetailsFragment
import com.example.snaplapse.view_models.ItemsViewModel
import com.example.snaplapse.view_models.ItemsViewModel2


class CustomAdapter(
    private val mList: List<ItemsViewModel2>,
    private val fragmentManager: FragmentManager,
    private val fragment: Fragment
    ) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timeline_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        holder.imageView.setImageBitmap(itemsViewModel.image)
        holder.imageView.setOnClickListener {
            val imageDetailsFragment = ImageDetailsFragment(ItemsViewModel(position, 1, 0, ""), listOf(), itemsViewModel)
            val transaction = fragmentManager.beginTransaction()
            transaction.hide(fragment)
            transaction.add(R.id.fragmentContainerView, imageDetailsFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // sets the text to the textview from our itemHolder class
        holder.textView.text = itemsViewModel.date
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.timelineItem)
    }
}
