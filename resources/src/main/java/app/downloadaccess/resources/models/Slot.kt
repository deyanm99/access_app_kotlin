package app.downloadaccess.resources.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Slot {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("from")
    @Expose
    var from: String? = null

    @SerializedName("to")
    @Expose
    var to: String? = null

    @SerializedName("occupiedSlots")
    @Expose
    var occupiedSlots: Int? = null

    @SerializedName("maxSlots")
    @Expose
    var maxSlots: Int? = null

    @SerializedName("isPlanned")
    @Expose
    var isPlanned: Boolean? = null

    @SerializedName("repeat")
    @Expose
    private var repeat: Boolean? = null

    @SerializedName("friends")
    @Expose
    var friends: Int? = null

    @SerializedName("userId")
    @Expose
    var userId: String? = null

}