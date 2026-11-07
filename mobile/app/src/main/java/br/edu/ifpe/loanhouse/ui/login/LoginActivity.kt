package br.edu.ifpe.loanhouse.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifpe.loanhouse.BuildConfig
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.databinding.ActivityLoginBinding
import br.edu.ifpe.loanhouse.ui.main.MainActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (auth.currentUser != null) {
            abrirTelaPrincipal()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mostrarStatusDoFirebase()

        binding.botaoEntrar.setOnClickListener { entrar() }
        binding.inputSenha.setOnEditorActionListener { _, acao, _ ->
            if (acao == EditorInfo.IME_ACTION_DONE) {
                entrar()
                true
            } else {
                false
            }
        }
    }


    private fun mostrarStatusDoFirebase() {
        if (!BuildConfig.DEBUG) return
        val projeto = FirebaseApp.getInstance().options.projectId ?: return
        binding.textoStatusFirebase.text = getString(R.string.login_status_firebase, projeto)
        binding.textoStatusFirebase.visibility = View.VISIBLE
    }

    private fun entrar() {
        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val senha = binding.inputSenha.text?.toString().orEmpty()
        if (email.isEmpty() || senha.isEmpty()) {
            mostrarErro(getString(R.string.erro_campos_vazios))
            return
        }

        carregando(true)
        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { abrirTelaPrincipal() }
            .addOnFailureListener { erro ->
                carregando(false)
                mostrarErro(getString(mensagemDoErro(erro)))
            }
    }

    private fun mensagemDoErro(erro: Exception): Int = when (erro) {
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> R.string.erro_credenciais
        is FirebaseTooManyRequestsException -> R.string.erro_muitas_tentativas
        is FirebaseNetworkException -> R.string.erro_sem_internet
        else -> R.string.erro_generico
    }

    private fun carregando(ativo: Boolean) {
        binding.botaoEntrar.isEnabled = !ativo
        binding.progresso.visibility = if (ativo) View.VISIBLE else View.GONE
        if (ativo) binding.textoErro.visibility = View.GONE
    }

    private fun mostrarErro(mensagem: String) {
        binding.textoErro.text = mensagem
        binding.textoErro.visibility = View.VISIBLE
    }

    private fun abrirTelaPrincipal() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // tira o login da pilha: o botão voltar não retorna para cá
    }
}
