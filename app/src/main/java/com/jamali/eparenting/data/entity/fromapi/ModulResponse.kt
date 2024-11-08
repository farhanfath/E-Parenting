package com.jamali.eparenting.data.entity.fromapi

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModulResponse(

	@field:SerializedName("modul")
	val modul: List<ModulItem>
) : Parcelable

@Parcelize
data class ModulItem(

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("penjelasan")
	val penjelasan: String,

	@field:SerializedName("jenis")
	val jenis: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("video")
	val video: String,

	@field:SerializedName("judul")
	val judul: String
) : Parcelable
