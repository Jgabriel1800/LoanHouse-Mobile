package br.edu.ifpe.loanhouse.ui.relatorios

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifpe.loanhouse.databinding.ActivityRelatoriosBinding

/** "Relatórios mensais" do Figma. O conteúdo (totais e PDF) é a task de 30/11. */
class RelatoriosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRelatoriosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
