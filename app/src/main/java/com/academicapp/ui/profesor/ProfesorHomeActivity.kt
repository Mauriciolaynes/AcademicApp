package com.academicapp.ui.profesor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.academicapp.R
import com.academicapp.databinding.ActivityProfesorHomeBinding

class ProfesorHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfesorHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfesorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBottomNav()
    }

    private fun setupViewPager() {
        val adapter = ProfesorPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Sincronizar ViewPager con BottomNav cuando se desliza
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.nav_asistencia -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.nav_notas -> {
                    binding.viewPager.currentItem = 2
                    true
                }
                R.id.nav_perfil -> {
                    binding.viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem != 0) {
            binding.viewPager.currentItem = 0
        } else {
            super.onBackPressed()
        }
    }
}
