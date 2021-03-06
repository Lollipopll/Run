package com.uestc.run

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.uestc.request.handler.Request
import com.uestc.request.model.Result
import com.uestc.run.basebase.BaseActivity
import com.uestc.run.basebase.BaseViewModel
import com.uestc.run.net.Banner
import com.uestc.run.net.TestService
import com.uestc.run.net.WanResponse
import com.uestc.run.widget.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : BaseActivity<TestViewModel>() {

    override fun initViewModel(): TestViewModel {
        return get(TestViewModel::class.java)
    }

    override fun layoutId(): Int {
        return R.layout.activity_main
    }


    override fun initPage(savedInstanceState: Bundle?) {
        Request.init(this, "https://www.wanandroid.com") {
            okHttp {
                //配置okhttp
                it
            }

            retrofit {
                //配置retrofit
                it
            }
        }


        test.setOnClickListener {
            viewModel.loadDSL()
        }
        test2.setOnClickListener {
            viewModel.loadCallback()
        }
        test3.setOnClickListener {
            viewModel.loadLiveData().observe(this, Observer {
                when (it) {
                    is Result.Error -> {
                        hideLoading()
                    }
                    is Result.Response -> {
                        hideLoading()
                        it.response.apply {
                            Log.e("onResponse-->", Gson().toJson(this))
                            showToast(Gson().toJson(this))
                        }
                    }
                    is Result.Start -> {
                        Log.e("Start-->", Thread.currentThread().name)
                        showLoading()
                    }
                    is Result.Finally -> {
                        Log.e("Finally-->", Thread.currentThread().name)
                    }
                    else -> {

                    }

                }
            })
        }

    }

    override fun initLivedata(viewModel: TestViewModel) {
        viewModel.liveData.observe(this, Observer {
            showToast(Gson().toJson(it))
        })
    }
}


class TestViewModel : BaseViewModel() {

    private val service by lazy { Request.apiService(TestService::class.java) }

    val liveData = MutableLiveData<WanResponse<List<Banner>>>()


    fun loadDSL() {
        apiDSL<WanResponse<List<Banner>>> {

            onRequest {
                Log.e("Thread-->onRequest", Thread.currentThread().name)
                service.getBanner()
            }

            onResponse {
                Log.e("Thread-->onResponse", Thread.currentThread().name)
                Log.e("onResponse-->", Gson().toJson(it))
                liveData.value = it
            }


            onStart {
                Log.e("Thread-->onStart", Thread.currentThread().name)
                false
            }

            onError {
                it.printStackTrace()
                Log.e("Thread-->onError", Thread.currentThread().name)
                false
            }

        }

    }

    fun loadCallback() {
        apiCallback({
            Log.e("Thread-->onRequest", Thread.currentThread().name)
            service.getBanner()
        }, {
            Log.e("Thread-->onResponse", Thread.currentThread().name)
            Log.e("onResponse-->", Gson().toJson(it))
            liveData.value = it
        })

    }

    fun loadLiveData(): LiveData<Result<WanResponse<List<Banner>>>> {
        return apiLiveData(context = SupervisorJob() + Dispatchers.Main.immediate, timeoutInMs = 2000) {
            service.getBanner()
        }
    }


}