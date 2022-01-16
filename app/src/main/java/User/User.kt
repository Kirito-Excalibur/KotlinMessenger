package User

import android.os.Parcelable

@kotlinx.parcelize.Parcelize

class User(val uid: String, val username: String, val profileImageUrl: String):Parcelable {
    constructor() : this("", "", "")
}