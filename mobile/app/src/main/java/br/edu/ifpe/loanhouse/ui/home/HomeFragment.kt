package br.edu.ifpe.loanhouse.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifpe.loanhouse.BuildConfig
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.data.model.Casa
import br.edu.ifpe.loanhouse.data.repository.CasaRepository
import br.edu.ifpe.loanhouse.databinding.FragmentHomeBinding
import br.edu.ifpe.loanhouse.domain.Formatos
import br.edu.ifpe.loanhouse.domain.Pendencias
import br.edu.ifpe.loanhouse.domain.TipoPendencia
import br.edu.ifpe.loanhouse.ui.casa.CasaActivity
import br.edu.ifpe.loanhouse.ui.casas.CasaAdapter
import br.edu.ifpe.loanhouse.ui.comum.abrirWhatsApp
import br.edu.ifpe.loanhouse.ui.comum.preencher
import br.edu.ifpe.loanhouse.ui.main.MainActivity
import br.edu.ifpe.loanhouse.ui.relatorios.RelatoriosActivity
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration

/**
 * Home (dashboard): mostra SÓ as casas com pendência (aluguel, água, luz ou IPTU).
 * Casas em dia e vagas aparecem apenas na aba Casas. Mesma regra do painel.html do site.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val repositorio = CasaRepository()
    private var escuta: ListenerRegistration? = null
    private val adapter = CasaAdapter { abrirCasa(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cabecalho.preencher(titulo = null) // a Home mostra o logo
        binding.listaPendentes.layoutManager = LinearLayoutManager(requireContext())
        binding.listaPendentes.adapter = adapter

        binding.botaoRelatorios.setOnClickListener { startActivity(Intent(requireContext(), RelatoriosActivity::class.java)) }
        binding.botaoLogout.setOnClickListener { (requireActivity() as MainActivity).sair() }
        binding.botaoExemplos.setOnClickListener { criarExemplos() }

        escuta = repositorio.escutarCasas(
            aoReceber = { casas -> _binding?.let { mostrar(casas) } },
            aoFalhar = { erro -> _binding?.let { mostrarErro(erro) } },
        )
    }

    private fun mostrar(todas: List<Casa>) {
        val pendentes = Pendencias.ordenar(todas).filter { Pendencias.temPendencia(it) }
        binding.progresso.visibility = View.GONE
        binding.textoMensagem.visibility = View.GONE
        binding.textoTitulo.text = getString(R.string.home_titulo, pendentes.size)
        adapter.submitList(pendentes)

        binding.cartaoEmDia.visibility = if (pendentes.isEmpty()) View.VISIBLE else View.GONE
        binding.botaoExemplos.visibility = if (BuildConfig.DEBUG && todas.isEmpty()) View.VISIBLE else View.GONE
        mostrarAlerta(pendentes)
    }

    /** Alerta vermelho do topo: quantas casas, quanto em atraso e o botão de cobrança. Mesma conta do site. */
    private fun mostrarAlerta(pendentes: List<Casa>) {
        if (pendentes.isEmpty()) {
            binding.cartaoAlerta.visibility = View.GONE
            return
        }
        binding.cartaoAlerta.visibility = View.VISIBLE

        val comAluguel = pendentes.filter { TipoPendencia.ALUGUEL in Pendencias.listar(it) }
        val base = comAluguel.ifEmpty { pendentes }
        val total = Formatos.moeda(base.sumOf { it.valorExibido })
        binding.textoAlerta.text = if (pendentes.size == 1) {
            getString(R.string.home_alerta_uma, total)
        } else {
            getString(R.string.home_alerta_varias, pendentes.size, total)
        }
        binding.textoAlertaDetalhe.text = pendentes.joinToString(" • ") { casa ->
            "${casa.codigo} (${Pendencias.listar(casa).joinToString(", ") { getString(it.rotulo) }})"
        }

        val primeira = base.first()
        val telefone = primeira.inquilinoTelefone
        binding.botaoCobrar.visibility = if (telefone.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.botaoCobrar.setOnClickListener {
            val nome = primeira.inquilinoNome?.split(" ")?.firstOrNull().orEmpty().ifBlank { "Inquilino" }
            abrirWhatsApp(requireContext(), telefone.orEmpty(), getString(R.string.mensagem_cobranca, nome, primeira.codigo))
        }
    }

    private fun mostrarErro(erro: FirebaseFirestoreException) {
        binding.progresso.visibility = View.GONE
        binding.textoMensagem.visibility = View.VISIBLE
        binding.textoMensagem.setText(
            if (erro.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) R.string.casas_sem_permissao else R.string.casas_erro,
        )
    }

    private fun criarExemplos() {
        binding.botaoExemplos.isEnabled = false
        repositorio.criarCasasDeExemplo()
            .addOnSuccessListener { context?.let { Toast.makeText(it, R.string.home_exemplos_criados, Toast.LENGTH_SHORT).show() } }
            .addOnFailureListener { _binding?.botaoExemplos?.isEnabled = true }
    }

    private fun abrirCasa(casa: Casa) = startActivity(CasaActivity.intent(requireContext(), casa.codigo))

    override fun onDestroyView() {
        escuta?.remove()
        escuta = null
        _binding = null
        super.onDestroyView()
    }
}
