package br.edu.ifpe.loanhouse.ui.embreve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import br.edu.ifpe.loanhouse.databinding.FragmentEmBreveBinding
import br.edu.ifpe.loanhouse.ui.comum.preencher

/** Abas Inquilinos, Calendário e Recursos enquanto as tasks delas não chegam. */
class EmBreveFragment : Fragment() {

    private var _binding: FragmentEmBreveBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentEmBreveBinding.inflate(inflater, container, false)
        _binding = binding
        val args = requireArguments()
        binding.cabecalho.preencher(getString(args.getInt(ARG_TITULO)))
        binding.icone.setImageResource(args.getInt(ARG_ICONE))
        binding.mensagem.setText(args.getInt(ARG_MENSAGEM))
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TITULO = "titulo"
        private const val ARG_MENSAGEM = "mensagem"
        private const val ARG_ICONE = "icone"

        fun novo(@StringRes titulo: Int, @StringRes mensagem: Int, @DrawableRes icone: Int) = EmBreveFragment().apply {
            arguments = bundleOf(ARG_TITULO to titulo, ARG_MENSAGEM to mensagem, ARG_ICONE to icone)
        }
    }
}
