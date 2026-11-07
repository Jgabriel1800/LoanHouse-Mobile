package br.edu.ifpe.loanhouse.ui.casa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.data.model.Casa
import br.edu.ifpe.loanhouse.data.repository.CasaRepository
import br.edu.ifpe.loanhouse.databinding.ActivityCasaBinding
import br.edu.ifpe.loanhouse.databinding.ItemCampoBinding
import br.edu.ifpe.loanhouse.domain.Formatos
import br.edu.ifpe.loanhouse.domain.Pendencias
import br.edu.ifpe.loanhouse.ui.comum.mostrarBadge
import br.edu.ifpe.loanhouse.ui.comum.mostrarPendencias
import br.edu.ifpe.loanhouse.ui.comum.pintar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration

/**
 * "Detalhes da casa" do Figma. Nesta entrega mostra os dados e permite excluir;
 * a edição (com ViaCEP) é a task de 09/11.
 */
class CasaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCasaBinding
    private val repositorio = CasaRepository()
    private var escuta: ListenerRegistration? = null
    private lateinit var codigo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codigo = intent.getStringExtra(EXTRA_CODIGO).orEmpty()
        binding = ActivityCasaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.botaoEditar.setOnClickListener {
            Toast.makeText(this, R.string.em_breve_cadastro, Toast.LENGTH_LONG).show()
        }
        binding.botaoExcluir.setOnClickListener { confirmarExclusao() }

        escuta = repositorio.escutarCasa(
            codigo,
            aoReceber = { casa -> if (casa == null) fecharSemCasa() else mostrar(casa) },
            aoFalhar = { fecharSemCasa() },
        )
    }

    private fun mostrar(casa: Casa) {
        val estado = Pendencias.estado(casa)
        binding.textoCodigo.text = casa.codigo
        binding.iconeCasa.pintar(estado)
        binding.badgeSituacao.mostrarBadge(estado)

        val pendencias = Pendencias.listar(casa)
        binding.grupoBadges.visibility = if (pendencias.isEmpty()) View.GONE else View.VISIBLE
        binding.grupoBadges.mostrarPendencias(pendencias, alugada = casa.situacao != "vaga")

        // Remove os campos antigos (mantém só o título "Dados do imóvel") e recria
        binding.listaCampos.removeViews(1, binding.listaCampos.childCount - 1)
        campo(R.string.campo_rua, casa.rua)
        campo(R.string.campo_numero, casa.numero)
        campo(R.string.campo_bairro, casa.bairro)
        campo(R.string.campo_cep, casa.cep)
        campo(R.string.campo_valor, Formatos.moeda(casa.valorExibido))
        binding.textoCidade.text = "${casa.cidade.ifBlank { "Belo Jardim" }}/${casa.uf.ifBlank { "PE" }}"
    }

    private fun campo(rotulo: Int, valor: String) {
        val item = ItemCampoBinding.inflate(LayoutInflater.from(this), binding.listaCampos, false)
        item.rotulo.setText(rotulo)
        item.valor.text = valor.ifBlank { "—" }
        binding.listaCampos.addView(item.root)
    }

    private fun confirmarExclusao() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.excluir_titulo, codigo))
            .setMessage(R.string.excluir_mensagem)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.excluir_confirmar) { _, _ ->
                repositorio.excluir(codigo).addOnSuccessListener { finish() }
            }
            .show()
    }

    private fun fecharSemCasa() {
        Toast.makeText(this, R.string.casa_nao_encontrada, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        escuta?.remove()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_CODIGO = "codigo"

        fun intent(context: Context, codigo: String) =
            Intent(context, CasaActivity::class.java).putExtra(EXTRA_CODIGO, codigo)
    }
}
