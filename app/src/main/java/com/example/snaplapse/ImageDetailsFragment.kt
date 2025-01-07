package com.example.snaplapse

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.snaplapse.image_details.FlagDialogFragment
import com.example.snaplapse.image_details.OnSwipeTouchListener
import com.example.snaplapse.view_models.ItemsViewModel
import com.example.snaplapse.view_models.ItemsViewModel2

class ImageDetailsFragment(var item: ItemsViewModel, private val mList: List<ItemsViewModel>, val item2: ItemsViewModel2? = null) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_image_details, container, false)
        val text = view.findViewById<TextView>(R.id.textView)
        val img: ImageView = view.findViewById(R.id.imageView) as ImageView
        if (item2 == null) {
            text.text = resources.getString(R.string.image_details).format(item.text)
            img.setImageResource(item.image)
            img.setOnTouchListener(OnSwipeTouchListener(activity, item, mList, view))
        } else {
            text.text = resources.getString(R.string.image_details).format(item2.text)
            img.setImageBitmap(item2.image)
        }

        val likeButton: ImageButton = view.findViewById(R.id.like_button) as ImageButton
        likeButton.setOnClickListener {
            likeButton.setImageResource(R.drawable.ic_baseline_thumb_up_24_blue)
            Toast.makeText(activity, resources.getString(R.string.like_toast), Toast.LENGTH_SHORT).show()
        }

        val flagButton: ImageButton = view.findViewById(R.id.flag_button) as ImageButton
        flagButton.setOnClickListener {
            FlagDialogFragment().show(childFragmentManager, "")
        }

        val backButton = view.findViewById<ImageButton>(R.id.image_details_back_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return view
    }
}
