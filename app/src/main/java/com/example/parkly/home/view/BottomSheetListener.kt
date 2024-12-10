package com.example.parkly.home.view

interface BottomSheetListener {


    enum class Type { POSITION, JOB_TYPE, WORKPLACE, SALARY }
    fun onValueSelected(value: List<String>, type: Type)
}