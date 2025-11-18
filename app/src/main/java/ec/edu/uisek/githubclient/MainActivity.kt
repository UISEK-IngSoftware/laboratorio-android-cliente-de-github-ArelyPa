package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.GithubApiService
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            onEditClick = { repo ->
                // Lógica para editar el repositorio
            val intent = Intent(this, RepoForm::class.java).apply {
                putExtra("IS_EDIT_MODE",true)
                putExtra("REPO_NAME", repo.name)
                putExtra("REPO_OWNER", repo.owner.login)
                putExtra("REPO_DESCRIPTION", repo.description)
            }
            startActivity(intent)
            },
            onDeleteClick = { repo ->
                // Acción para ELIMINAR: Mostrar el diálogo de confirmación
                showDeleteConfirmationDialog(repo)
            }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }
    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el repositorio '${repo.name}'?")
            .setPositiveButton("Aceptar") { _, _ ->
                deleteRepositoryFromApi(repo)
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(R.drawable.baseline_delete_outline_24)
            .show()
    }
    private fun deleteRepositoryFromApi(repo: Repo) {
        val owner = repo.owner.login
        val repoName = repo.name

        RetrofitClient.gitHubApiService.deleteRepo(owner, repoName).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio '${repo.name}' eliminado correctamente")
                    fetchRepositories()
                } else {
                    val errorMessage = "Error al eliminar: ${response.code()} - ${response.message()}"
                    showMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                val errorMsg = "Fallo de conexión al intentar eliminar: ${t.message}"
                showMessage(errorMsg)
            }
        })
    }


    private fun fetchRepositories() {
        val apiService: GithubApiService = RetrofitClient.gitHubApiService
        val call = apiService.getRepos()

        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>?>, response: Response<List<Repo>?>) {
                if (response.isSuccessful){
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                    }
                } else {
                    val errorMessage = when (response.code()){
                        401 -> "No autorizado"
                        403 -> "Prohibido"
                        404 -> "No encontrado"
                        else -> "Error ${response.code()}"
                    }
                    showMessage("Error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<List<Repo>?>, t: Throwable) {
                showMessage("No se pudieron cargar repositorios")
            }
        })
    }

    private fun showMessage (message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepoForm(){
        val intent = Intent(this, RepoForm::class.java).apply {
            putExtra("IS_EDIT_MODE", false)
        }
            startActivity(intent)
        }
    }
