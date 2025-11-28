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

        // Configura la lista (RecyclerView) y sus acciones
        setupRecyclerView()

        // Configura el botón flotante (+) para abrir el formulario de creación
        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
    }

    // Cada vez que la pantalla se vuelve visible, se actualiza la lista de repositorios
    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    // Conecta el RecyclerView con su adaptador y define qué hacer al hacer clic en editar/eliminar
    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            // Acción al pulsar el botón de editar en un item
            onEditClick = { repo ->
                val intent = Intent(this, RepoForm::class.java).apply {
                    putExtra("IS_EDIT_MODE", true)
                    putExtra("REPO_NAME", repo.name)
                    putExtra("REPO_OWNER", repo.owner.login)
                    putExtra("REPO_DESCRIPTION", repo.description)
                }
                startActivity(intent)
            },
            // Acción al pulsar el botón de eliminar en un item
            onDeleteClick = { repo ->
                showDeleteConfirmationDialog(repo)
            }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }

    // Muestra una ventana de alerta para confirmar la eliminación
    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el repositorio '${repo.name}'?")
            .setPositiveButton("Aceptar") { _, _ ->
                // Si el usuario acepta, llama a la función para borrar en la API
                deleteRepositoryFromApi(repo)
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(R.drawable.baseline_delete_outline_24)
            .show()
    }

    // Llama a la API para eliminar un repositorio específico
    private fun deleteRepositoryFromApi(repo: Repo) {
        val owner = repo.owner.login
        val repoName = repo.name

        RetrofitClient.getApiService().deleteRepo(owner, repoName).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio '${repo.name}' eliminado correctamente")
                    // Actualiza la lista para que desaparezca el item eliminado
                    fetchRepositories()
                } else {
                    showMessage("Error al eliminar: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                showMessage("Fallo de conexión al intentar eliminar: ${t.message}")
            }
        })
    }

    // Llama a la API para obtener la lista de repositorios del usuario
    private fun fetchRepositories() {
        val apiService: GithubApiService = RetrofitClient.getApiService()
        val call = apiService.getRepos()

        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "No autorizado (Revisa tu Token)"
                        403 -> "Prohibido"
                        404 -> "No encontrado"
                        else -> "Error ${response.code()}"
                    }
                    showMessage("Error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                showMessage("No se pudieron cargar repositorios")
            }
        })

    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepoForm() {
        val intent = Intent(this, RepoForm::class.java).apply {
            putExtra("IS_EDIT_MODE", false)
        }
        startActivity(intent)
    }
}
