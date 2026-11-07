package br.edu.ifpe.loanhouse.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.databinding.ActivityMainBinding
import br.edu.ifpe.loanhouse.ui.casas.CasasFragment
import br.edu.ifpe.loanhouse.ui.embreve.EmBreveFragment
import br.edu.ifpe.loanhouse.ui.home.HomeFragment
import br.edu.ifpe.loanhouse.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Tela principal: a navegação inferior do Figma (Home, Casas, Inquilinos, Calendário, Recursos)
 * troca o Fragment mostrado. Cada Fragment tem o próprio cabeçalho.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser == null) {
            irParaLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navegacao.setOnItemSelectedListener { item ->
            mostrar(item.itemId)
            true
        }

        if (savedInstanceState == null) binding.navegacao.selectedItemId = R.id.nav_home
    }

    private fun mostrar(@IdRes itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_casas -> CasasFragment()
            R.id.nav_inquilinos -> EmBreveFragment.novo(R.string.nav_inquilinos, R.string.em_breve_inquilinos, R.drawable.ic_inquilinos)
            R.id.nav_calendario -> EmBreveFragment.novo(R.string.nav_calendario, R.string.em_breve_calendario, R.drawable.ic_calendario)
            R.id.nav_recursos -> EmBreveFragment.novo(R.string.nav_recursos, R.string.em_breve_recursos, R.drawable.ic_recursos)
            else -> HomeFragment()
        }
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.conteudo, fragment)
            .commit()
    }

    /** Chamado pelo botão Logout da Home. */
    fun sair() {
        auth.signOut()
        irParaLogin()
    }

    private fun irParaLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
        finish()
    }
}
