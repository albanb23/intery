package com.albaburdallo.intery.wallet

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.util.entities.Transaction
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WalletAdapter(options: FirestoreRecyclerOptions<Transaction>
): FirestoreRecyclerAdapter<Transaction,WalletAdapter.ViewHolder>(options) {

    private var clickListener: ClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val db = FirebaseFirestore.getInstance()
        val context: Context = itemView.context

        private val expenditureIcon = itemView.findViewById<View>(R.id.expenditureImageView) as ImageView
        private val incomeIcon = itemView.findViewById<View>(R.id.incomeImageView) as ImageView
        private val transactionName = itemView.findViewById<View>(R.id.transNameTextView) as TextView
        private val transactionDate = itemView.findViewById<View>(R.id.transDateTextView) as TextView
        private val transactionMoney = itemView.findViewById<View>(R.id.moneyTextView) as TextView
        private val currency = itemView.findViewById<View>(R.id.walletCurrencyTextView)as TextView

        init {
            itemView.setOnClickListener(this)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun bind(transaction: Transaction) {
            if(transaction.expenditure) {
                expenditureIcon.visibility = View.VISIBLE
                incomeIcon.visibility = View.GONE
                transactionMoney.text = "- " + transaction.money
            } else if ( transaction.income) {
                expenditureIcon.visibility = View.GONE
                incomeIcon.visibility = View.VISIBLE
                transactionMoney.text = "+ " + transaction.money
            }

            if (transaction.concept.length > 20) {
                (transaction.concept.substring(0, 20) + "...").also { transactionName.text = it }
            } else {
                transactionName.text = transaction.concept
            }
            transactionDate.text = formatDate(transaction.date)

            db.collection("common").document(FirebaseAuth.getInstance().currentUser?.email.toString()).get().addOnSuccessListener {
                val curr = it.get("currency") as String
                currency.text = curr
            }

        }

        override fun onClick(v: View?) {
            if (v!=null) {
                clickListener?.onItemCLick(v, adapterPosition)
            }
        }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun formatDate(date: Date): String {
            val pattern = "dd/MM/yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern, context.resources?.configuration?.locales?.get(0))
            return simpleDateFormat.format(date)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wallet_list, parent, false)
        return ViewHolder(view)
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemCLick(v: View, position: Int)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Transaction) {
        holder.bind(model)
    }
}