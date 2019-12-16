package com.hololo.app.dnschanger.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "dns_table")
class DNSModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "name")
    @SerializedName("name") var name: String = "Custom DNS",
    @ColumnInfo(name = "first_dns")
    @SerializedName("firstDNS") var firstDns: String = "0.0.0.0",
    @ColumnInfo(name = "second_dns")
    @SerializedName("secondDNS") var secondDns: String = "0.0.0.0"
) : Parcelable
