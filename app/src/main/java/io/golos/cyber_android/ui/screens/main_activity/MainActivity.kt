package io.golos.cyber_android.ui.screens.main_activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import io.golos.cyber_android.R
import io.golos.cyber_android.application.App
import io.golos.cyber_android.databinding.ActivityMainBinding
import io.golos.cyber_android.ui.screens.main_activity.di.MainActivityComponent
import io.golos.cyber_android.ui.common.base.ActivityBase
import io.golos.cyber_android.ui.common.mvvm.ActivityBaseMVVM
import io.golos.cyber_android.ui.common.mvvm.viewModel.ActivityViewModelFactory
import io.golos.cyber_android.ui.common.mvvm.view_commands.ViewCommand
import io.golos.cyber_android.ui.screens.dashboard.di.DashboardFragmentComponent
import io.golos.cyber_android.ui.screens.dashboard.view.DashboardFragment
import io.golos.cyber_android.ui.screens.ftue.di.FtueFragmentComponent
import io.golos.cyber_android.ui.screens.ftue.view.FtueFragment
import io.golos.cyber_android.ui.screens.main_activity.view.viewCommand.ContentPage
import io.golos.cyber_android.ui.screens.main_activity.view.viewCommand.NavigateToContentCommand
import io.golos.cyber_android.ui.screens.main_activity.view_model.MainViewModel
import javax.inject.Inject


class MainActivity : ActivityBaseMVVM<ActivityMainBinding, MainViewModel>() {

    override fun provideViewModelType(): Class<MainViewModel> = MainViewModel::class.java

    override fun layoutResId(): Int = R.layout.activity_main

    override fun inject() = App.injections.get<MainActivityComponent>()
        .inject(this)

    override fun releaseInjection() {
        App.injections.release<MainActivityComponent>()
    }

    override fun linkViewModel(binding: ActivityMainBinding, viewModel: MainViewModel) {
        binding.viewModel = viewModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.injections.get<MainActivityComponent>().inject(this)
    }

    override fun processViewCommand(command: ViewCommand) {
        super.processViewCommand(command)
        if(command is NavigateToContentCommand){
            if(command.contentPage == ContentPage.FTUE){
                showFragment(FtueFragment.newInstance(), false)
            } else if(command.contentPage == ContentPage.DASHBOARD){
                showFragment(DashboardFragment.newInstance(), false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showFragment(fragment: Fragment, isAddToBackStack: Boolean = true) {
        val tag = fragment::class.simpleName
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            val beginTransaction = supportFragmentManager.beginTransaction()
            if (isAddToBackStack) {
                beginTransaction.addToBackStack(tag)
            }

            beginTransaction.setCustomAnimations(
                R.anim.nav_slide_in_right,
                R.anim.nav_slide_out_left,
                R.anim.nav_slide_in_left,
                R.anim.nav_slide_out_right)

            beginTransaction
                .add(R.id.rootContainer, fragment, tag)
                .commit()
        }
    }
}
