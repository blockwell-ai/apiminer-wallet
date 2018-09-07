package com.apiminer.demos.wallet.view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.apiminer.demos.wallet.R
import com.apiminer.demos.wallet.api.Transfer
import kotlinx.android.synthetic.main.item_transfer.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource
import java.math.BigDecimal

class TransferAdapter : RecyclerView.Adapter<TransferHolder>() {
    private var items = listOf<Transfer>()
    var userAccount: String = ""

    fun setTransfers(transfers: Array<Transfer>) {
        items = transfers.sortedByDescending { it.blockNumber }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = TransferHolder(parent.context.layoutInflater.inflate(R.layout.item_transfer, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TransferHolder, position: Int) {
        val transfer = items[position]
        holder.bind(transfer, transfer.to.compareTo(userAccount, true) == 0)
    }
}

class TransferHolder(val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(transfer: Transfer, received: Boolean) {
        val res = view.resources
        val value = BigDecimal(transfer.value).divide(BigDecimal(1e18))
        if (received) {
            view.direction.setText(R.string.received)
            view.direction.textColorResource = R.color.colorAccent

            view.value.text = res.getString(R.string.positive_value, value)
            view.value.textColorResource = R.color.colorAccent

            view.address.text = res.getString(R.string.transfer_address,
                    res.getString(R.string.from),
                    transfer.from)
        } else {
            view.direction.setText(R.string.sent)
            view.direction.textColorResource = R.color.colorSecondary

            view.value.text = res.getString(R.string.negative_value, value)
            view.value.textColorResource = R.color.colorSecondary

            view.address.text = res.getString(R.string.transfer_address,
                    res.getString(R.string.to),
                    transfer.to)
        }

        view.blocknum.text = res.getString(R.string.block_number, transfer.blockNumber)
    }
}
