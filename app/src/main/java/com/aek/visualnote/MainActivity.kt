package com.aek.visualnote

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.list_page_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.list_menu_add_button -> {
                val action = ListFragmentDirections.actionListFragmentToSaveDataFragment( enumActionNeed = ActionNeed.AddData)
                Navigation.findNavController(this, R.id.fragment).navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)

    }
}