package ec.edu.uisek.githubclient.services

import ec.edu.uisek.githubclient.models.UpdateRepoRequest
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApiService {
    // Obtiene la lista de repositorios del usuario
    @GET("user/repos")
    fun getRepos(
        @Query("sort") sort: String = "created",
        @Query("direction") direction: String = "desc"
    ): Call<List<Repo>>

    // Crea un nuevo repositorio
    @POST("user/repos")
    fun addRepo(
        @Body repoRequest: RepoRequest
    ): Call<Repo>

    // Editar, owner: dueño del repositorio
    @PATCH("repos/{owner}/{repo}")
    fun updateRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body repoUpdateRequest: UpdateRepoRequest
    ): Call<Repo>

    // Elimina un repositorio existente
    @DELETE("repos/{owner}/{repo}")
    fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Call<Unit> // Unit significa que no esperamos respuesta en el cuerpo
}
