package com.hankki.fooddeal.ux.recyclerview;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hankki.fooddeal.R;

import java.util.ArrayList;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewholder> {
    private ArrayList<String> addressItems;
    private AddressViewholder viewHolder;
    private OnItemClickListener listener = null;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

    public interface OnItemClickListener
    {
        void onItemClick(View v, int pos);
    }

    public class AddressViewholder extends RecyclerView.ViewHolder {
        private TextView tv_address;
        private View parentView;

        public AddressViewholder(@NonNull View view) {
            super(view);
            tv_address = view.findViewById(R.id.tv_address);
            parentView = view.findViewById(R.id.layout_address);
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)
                    {
                        listener.onItemClick(v, pos);
                    }
                }
            });
        }
    }

    public AddressAdapter(ArrayList<String> list) {
        this.addressItems = list;
    }

    @NonNull
    @Override
    public AddressViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(),R.layout.address_item,null);
        viewHolder = new AddressViewholder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewholder holder, int position) {
        String item = addressItems.get(position);
        holder.tv_address.setText(item);
    }

    @Override
    public int getItemCount() {
        return addressItems.size();
    }


}
