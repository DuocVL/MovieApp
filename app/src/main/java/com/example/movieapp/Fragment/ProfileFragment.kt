package com.example.movieapp.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movieapp.Activities.LoginActivity
import com.example.movieapp.Activities.SavedMovieListActivity
import com.example.movieapp.R
import com.example.movieapp.SessionManager
import com.example.movieapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logOutButton.setOnClickListener {
            val sessionManager = SessionManager(requireContext())
            sessionManager.clearSession()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.listSavedMovieButton.setOnClickListener {
            val intent = Intent(requireContext(), SavedMovieListActivity::class.java)
            startActivity(intent)
        }
    }

}