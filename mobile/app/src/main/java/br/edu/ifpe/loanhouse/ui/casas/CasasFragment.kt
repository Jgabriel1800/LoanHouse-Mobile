package br.edu.ifpe.loanhouse.ui.casas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifpe.loanhouse.R
import br.edu.ifpe.loanhouse.data.model.Casa
import br.edu.ifpe.loanhouse.data.repository.CasaRepository
import br.edu.ifpe.loanhouse.databinding.FragmentCasasBinding
import br.edu.ifpe.loanhouse.domain.EstadoCasa
import br.edu.ifpe.loanhouse.domain.Pendencias
import br.edu.ifpe.loanhouse.ui.casa.CasaActivity
import br.edu.ifpe.loanhouse.ui.comum.preencher
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration

/** Aba Casas: todas as casas, na ordem Pendência -> Alugada em dia -> Vaga (igual ao casas.html do site). */
class CasasFragment : Fragment() {

    private var _binding: FragmentCasasBinding? = null
    private val binding get() = _binding!!

    private val repositorio = CasaRepository()
    private var escuta: ListenerRegistration? = null
    private val adapter = CasaAdapter { abrirCasa(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCasasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cabecalho.preencher(getString(R.string.nav_casas))
        binding.listaCasas.layoutManager = LinearLayoutManager(requireContext())
        binding.listaCasas.adapter = adapter
        binding.textoTitulo.text = getString(R.string.casas_titulo, 0)
        binding.botaoCadastrar.setOnClickListener {
            Toast.makeText(requireContext(), R.string.em_breve_cadastro, Toast.LENGTH_LONG).show()
        }

        escuta = repositorio.escutarCasas(
            aoReceber = { casas -> _binding?.let { mostrar(casas) } },
            aoFalhar = { erro -> _binding?.let { mostrarErro(erro) } },
        )
    }

    private fun mostrar(todas: List<Casa>) {
        val casas = Pendencias.ordenar(todas)
        val porEstado = casas.groupingBy { Pendencias.estado(it) }.eachCount()
        binding.progresso.visibility = View.GONE
        binding.textoTitulo.text = getString(R.string.casas_titulo, casas.size)
        binding.textoResumo.text = getString(
            R.string.casas_resumo,
            porEstado[EstadoCasa.COM_PENDENCIA] ?: 0,
            porEstado[EstadoCasa.ALUGADA] ?: 0,
            porEstado[EstadoCasa.VAGA] ?: 0,
        )
        adapter.submitList(casas)
        binding.textoMensagem.visibility = if (casas.isEmpty()) View.VISIBLE else View.GONE
        if (casas.isEmpty()) binding.textoMensagem.setText(R.string.casas_vazio)
    }

    private fun mostrarErro(erro: FirebaseFirestoreException) {
        binding.progresso.visibility = View.GONE
        binding.textoMensagem.visibility = View.VISIBLE
        binding.textoMensagem.setText(
            if (erro.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) R.string.casas_sem_permissao else R.string.casas_erro,
        )
    }

    private fun abrirCasa(casa: Casa) = startActivity(CasaActivity.intent(requireContext(), casa.codigo))

    override fun onDestroyView() {
        escuta?.remove()
        escuta = null
        _binding = null
        super.onDestroyView()
    }
}
