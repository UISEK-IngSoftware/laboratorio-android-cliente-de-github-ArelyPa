package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepoFormBinding
    private var isEditMode = false
    private var repoNameToEdit: String? = null
    private var repoOwner: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        if (isEditMode) {
            title = "Editar Repositorio"
            repoNameToEdit = intent.getStringExtra("REPO_NAME")
            repoOwner = intent.getStringExtra("REPO_OWNER")
            val description = intent.getStringExtra("REPO_DESCRIPTION")

            binding.repoNameInput.setText(repoNameToEdit)
            binding.repoNameInput.isEnabled = false
            binding.repoDescriptionInput.setText(description)
        } else {
            title = "Nuevo Repositorio"
        }

        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { createRepo() }
    }
    private fun handleSave() {
        // La validación del nombre solo es necesaria en modo creación
        if (!isEditMode && !validateForm()) {
            return
        }

        if (isEditMode) {
            updateRepo()
        } else {
            createRepo()
        }
    }
    private fun validateForm() : Boolean{
        val repoName = binding.repoNameInput.text.toString()

        if (repoName.isBlank()){
            binding.repoNameInput.error= "El nombre del repositorio es requerido"
            return false
        }

        if (repoName.contains(" ")) {
            binding.repoNameInput.error = "El nombre del repositorio no puede contener espacios"
            return false
        }

        binding.repoNameInput.error = null
        return true
    }

    private fun createRepo() {
        if (!validateForm()) {
            return
        }
        val repoName = binding.repoNameInput.text.toString().trim()
        val repoDescription = binding.repoDescriptionInput.text.toString().trim()

        val repoRequest = RepoRequest(repoName, repoDescription)
        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.addRepo(repoRequest)

        call.enqueue(object: Callback<Repo>{
            override fun onResponse(call: Call<Repo?>, response: Response<Repo?>) {
                if (response.isSuccessful){
                    showMessage("Repositorio creado exitosamente")
                    finish()

                }else {
                    val errorMessage = when (response.code()){
                        401 -> "No autorizado"
                        403 -> "Prohibido"
                        404 -> "No encontrado"
                        else -> "Error ${response.code()}"
                    }
                    showMessage("Error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<Repo?>, t: Throwable) {
                val errorMsg = "Error al crear el repositorio: ${t.message}"
                Log.d("RepoForm", errorMsg, t)
                showMessage(errorMsg)
            }
        })

    }

    private fun updateRepo() {
        val repoDescription = binding.repoDescriptionInput.text.toString().trim()

        // Necesitamos el dueño y el nombre del repo que guardamos antes
        if (repoOwner == null || repoNameToEdit == null) {
            showMessage("Error: No se encontró la información del repositorio a editar.")
            return
        }

        // Usaremos RepoRequest para la petición, asumiendo que tiene 'description'
        // Si no, deberías crear un UpdateRepoRequest(val description: String)
        val repoRequest = RepoRequest(name = "", description = repoDescription) // El 'name' se ignora en PATCH

        val apiService = RetrofitClient.gitHubApiService
        // Asegúrate de que tu apiService tenga un método `updateRepo`
        val call = apiService.updateRepo(repoOwner!!, repoNameToEdit!!, repoRequest)

        call.enqueue(object: Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful){
                    showMessage("Repositorio actualizado exitosamente")
                    finish()
                } else {
                    showMessage("Error al actualizar: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                val errorMsg = "Error al actualizar el repositorio: ${t.message}"
                Log.d("RepoForm", errorMsg, t)
                showMessage(errorMsg)
                }
            })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
