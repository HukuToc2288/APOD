package ru.hukutoc2288.apod.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

class ApodEntry(
    val date: Date?,
    val copyright: String?,
    val explanation: String?,
    @SerializedName("hdurl") val hdUrl: String?,
    @SerializedName("media_type") val mediaType: String?,
    val title: String?,
    val url: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readSerializable() as Date,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(date)
        parcel.writeString(copyright)
        parcel.writeString(explanation)
        parcel.writeString(hdUrl)
        parcel.writeString(mediaType)
        parcel.writeString(title)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ApodEntry> {
        override fun createFromParcel(parcel: Parcel): ApodEntry {
            return ApodEntry(parcel)
        }

        override fun newArray(size: Int): Array<ApodEntry?> {
            return arrayOfNulls(size)
        }
    }
}