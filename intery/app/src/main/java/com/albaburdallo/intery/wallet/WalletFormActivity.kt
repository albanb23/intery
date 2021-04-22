package com.albaburdallo.intery.wallet

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieDrawable
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Entity
import com.albaburdallo.intery.util.entities.Section
import com.albaburdallo.intery.util.entities.Transaction
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.android.synthetic.main.activity_wallet_form.*
import kotlinx.android.synthetic.main.activity_wallet_form.toggleGroup
import kotlinx.android.synthetic.main.loading_layout.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class WalletFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    AddEntityFragment.EntityCallBackListener, AddSectionFragment.SectionCallBackListener {

    private lateinit var transactions: ArrayList<Transaction>
    private lateinit var entities: ArrayList<String>
    private lateinit var sections: ArrayList<String>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var transaction: Transaction
    private val authEmail = FirebaseAuth.getInstance().currentUser?.email;
    private lateinit var transactionId: String
    private lateinit var date: Date
    private lateinit var entityAdapter: ArrayAdapter<String>
    private lateinit var sectionAdapter: ArrayAdapter<String>
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0
    var day = 0
    var month: Int = 0
    var year: Int = 0
    var totalMoney: String = "0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_form)
        this.supportActionBar?.hide()

    }

    override fun onStart() {
        super.onStart()
        loadingLottie.setAnimation(R.raw.loading)
        loadingLottie.playAnimation()
        loadingLottie.repeatCount = LottieDrawable.INFINITE

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        transactionId = intent.extras?.getString("transactionId") ?: ""
        totalMoney = prefs.getString("totalMoney", "0.0").toString()
        val form = intent.extras?.getString("form") ?: ""

        backImageView.setOnClickListener { onBackPressed() }

        if (form == "edit") {
            trashImageView.visibility = View.VISIBLE
        } else {
            trashImageView.visibility = View.GONE
        }

        if (transactionId != "") {
            db.collection("wallet").document(transactionId).get().addOnSuccessListener {value ->
                val isExpenditure = value!!.get("expenditure") as Boolean
                val isIncome = value.get("income") as Boolean
                val quantity = value.get("money") as Double
                quantityEditText.setText(quantity.toString())
                conceptEditText.setText(value.get("concept") as String)
                date = (value.get("date") as Timestamp).toDate()
                dateEditText.text = formatDate(date)
                notesEditText.setText(value.get("notes") as String)
                if (isExpenditure) {
                    toggleGroup.check(R.id.expenditureBut)
                } else {
                    toggleGroup.check(R.id.incomeBut)
                }


                trashImageView.setOnClickListener {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(this.resources.getString(R.string.delete))
                        .setCancelable(false)
                        .setPositiveButton(this.resources.getString(R.string.yes)) { dialog, id ->
                            var total = totalMoney.toDouble()
                            if (isExpenditure) {
                                total += quantity
                            } else {
                                total -= quantity
                            }
                            if (authEmail != null) {
                                db.collection("common").document(authEmail)
                                    .update("money", ((total * 100.0).roundToInt() / 100.0).toString())
                            }
                            db.collection("wallet").document(transactionId).delete()
                            showWallet()
                        }
                        .setNegativeButton(this.resources.getString(R.string.no)) { dialog, id ->
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                    loadingLayout.visibility = View.GONE
                }
                loadingLayout.visibility = View.GONE
            }
        }

        //entity spinner
        val entitySpinner: Spinner = findViewById(R.id.entitySpinner)
        entities = arrayListOf()
        entityAdapter = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_item,
            entities
        )
        entityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        entitySpinner.adapter = entityAdapter


        db.collection("entities").orderBy("created").addSnapshotListener { value, error ->
            if (error != null){
                return@addSnapshotListener
            }
            entities.clear()
            entities.add(0, this.resources.getString(R.string.selectEntity))
            entities.add(entities.size, this.resources.getString(R.string.addEntity))
            for (document in value!!) {
                val user = document.get("user") as HashMap<*, *>
                if (user["email"] == authEmail) {
                    val entityName = document.get("name") as String
                    entities.add(1, entityName)
                }
            }
            entityAdapter.notifyDataSetChanged()

            //section spinner
            val sectionSpinner: Spinner = findViewById(R.id.sectionSpinner)
            sections = arrayListOf()
            sectionAdapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_item,
                sections
            )
            sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sectionSpinner.adapter = sectionAdapter

            db.collection("sections").orderBy("created").addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                sections.clear()
                sections.add(0, this.resources.getString(R.string.selectSection))
                sections.add(sections.size, this.resources.getString(R.string.addSection))
                for (document in value!!) {
                    val user = document.get("user") as HashMap<*, *>
                    if (user["email"] == authEmail) {
                        val sectionName = document.get("name") as String
                        sections.add(1, sectionName)
                    }
                }
                sectionAdapter.notifyDataSetChanged()

                if (transactionId != "") {
                    db.collection("wallet").document(transactionId).get().addOnSuccessListener {value ->
                        val ent = value!!.get("entity") as HashMap<*, *>
                        val sec = value.get("section") as HashMap<*, *>
                        for (entity in entities) {
                            if (entity == ent["name"]) {
                                entitySpinner.setSelection(entities.indexOf(entity))
                                break
                            }
                        }
                        for (section in sections) {
                            if (section == sec["name"]) {
                                sectionSpinner.setSelection(sections.indexOf(section))
                                break
                            }
                        }
                    }
                }

                entitySpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {
                        if (position == entities.size - 1) {
                            AddEntityFragment.newInstance().show(supportFragmentManager, "new")
                            entityAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }

                sectionSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {
                        if (position == sections.size - 1) {
                            AddSectionFragment.newInstance().show(supportFragmentManager, "new")
                            sectionAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }
            }
        }

        transactions = arrayListOf()
        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, this, year, month, day)
            datePickerDialog.show()
        }

        saveTransactionButton.setOnClickListener {
            if (validateForm()) {
                addTransaction()
            }
        }
    }

    private fun addTransaction() {
        var isExpenditure = false
        var isIncome = false
        val buttonId: Int = toggleGroup.checkedButtonId
        if (buttonId == R.id.expenditureBut) {
            isExpenditure = true
        } else {
            isIncome = true
        }
        val money = quantityEditText.text
        val concept = conceptEditText.text
        val notes = notesEditText.text
        val entityName = entitySpinner.selectedItem.toString()
        var entity: Entity? = null
        db.collection("entities").whereEqualTo("name", entityName).addSnapshotListener { value, error ->
            if (error != null){
                return@addSnapshotListener
            }
            for (document in value!!) {
                val user = document.get("user") as HashMap<*, *>
                if (user["email"] == authEmail) {
                    entity = Entity(
                        document.get("id") as String,
                        document.get("name") as String
                    )
                }
            }

            val sectionName = sectionSpinner.selectedItem.toString()
            var section: Section? = null
            db.collection("sections").whereEqualTo("name", sectionName).addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                for (document in value!!) {
                    val user = document.get("user") as HashMap<*, *>
                    if (user["email"] == authEmail) {
                        section = Section(
                            document.get("id") as String,
                            document.get("name") as String
                        )
                    }
                }

                val idList = arrayListOf<Int>()
                db.collection("wallet").orderBy("id", Query.Direction.DESCENDING).get().addOnSuccessListener {value ->
                    for (document in value!!) {
                        val documentId = document.get("id") as String
                        val subId = documentId.substring(0, documentId.indexOf("-"))
                        idList.add(subId.toInt())
                    }

                    var id = transactionId
                    if (id == "") {
                        var int = 1
                        if (idList.isNotEmpty()) {
                            int = idList[0] + 1
                        }
                        id = int.toString() + "-" + concept.toString() + "-" + authEmail
                    }

                    transaction = Transaction(
                        id,
                        isExpenditure,
                        isIncome,
                        concept.toString(),
                        money.toString().toDouble(),
                        date,
                        notes.toString(),
                        entity,
                        section
                    )

                    if (authEmail != null) {
                        var total = totalMoney.toDouble()
                        if (transactionId == "") {
                            if (isExpenditure) {
                                total -= transaction.money
                            } else if (isIncome) {
                                total += transaction.money
                            }
                            db.collection("common").document(authEmail)
                                .update(
                                        "money", ((total * 100.0).roundToInt() / 100.0).toString()
                                    )
                            db.collection("wallet").document(transaction.id)
                                .set(transaction)
                        } else {
                            db.collection("wallet").document(transactionId).get().addOnSuccessListener {value ->
                                val current = value!!.get("money") as Double
                                val modified = money.toString().toDouble()
                                //si se cambia solo la cantidad
                                if (modified - current != 0.0) {
                                    if (isExpenditure) {
                                        total -= (modified - current)
                                    } else if (isIncome) {
                                        total += (modified - current)
                                    }
                                }
                                //si se cambia de gasto a ingreso
                                if (value.get("expenditure") as Boolean != isExpenditure) {
                                    if (isExpenditure) {
                                        total -= (current + modified)
                                    } else if (isIncome) {
                                        total += (current + modified)
                                    }
                                }
                                if (total>0) {
                                    db.collection("common").document(authEmail)
                                        .update(
                                                "money",((total * 100.0).roundToInt() / 100.0).toString()
                                        )
                                    db.collection("wallet").document(transaction.id)
                                        .set(transaction)
                                } else {
                                    Toast.makeText(this, resources.getString(R.string.totalZero),Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    transactions.add(transaction)
                    showWallet()
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var res = true
        res =
            quantityEditText.validator().nonEmpty().addErrorCallback { quantityEditText.error = it }
                .check()
                    && conceptEditText.validator().nonEmpty()
                .addErrorCallback { conceptEditText.error = it }
                .check()
                    && dateEditText.validator().nonEmpty()
                .addErrorCallback { dateEditText.error = it }
                .check()
        if (sectionSpinner.selectedItem == resources.getString(R.string.selectSection)) {
            Toast.makeText(
                this,
                this.getString(R.string.validateSection),
                Toast.LENGTH_LONG
            ).show()
            res = false
        }
        if (entitySpinner.selectedItem == resources.getString(R.string.selectEntity)) {
            Toast.makeText(
                this,
                this.getString(R.string.validateEntity),
                Toast.LENGTH_LONG
            ).show()
            res = false
        }
        return res
    }


    override fun onEntityAdded(entity: Entity) {
        val query = db.collection("entities")
        query.whereEqualTo("user.email", authEmail).get().addOnSuccessListener {value ->
            var exists = false
            for (document in value!!) {
                val entityName = document.get("name") as String
                if (entityName == entity.name) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                entities.add(1, entity.name)
                query.document(entity.id).set(entity as Entity)
                entitySpinner.setSelection(entities.indexOf(entity.name))
                entityAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(
                    this,
                    this.resources.getString(R.string.entityExists),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onSectionAdded(section: Section) {
        val query = db.collection("sections")
        query.whereEqualTo("user.email", authEmail).get().addOnSuccessListener {value ->
            var exists = false
            for (document in value!!) {
                val sectionName = document.get("name") as String
                if (sectionName == section.name) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                sections.add(1, section.name)
                query.document(section.id).set(section as Section)
                sectionSpinner.setSelection(sections.indexOf(section.name))
                sectionAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(
                    this,
                    this.resources.getString(R.string.sectionExists),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showWallet() {
        val listIntent = Intent(this, WalletActivity::class.java)
        startActivity(listIntent)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = dayOfMonth
        myMonth = month
        myYear = year
        val dateString = myDay.toString() + "/" + (myMonth + 1).toString() + "/" + myYear.toString()
        val calendar: Calendar = Calendar.getInstance()
        date = Date(myYear - 1900, myMonth, myDay)
        dateEditText.text = formatDate(date)
    }

    private fun formatDate(date: Date): String {
        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(date)
    }

}