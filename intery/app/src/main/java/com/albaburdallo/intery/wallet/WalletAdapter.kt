package com.albaburdallo.intery.wallet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.albaburdallo.intery.R
import com.albaburdallo.intery.model.entities.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class WalletAdapter(val transactions: MutableList<Transaction>): RecyclerView.Adapter<WalletAdapter.ViewHolder>() {

    private var clickListener: ClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val db = FirebaseFirestore.getInstance()
        val context: Context = itemView.context

        private val expenditureIcon = itemView.findViewById<View>(R.id.expenditureImageView) as ImageView
        private val incomeIcon = itemView.findViewById<View>(R.id.incomeImageView) as ImageView
        private val transactionName = itemView.findViewById<View>(R.id.transNameTextView) as TextView
        private val transactionDate = itemView.findViewById<View>(R.id.transDateTextView) as TextView
        private val transactionMoney = itemView.findViewById<View>(R.id.moneyTextView) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(transaction: Transaction, transactions: MutableList<Transaction>) {
            if(transaction.expenditure) {
                expenditureIcon.visibility = View.VISIBLE
                incomeIcon.visibility = View.GONE
                transactionMoney.text = "- " + transaction.money
            } else if ( transaction.income) {
                expenditureIcon.visibility = View.GONE
                incomeIcon.visibility = View.VISIBLE
                transactionMoney.text = "+ " + transaction.money
            }
            transactionName.text = transaction.concept
            transactionDate.text = formatDate(transaction.date)

        }

        override fun onClick(v: View?) {
            if (v!=null) {
                clickListener?.onItemCLick(v, adapterPosition)
            }
        }

        private fun formatDate(date: Date): String {
            val pattern = "dd/MM/yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern)
            return simpleDateFormat.format(date)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wallet_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletAdapter.ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction, transactions)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    interface ClickListener {
        fun onItemCLick(v: View, position: Int)
    }
}