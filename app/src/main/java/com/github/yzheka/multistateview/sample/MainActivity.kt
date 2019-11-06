package com.github.yzheka.multistateview.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.yzheka.multistateview.MultiStateView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MultiStateView.OnViewStateChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content.setOnClickListener { stateView.state=MultiStateView.ViewState.CONTENT }
        empty.setOnClickListener { stateView.state=MultiStateView.ViewState.EMPTY }
        error.setOnClickListener { stateView.state=MultiStateView.ViewState.ERROR }
        loading.setOnClickListener { stateView.state= MultiStateView.ViewState.LOADING }
        stateView.addOnViewStateChangeListener(this)
        onViewStateChanged(stateView,stateView.state)
    }

    override fun onViewStateChanged(view: MultiStateView, viewState: MultiStateView.ViewState) {
        content.isChecked=viewState==MultiStateView.ViewState.CONTENT
        empty.isChecked=viewState==MultiStateView.ViewState.EMPTY
        error.isChecked=viewState==MultiStateView.ViewState.ERROR
        loading.isChecked=viewState==MultiStateView.ViewState.LOADING
    }
}
