package com.jamali.eparenting.ui.customer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jamali.eparenting.databinding.ItemWelcomePageBinding

class WelcomeAdapter(private val slides: List<WelcomeSlide>) : RecyclerView.Adapter<WelcomeAdapter.ViewPagerViewHolder>() {

    class ViewPagerViewHolder(val binding: ItemWelcomePageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val binding = ItemWelcomePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        val slide = slides[position]
        with(holder.binding) {
            titleText.text = slide.title
            descText.text = slide.description
            welcomeImage.setAnimation(slide.imageSlide)
        }
    }

    override fun getItemCount() = slides.size
}

data class WelcomeSlide(
    val imageSlide: Int,
    val title: String,
    val description: String,
)