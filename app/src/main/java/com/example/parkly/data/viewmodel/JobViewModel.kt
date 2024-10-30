package com.example.parkly.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.Job
import com.example.parkly.data.SaveJob
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class JobViewModel(val app: Application) : AndroidViewModel(app) {
    private val JOBS = Firebase.firestore.collection("job")
    private val SAVE_JOBS = Firebase.firestore.collection("save_job")
    private val saveJobsLD = MutableLiveData<List<SaveJob>>()
    private val jobsLD = MutableLiveData<List<Job>>()
    private var listener: ListenerRegistration? = null
    private var listener2: ListenerRegistration? = null
    private val required = "* Required"

    init {
        listener = JOBS.addSnapshotListener { snap, _ ->
            jobsLD.value = snap?.toObjects()
            updateResult()
            updateArchived()
        }
        listener2 = SAVE_JOBS.addSnapshotListener{ snap, _ ->
            saveJobsLD.value = snap?.toObjects()
        }
    }

    override fun onCleared() {
        listener?.remove()
        listener2?.remove()
    }

    fun init() = Unit

    fun getJobsLD() = jobsLD

    fun getAll() = jobsLD.value ?: emptyList()

    fun get(jobID: String) = getAll().find { it.jobID == jobID }

    fun getJobLive(jobList: List<Job>, jobID: String) = jobList.find { it.jobID == jobID }!!

    fun reloadJob() {jobsLD.value = getAll()}

    fun set(job: Job) {
        JOBS.document().set(job)
    }

    suspend fun update(job: Job) {
        JOBS.document(job.jobID).set(job).await()
    }

    fun getSaveJobLD() = saveJobsLD

    fun getAllSaveJob() = saveJobsLD.value ?: emptyList()

    fun getSaveJobByUser(userID: String): List<SaveJob> {
        var list = getAllSaveJob()

        list = list.filter {
            it.userID == userID
        }

        saveJobsLD.value = list

        return list
    }

    fun saveJob(saveJob: SaveJob) {
        SAVE_JOBS.document(saveJob.id).set(saveJob)
    }

    fun unsaveJob(id: String) {
        SAVE_JOBS.document(id).delete()
    }

    fun validateInput(field: TextInputLayout, fieldValue: String): Boolean {
        val isValid = !fieldValue.isNullOrEmpty()
        field.helperText = if (isValid) "" else required
        return isValid
    }

    fun validateSalaryInput(
        minInput: TextInputLayout,
        maxInput: TextInputLayout,
        min: Int?,
        max: Int?
    ): Boolean {
        val isMinValid = min != null
        val isMaxValid = max != null

        minInput.helperText = if (isMinValid) "" else required
        maxInput.helperText = if (isMaxValid) "" else required

        if (isMinValid && isMaxValid) {
            if (min!! > max!!) {
                minInput.helperText = "Minimum salary must be less than maximum salary"
                maxInput.helperText = "Maximum salary must be greater than minimum salary"
                return false
            }
        }

        return isMinValid && isMaxValid
    }

    private val archivedLD = MutableLiveData<List<Job>>()

    fun getArchivedLD() = archivedLD

    fun updateArchived() {
        var list = getAll()

        list = list.filter {
            it.deletedAt != 0.toLong()
        }

        archivedLD.value = list

    }

    fun getJobByCompany(companyID:String) {
        var list = getAll()

        list = list.filter {
            it.companyID == companyID
        }
        list = list.filter {
            it.deletedAt == 0.toLong()
        }

        resultLD.value = list
    }

    private val resultLD = MutableLiveData<List<Job>>()
    private var search = ""
    private var position = emptyList<String>()
    private var jobType = emptyList<String>()
    private var workplace = emptyList<String>()
    private var salary = emptyList<String>()

    fun getResultLD() = resultLD

    fun clearSearch() {
        search = ""
        position = emptyList()
        jobType = emptyList()
        workplace = emptyList()
        salary = emptyList()
        updateResult()
    }

    fun search(search: String) {
        this.search = search
        updateResult()
    }

    fun filterPosition(position: List<String>) {
        this.position = position
        updateResult()
    }

    fun filterJobType(jobType: List<String>) {
        this.jobType = jobType
        updateResult()
    }

    fun filterWorkplace(workplace: List<String>) {
        this.workplace = workplace
        updateResult()
    }

    fun filterSalary(salary: List<String>) {
        this.salary = salary
        updateResult()
    }

    fun updateResult() {
        var list = getAll()

        list = list.filter {
            it.deletedAt == 0.toLong()
        }

        list = list.filter {
            it.jobName.contains(search, true) || it.company.name.contains(search, true)
        }

        if (position.isNotEmpty()) {
            list = list.filter { job ->
                position.any { it in job.position }
            }
        }

        if (jobType.isNotEmpty()) {
            list = list.filter { job ->
                jobType.any { it in job.jobType }
            }
        }

        if (workplace.isNotEmpty()) {
            list = list.filter { job ->
                workplace.any { it in job.workplace }
            }
        }

        if (salary.isNotEmpty()) {
            val minSalary = salary[0].toInt()
            val maxSalary = salary[1].toInt()
            if (minSalary != 0 && maxSalary != 999999) {
                list = list.filter { job ->
                    minSalary >= job.minSalary && maxSalary <= job.maxSalary
                }
            }

        }

        resultLD.value = list
    }

}