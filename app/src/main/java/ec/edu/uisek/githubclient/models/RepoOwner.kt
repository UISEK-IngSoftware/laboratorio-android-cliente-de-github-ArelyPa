package ec.edu.uisek.githubclient.models

import com.google.gson.annotations.SerializedName

data class RepoOwner (
    val id: Long,
    val login: String,
    val avatarUrl: String,
    @SerializedName("avatar_url")
    val html_url: String
)
