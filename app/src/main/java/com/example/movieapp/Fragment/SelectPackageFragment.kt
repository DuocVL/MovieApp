package com.example.movieapp.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Activities.PackagePaymentActivity
import com.example.movieapp.Adapters.PackageAdapter
import com.example.movieapp.Dataclass.PackagePayment
import com.example.movieapp.databinding.FragmentSelectPackageBinding

class SelectPackageFragment : Fragment(){
    private var _binding : FragmentSelectPackageBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectPackageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.rvPackages
        val packages = listOf(
            PackagePayment("1", "Gói theo tháng", "30.000đ/tháng","1"),
            PackagePayment("2", "Gói theo quý", "99.000đ/quý","3"),
            PackagePayment("3", "Gói theo năm", "299.000đ/năm","12")
        )
        val adapter = PackageAdapter(packages){ packagePayment ->
            (activity as? PackagePaymentActivity)?.apply {
                setSelectedPackage(packagePayment)
                goToPayment()
            }
        }
        Log.d("PackageAdapter", "Number of packages: ${packages.size}")
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter = adapter
    }
}