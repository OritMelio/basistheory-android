package com.basistheory.android.example.view.reveal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.basistheory.Token
import com.basistheory.android.example.databinding.FragmentRevealBinding
import com.basistheory.android.example.util.tokenExpirationTimestamp
import com.basistheory.android.example.viewmodel.CardFragmentViewModel
import com.basistheory.android.service.ProxyRequest
import com.basistheory.android.service.getElementValueReference
import com.basistheory.android.service.getValue

class RevealDataFragment : Fragment() {
    private val binding: FragmentRevealBinding by lazy {
        FragmentRevealBinding.inflate(layoutInflater)
    }
    private val viewModel: CardFragmentViewModel by viewModels()
    private var tokenId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.cvc.cardNumberElement = binding.cardNumber

        binding.tokenizeButton.setOnClickListener { tokenize() }
        binding.revealButton.setOnClickListener { reveal() }

        setValidationListeners()

        return binding.root
    }

    private fun tokenize() =
        viewModel.tokenize(
            object {
                val type = "card"
                val data = object {
                    val number = binding.cardNumber
                    val expiration_month = binding.expirationDate.month()
                    val expiration_year = binding.expirationDate.year()
                    val cvc = binding.cvc
                }
                val expires_at = tokenExpirationTimestamp()
            }).observe(viewLifecycleOwner) {
            tokenId = it.getValue<String>("id")
        }

    /**
     * demonstrates how an application could potentially wire up custom validation behaviors
     */
    private fun setValidationListeners() {
        binding.cardNumber.addChangeEventListener {
            viewModel.cardNumber.observe(it)
        }
        binding.expirationDate.addChangeEventListener {
            viewModel.cardExpiration.observe(it)
        }
        binding.cvc.addChangeEventListener {
            viewModel.cardCvc.observe(it)
        }
    }

    private fun reveal() {

        val proxyRequest: ProxyRequest = ProxyRequest().apply {
            headers = mapOf(
                "BT-PROXY-URL" to "https://echo.basistheory.com/post",
                "Content-Type" to "application/json"
            )
            body = object {
                val card = "{{ $tokenId }}"
            }
        }

        if (tokenId != null) {
            viewModel.getToken(tokenId!!).observe(viewLifecycleOwner) {
                binding.revealedCardNumber
                    .setValueRef(
                        it.data.getElementValueReference("number")
                    )

                binding.revealedExpirationDate
                    .setValueRef(
                        it.data.getElementValueReference("expiration_month"),
                        it.data.getElementValueReference("expiration_year"),
                    )
            }

            viewModel.proxy(proxyRequest).observe(viewLifecycleOwner) {
                binding.revealedCvc.setValueRef(it.getElementValueReference("json.card.cvc"))
            }
        }
    }
}
