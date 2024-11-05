package com.shubhit.womensafetyapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shubhit.womensafetyapp.databinding.ContactItemBinding

class ContactAdapter(private val contacts: MutableList<Contact>,
                     private val onDeleteClick: (Contact) -> Unit,
    private val onCallClick:(Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding =ContactItemBinding.inflate(LayoutInflater.from(parent.context))
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
        holder.binding.btnDeleteContact.setOnClickListener {
            onDeleteClick(contact)
        }
        holder.binding.btnDialContact.setOnClickListener{
            onCallClick(contact)
        }
    }

    override fun getItemCount() = contacts.size

    class ContactViewHolder(val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.tvContactName.text = contact.name
            binding.tvContactNumber.text = contact.number

        }
    }
}