package com.github.yzheka.multistateview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.yzheka.multistateview.MultiStateView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MultiStateView.OnViewStateChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content.setOnClickListener { stateView.showContent() }
        empty.setOnClickListener { stateView.showEmpty() }
        error.setOnClickListener { stateView.showError() }
        loading.setOnClickListener { stateView.showLoading() }
        stateView.addOnViewStateChangeListener(this)
        onViewStateChanged(stateView,stateView.state)
    }

    override fun onViewStateChanged(view: MultiStateView, viewState: MultiStateView.ViewState) {
        content.isChecked=view.isInContentState
        empty.isChecked=view.isInEmptyState
        error.isChecked=view.isInErrorState
        loading.isChecked=view.isInLoadingState
    }
}
