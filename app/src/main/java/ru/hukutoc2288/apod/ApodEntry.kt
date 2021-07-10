package ru.hukutoc2288.apod

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class ApodEntry(
    val date: Date?, val copyright: String?, val explanation: String?, val hdurl: String?, val media_type: String?, val title: String?, val url: String?
): Parcelable {
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
        parcel.writeString(hdurl)
        parcel.writeString(media_type)
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