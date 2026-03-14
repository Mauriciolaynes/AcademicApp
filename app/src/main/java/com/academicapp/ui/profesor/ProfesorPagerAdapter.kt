package com.academicapp.ui.profesor

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfesorPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InicioFragment()
            1 -> ListaCursosFragment.newInstance("asistencia")
            2 -> ListaCursosFragment.newInstance("notas")
            3 -> PerfilFragment()
            else -> InicioFragment()
        }
    }
}
